package lol.maki.rewarddining.account;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

/**
 * IMPORTANT!!!
 * Per best practices, this class shouldn't be in 'src/main/java' but rather in 'src/test/java'.
 * However, it is used by numerous Test classes inside multiple projects. Maven does not 
 * provide an easy way to access a class that is inside another project's 'src/test/java' folder.
 *
 * Rather than using some complex Maven configuration, we decided it is acceptable to place this test 
 * class inside 'src/main/java'.
 *
 */
public class StubAccountManager implements AccountManager {

	public static final int NUM_ACCOUNTS_IN_STUB = 1;

	private Map<Long, Account> accountsById = new HashMap<Long, Account>();

	private AtomicLong nextEntityId = new AtomicLong(3);

	private final Logger log = LoggerFactory.getLogger(StubAccountManager.class);

	public StubAccountManager() {
		Account account = new Account(0L, "123456789", "Keith and Keri Donald");
		account.addBeneficiary("Annabelle", Percentage.valueOf("50%"));
		account.addBeneficiary("Corgan", Percentage.valueOf("50%"));
		account.getBeneficiary("Annabelle").setId(0L);
		account.getBeneficiary("Corgan").setId(1L);
		accountsById.put(0L, account);

		log.info("Created StubAccountManager");
	}

	@Override
	public List<Account> getAllAccounts() {
		return new ArrayList<Account>(accountsById.values());
	}

	@Override
	public Account getAccount(Long id) {
		Account account = accountsById.get(id);
		if (account == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND);
		}
		return account;
	}

	@Override
	public Account save(Account newAccount) {
		for (Beneficiary beneficiary : newAccount.getBeneficiaries()) {
			beneficiary.setId(nextEntityId.getAndIncrement());
		}

		newAccount.setId(nextEntityId.getAndIncrement());
		accountsById.put(newAccount.getId(), newAccount);
		return newAccount;
	}

	@Override
	public void update(Account account) {
		accountsById.put(account.getId(), account);
	}

	@Override
	public void updateBeneficiaryAllocationPercentages(Long accountId, Map<String, Percentage> allocationPercentages) {
		Account account = accountsById.get(accountId);
		for (Entry<String, Percentage> entry : allocationPercentages.entrySet()) {
			account.getBeneficiary(entry.getKey()).setAllocationPercentage(entry.getValue());
		}
	}

	@Override
	public void addBeneficiary(Long accountId, String beneficiaryName) {
		accountsById.get(accountId).addBeneficiary(beneficiaryName, Percentage.zero());
	}

	@Override
	public void removeBeneficiary(Long accountId, String beneficiaryName, Map<String, Percentage> allocationPercentages) {
		accountsById.get(accountId).removeBeneficiary(beneficiaryName);
		updateBeneficiaryAllocationPercentages(accountId, allocationPercentages);
	}

}
