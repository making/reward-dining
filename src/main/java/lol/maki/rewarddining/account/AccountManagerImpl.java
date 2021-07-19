package lol.maki.rewarddining.account;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AccountManagerImpl implements AccountManager {
	private final AccountRepository accountRepository;

	public AccountManagerImpl(AccountRepository accountRepository) {
		this.accountRepository = accountRepository;
	}

	@Override
	public List<Account> getAllAccounts() {
		return this.accountRepository.findAll();
	}

	@Override
	public Account getAccount(Long id) {
		try {
			return this.accountRepository.findById(id);
		}
		catch (EmptyResultDataAccessException e) {
			return null;
		}
	}

	@Override
	@Transactional
	public Account save(Account account) {
		return this.accountRepository.insert(account);
	}

	@Override
	@Transactional
	public void update(Account account) {
		this.accountRepository.update(account);
	}

	@Override
	@Transactional
	public void updateBeneficiaryAllocationPercentages(Long accountId, Map<String, Percentage> allocationPercentages) {
		final Account account = this.getAccount(accountId);
		for (Entry<String, Percentage> entry : allocationPercentages.entrySet()) {
			account.getBeneficiary(entry.getKey()).setAllocationPercentage(entry.getValue());
		}
		this.accountRepository.updateBeneficiaries(account);
	}

	@Override
	@Transactional
	public void addBeneficiary(Long accountId, String beneficiaryName) {
		final Account account = this.getAccount(accountId);
		account.addBeneficiary(beneficiaryName, Percentage.zero());
		this.accountRepository.updateBeneficiaries(account);
	}

	@Override
	@Transactional
	public void removeBeneficiary(Long accountId, String beneficiaryName, Map<String, Percentage> allocationPercentages) {
		final Account account = this.getAccount(accountId);
		account.removeBeneficiary(beneficiaryName);
		this.accountRepository.updateBeneficiaries(account);
		if (allocationPercentages != null) {
			this.updateBeneficiaryAllocationPercentages(accountId, allocationPercentages);
		}
	}
}
