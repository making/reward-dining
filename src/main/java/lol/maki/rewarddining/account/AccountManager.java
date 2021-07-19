package lol.maki.rewarddining.account;

import java.util.List;
import java.util.Map;

/**
 * Manages access to account information. Used as the service layer component in
 * the <tt>mvc</tt> and <tt>security</tt> projects.
 */
public interface AccountManager {

	/**
	 * Get all accounts in the system
	 *
	 * @return all accounts
	 */
	List<Account> getAllAccounts();

	/**
	 * Find an account by its number.
	 *
	 * @param id
	 *            the account id
	 * @return the account
	 */
	Account getAccount(Long id);

	/**
	 * Takes a transient account and persists it.
	 *
	 * @param account
	 *            The account to save
	 * @return The persistent account - this may or may not be the same object
	 *         as the method argument.
	 */
	Account save(Account account);

	/**
	 * Takes a changed account and persists any changes made to it.
	 *
	 * @param account
	 *            The account with changes
	 */
	void update(Account account);

	/**
	 * Updates the allocation percentages for the entire collection of
	 * beneficiaries in an account
	 *
	 * @param accountId
	 *            the account id
	 * @param allocationPercentages
	 *            A map of beneficiary names and allocation percentages, keyed
	 *            by beneficiary name
	 */
	void updateBeneficiaryAllocationPercentages(Long accountId,
			Map<String, Percentage> allocationPercentages);

	/**
	 * Adds a beneficiary to an account. The new beneficiary will have a 0
	 * allocation percentage.
	 *
	 * @param accountId
	 *            the account id
	 * @param beneficiaryName
	 *            the name of the beneficiary to remove
	 */
	void addBeneficiary(Long accountId, String beneficiaryName);

	/**
	 * Removes a beneficiary from an account.
	 *
	 * @param accountId
	 *            the account id
	 * @param beneficiaryName
	 *            the name of the beneficiary to remove
	 * @param allocationPercentages
	 *            new allocation percentages, keyed by beneficiary name
	 */
	void removeBeneficiary(Long accountId, String beneficiaryName,
			Map<String, Percentage> allocationPercentages);
}
