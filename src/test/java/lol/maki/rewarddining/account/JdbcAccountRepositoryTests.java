package lol.maki.rewarddining.account;

import javax.money.MonetaryAmount;
import javax.sql.DataSource;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

@JdbcTest(properties = { "logging.level.sql=TRACE" })
@Import(JdbcAccountRepository.class)
class JdbcAccountRepositoryTests {

	@Autowired
	AccountRepository repository;

	@Autowired
	DataSource dataSource;

	@Autowired
	JdbcTemplate jdbcTemplate;

	@Test
	void testFindAccountByCreditCard() {
		Account account = repository.findByCreditCard("1234123412341234");
		// assert the returned account contains what you expect given the state of the database
		assertNotNull(account, "account should never be null");
		assertEquals(Long.valueOf(0), account.getId(), "wrong entity id");
		assertEquals("123456789", account.getNumber(), "wrong account number");
		assertEquals("Keith and Keri Donald", account.getName(), "wrong name");
		assertEquals(2, account.getBeneficiaries().size(), "wrong beneficiary collection size");

		Beneficiary b1 = account.getBeneficiary("Annabelle");
		assertNotNull(b1, "Annabelle should be a beneficiary");
		assertEquals(USD.valueOf("0.00"), b1.getSavings(), "wrong savings");
		assertEquals(Percentage.valueOf("50%"), b1.getAllocationPercentage(), "wrong allocation percentage");

		Beneficiary b2 = account.getBeneficiary("Corgan");
		assertNotNull(b2, "Corgan should be a beneficiary");
		assertEquals(USD.valueOf("0.00"), b2.getSavings(), "wrong savings");
		assertEquals(Percentage.valueOf("50%"), b2.getAllocationPercentage(), "wrong allocation percentage");
	}

	@Test
	void testFindAccountByCreditCardNoAccount() {
		try {
			repository.findByCreditCard("bogus");
			fail("Should've failed");
		}
		catch (EmptyResultDataAccessException e) {
			// expected
		}
	}


	@Test
	void testUpdateBeneficiaries() {
		Account account = repository.findByCreditCard("1234123412341234");
		account.makeContribution(USD.valueOf("8.00"));
		repository.updateBeneficiaries(account);
		verifyBeneficiaryTableUpdated();
	}

	private void verifyBeneficiaryTableUpdated() {
		String sql = "select savings from t_account_beneficiary where name = ? and account_id = ?";
		// assert Annabelle has $4.00 savings now
		MonetaryAmount annabelleAmount = jdbcTemplate.queryForObject(sql, (rs, i) -> USD.valueOf(rs.getString(1)), "Annabelle", 0L);
		assertEquals(USD.valueOf("4.00"), annabelleAmount);
		// assert Corgan has $4.00 savings now
		MonetaryAmount corganAmount = jdbcTemplate.queryForObject(sql, (rs, i) -> USD.valueOf(rs.getString(1)), "Corgan", 0L);
		assertEquals(USD.valueOf("4.00"), corganAmount);
	}
}