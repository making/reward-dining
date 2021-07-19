package lol.maki.rewarddining.account;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import javax.money.MonetaryAmount;

import lol.maki.rewarddining.util.FileLoader;

import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

/**
 * Loads accounts from a data source using the JDBC API.
 */
@Repository
public class JdbcAccountRepository implements AccountRepository {

	private final JdbcTemplate jdbcTemplate;

	private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

	public JdbcAccountRepository(JdbcTemplate jdbcTemplate, NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
		this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
	}

	private final ResultSetExtractor<List<Account>> accountsExtractor = new AccountsExtractor();

	@Override
	public List<Account> findAll() {
		final String sql = FileLoader.load("lol/maki/rewarddining/account/AccountRepository/findAll.sql");
		return this.jdbcTemplate.query(sql, this.accountsExtractor);
	}

	@Override
	public Account findById(Long id) {
		final String sql = FileLoader.load("lol/maki/rewarddining/account/AccountRepository/findById.sql");
		final List<Account> accounts = this.jdbcTemplate.query(sql, this.accountsExtractor, id);
		if (accounts.isEmpty()) {
			throw new EmptyResultDataAccessException(1);
		}
		return accounts.get(0);
	}

	@Override
	public Account findByCreditCard(String creditCardNumber) {
		final String sql = FileLoader.load("lol/maki/rewarddining/account/AccountRepository/findByCreditCard.sql");
		final List<Account> accounts = this.jdbcTemplate.query(sql, this.accountsExtractor, creditCardNumber);
		if (accounts.isEmpty()) {
			throw new EmptyResultDataAccessException(1);
		}
		return accounts.get(0);
	}

	@Override
	@Transactional
	public Account insert(Account account) {
		final String insertAccountSql = FileLoader.load("lol/maki/rewarddining/account/AccountRepository/insertAccount.sql");
		final KeyHolder keyHolder = new GeneratedKeyHolder();
		this.jdbcTemplate.update(connection -> {
			final PreparedStatement statement = connection.prepareStatement(insertAccountSql, new String[] { "id" });
			statement.setString(1, account.getNumber());
			statement.setString(2, account.getName());
			return statement;
		}, keyHolder);
		final long accountId = keyHolder.getKey().longValue();
		account.setId(accountId);
		final Set<Beneficiary> beneficiaries = account.getBeneficiaries();
		if (!CollectionUtils.isEmpty(beneficiaries)) {
			final String insertBeneficiarySql = FileLoader.load("lol/maki/rewarddining/account/AccountRepository/insertBeneficiaries.sql");
			final List<Object[]> args = account.getBeneficiaries().stream()
					.map(b -> new Object[] { accountId, b.getName(), b.getAllocationPercentage().asBigDecimal(), b.getSavings().getNumber().numberValue(BigDecimal.class) })
					.collect(Collectors.toList());
			this.jdbcTemplate.batchUpdate(insertBeneficiarySql, args);
		}
		return account;
	}

	@Override
	@Transactional
	public void update(Account account) {
		if (account.getId() == null) {
			this.insert(account);
			return;
		}
		final String updateAccountSql = FileLoader.load("lol/maki/rewarddining/account/AccountRepository/updateAccount.sql");
		this.jdbcTemplate.update(updateAccountSql, account.getNumber(), account.getName(), account.getId());
		this.updateBeneficiaries(account);
	}

	@Override
	@Transactional
	public void updateBeneficiaries(Account account) {
		final Set<Beneficiary> beneficiaries = account.getBeneficiaries();
		final List<Beneficiary> beneficiariesToUpdate = beneficiaries.stream().filter(x -> x.getId() != null).collect(Collectors.toList());
		final List<Beneficiary> beneficiariesToInsert = beneficiaries.stream().filter(x -> x.getId() == null).collect(Collectors.toList());
		if (!CollectionUtils.isEmpty(beneficiariesToUpdate)) {
			final String sql = FileLoader.load("lol/maki/rewarddining/account/AccountRepository/updateBeneficiaries.sql");
			final List<Object[]> args = beneficiariesToUpdate.stream()
					.map(b -> new Object[] { b.getSavings().getNumber().numberValue(BigDecimal.class), account.getId(), b.getName() })
					.collect(Collectors.toList());
			this.jdbcTemplate.batchUpdate(sql, args);
		}
		if (!CollectionUtils.isEmpty(beneficiariesToInsert)) {
			final String sql = FileLoader.load("lol/maki/rewarddining/account/AccountRepository/insertBeneficiaries.sql");
			final List<Object[]> args = beneficiariesToInsert.stream()
					.map(b -> new Object[] { account.getId(), b.getName(), b.getAllocationPercentage().asBigDecimal(), b.getSavings().getNumber().numberValue(BigDecimal.class) })
					.collect(Collectors.toList());
			this.jdbcTemplate.batchUpdate(sql, args);
		}
		final List<String> beneficiaryNames = beneficiaries.stream().map(Beneficiary::getName).collect(Collectors.toList());
		final String sql = FileLoader.load("lol/maki/rewarddining/account/AccountRepository/deleteBeneficiaries.sql");
		this.namedParameterJdbcTemplate.update(sql, Map.of("accountId", account.getId(), "beneficiaryNames", beneficiaryNames));
	}

	/**
	 * Maps the beneficiary columns in a single row to an AllocatedBeneficiary object.
	 *
	 * @param rs the result set with its cursor positioned at the current row
	 * @return an allocated beneficiary
	 * @throws SQLException an exception occurred extracting data from the result set
	 */
	private Beneficiary mapBeneficiary(ResultSet rs) throws SQLException {
		final String name = rs.getString("BENEFICIARY_NAME");
		final MonetaryAmount savings = USD.valueOf(rs.getString("BENEFICIARY_SAVINGS"));
		final Percentage allocationPercentage = Percentage.valueOf(rs.getString("BENEFICIARY_ALLOCATION_PERCENTAGE"));
		final Beneficiary beneficiary = new Beneficiary(name, allocationPercentage, savings);
		beneficiary.setId(rs.getLong("BENEFICIARY_ID"));
		return beneficiary;
	}

	private class AccountsExtractor implements ResultSetExtractor<List<Account>> {

		@Override
		public List<Account> extractData(ResultSet rs) throws SQLException, DataAccessException {
			final List<Account> accounts = new ArrayList<>();
			Account lastAccount = null;
			Long lastAccountId = null;
			while (rs.next()) {
				final Long accountId = rs.getLong("ID");
				if (lastAccount == null || !Objects.equals(lastAccountId, accountId)) {
					if (lastAccount != null) {
						accounts.add(lastAccount);
					}
					final String number = rs.getString("ACCOUNT_NUMBER");
					final String name = rs.getString("ACCOUNT_NAME");
					lastAccount = new Account(number, name);
					lastAccount.setId(accountId);
				}
				if (rs.getString("BENEFICIARY_NAME") != null) {
					lastAccount.restoreBeneficiary(mapBeneficiary(rs));
				}
				lastAccountId = accountId;
			}
			if (lastAccount != null) {
				accounts.add(lastAccount);
			}
			return accounts;
		}
	}
}