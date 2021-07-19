package lol.maki.rewarddining.account;

import java.util.List;

/**
 * Loads account aggregates. Called by the reward network to find and reconstitute Account entities from an external
 * form such as a set of RDMS rows.
 *
 * Objects returned by this repository are guaranteed to be fully-initialized and ready to use.
 */
public interface AccountRepository {
	/**
	 * Get all accounts in the system
	 *
	 * @return all accounts
	 */
	List<Account> findAll();

	/**
	 * Find an account by its number.
	 *
	 * @param id
	 *            the account id
	 * @return the account
	 */
	Account findById(Long id);

	/**
	 * Load an account by its credit card.
	 * @param creditCardNumber the credit card number
	 * @return the account object
	 */
	Account findByCreditCard(String creditCardNumber);

	Account insert(Account account);

	void update(Account account);

	/**
	 * Updates the 'savings' of each account beneficiary. The new savings balance contains the amount distributed for a
	 * contribution made during a reward transaction.
	 * <p>
	 * Note: use of an object-relational mapper (ORM) with support for transparent-persistence like Hibernate (or the
	 * new Java Persistence API (JPA)) would remove the need for this explicit update operation as the ORM would take
	 * care of applying relational updates to a modified Account entity automatically.
	 * @param account the account whose beneficiary savings have changed
	 */
	void updateBeneficiaries(Account account);
}