package lol.maki.rewarddining.reward;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lol.maki.rewarddining.account.Account;
import lol.maki.rewarddining.account.AccountRepository;
import lol.maki.rewarddining.account.Percentage;

import org.springframework.dao.EmptyResultDataAccessException;

/**
 * A dummy account repository implementation. Has a single Account "Keith and Keri Donald" with two beneficiaries
 * "Annabelle" (50% allocation) and "Corgan" (50% allocation) associated with credit card "1234123412341234".
 *
 * Stubs facilitate unit testing. An object needing an AccountRepository can work with this stub and not have to bring
 * in expensive and/or complex dependencies such as a Database. Simple unit tests can then verify object behavior by
 * considering the state of this stub.
 */
public class StubAccountRepository implements AccountRepository {

	private Map<String, Account> accountsByCreditCard = new HashMap<String, Account>();

	public StubAccountRepository() {
		Account account = new Account("123456789", "Keith and Keri Donald");
		account.addBeneficiary("Annabelle", Percentage.valueOf("50%"));
		account.addBeneficiary("Corgan", Percentage.valueOf("50%"));
		accountsByCreditCard.put("1234123412341234", account);
	}

	@Override
	public List<Account> findAll() {
		return null;
	}

	@Override
	public Account findById(Long id) {
		return null;
	}

	public Account findByCreditCard(String creditCardNumber) {
		Account account = accountsByCreditCard.get(creditCardNumber);
		if (account == null) {
			throw new EmptyResultDataAccessException(1);
		}
		return account;
	}

	@Override
	public Account insert(Account account) {
		return null;
	}

	@Override
	public void update(Account account) {

	}

	public void updateBeneficiaries(Account account) {
		// nothing to do, everything is in memory
	}
}