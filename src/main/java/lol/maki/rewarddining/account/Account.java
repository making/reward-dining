package lol.maki.rewarddining.account;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.money.MonetaryAmount;

import am.ik.yavi.arguments.Arguments1;
import am.ik.yavi.arguments.Arguments3Validator;
import am.ik.yavi.arguments.ArgumentsValidators;
import am.ik.yavi.arguments.LongValidator;
import am.ik.yavi.builder.StringValidatorBuilder;
import am.ik.yavi.builder.ValidatorBuilder;
import am.ik.yavi.core.ConstraintGroup;
import am.ik.yavi.core.ConstraintViolationsException;
import am.ik.yavi.core.Validated;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lol.maki.rewarddining.account.AccountContribution.Distribution;

/**
 * An account for a member of the reward network. An account has one or more beneficiaries whose allocations must add up
 * to 100%.
 *
 * An account can make contributions to its beneficiaries. Each contribution is distributed among the beneficiaries
 * based on an allocation.
 *
 * An entity. An aggregate.
 */
public class Account {
	private Long id;

	private final String number;

	private final String name;

	private final Set<Beneficiary> beneficiaries = Collections.newSetFromMap(new ConcurrentHashMap<>());

	static final ConstraintGroup UPDATE = ConstraintGroup.of("UPDATE");

	public static final Arguments3Validator<Long, String, String, Account> accountValidator = ArgumentsValidators.split(
					/* TODO: Update after 0.11.3 */
					new LongValidator<>(ValidatorBuilder
							.<Arguments1<Long>>of()
							.constraintOnGroup(UPDATE, b -> b._long(Arguments1::arg1, "id", c -> c.notNull()))
							._long(Arguments1::arg1, "id", c -> c.greaterThanOrEqual(0L))
							.build(), x -> x),
					StringValidatorBuilder
							.of("number", c -> c.notBlank().pattern("[0-9]{9}").message("\"{0}\" must be a 9 digit number"))
							.build(),
					StringValidatorBuilder
							.of("name", c -> c.notBlank().lessThanOrEqual(50))
							.build())
			.apply(Account::new);

	public static Validated<Account> forCreate(String number, String name) {
		return accountValidator.validate(null, number, name);
	}

	public static Validated<Account> forUpdate(Long id, String number, String name) {
		return accountValidator.validate(id, number, name, UPDATE);
	}

	/**
	 * Create a new account.
	 * @param id the account id
	 * @param number the account number
	 * @param name the name on the account
	 */
	public Account(Long id, String number, String name) {
		this.id = id;
		this.number = number;
		this.name = name;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	/**
	 * Returns the number used to uniquely identify this account.
	 */
	public String getNumber() {
		return number;
	}

	/**
	 * Returns the name on file for this account.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Add a single beneficiary with a 100% allocation percentage.
	 * @param beneficiaryName the name of the beneficiary (should be unique)
	 */
	public void addBeneficiary(String beneficiaryName) {
		addBeneficiary(beneficiaryName, Percentage.oneHundred());
	}

	/**
	 * Add a single beneficiary with the specified allocation percentage.
	 * @param beneficiaryName the name of the beneficiary (should be unique)
	 * @param allocationPercentage the beneficiary's allocation percentage within this account
	 */
	public void addBeneficiary(String beneficiaryName, Percentage allocationPercentage) {
		beneficiaries.add(new Beneficiary(beneficiaryName, allocationPercentage));
	}

	/**
	 * Removes a single beneficiary from this account.
	 *
	 * @param beneficiaryName
	 *            the name of the beneficiary (should be unique)
	 */
	public void removeBeneficiary(String beneficiaryName) {
		beneficiaries.remove(getBeneficiary(beneficiaryName));
	}

	/**
	 * Validation check that returns true only if the total beneficiary allocation adds up to 100%.
	 */
	@JsonIgnore
	public boolean isValid() {
		Percentage totalPercentage = Percentage.zero();
		for (Beneficiary b : beneficiaries) {
			try {
				totalPercentage = totalPercentage.add(b.getAllocationPercentage());
			}
			catch (ConstraintViolationsException e) {
				// total would have been over 100% - return invalid
				return false;
			}
		}
		if (totalPercentage.equals(Percentage.oneHundred())) {
			return true;
		}
		else {
			return false;
		}
	}

	/**
	 * Make a monetary contribution to this account. The contribution amount is distributed among the account's
	 * beneficiaries based on each beneficiary's allocation percentage.
	 * @param amount the total amount to contribute
	 * @return the contribution summary
	 */
	public AccountContribution makeContribution(MonetaryAmount amount) {
		if (!isValid()) {
			throw new IllegalStateException(
					"Cannot make contributions to this account: it has invalid beneficiary allocations");
		}
		Set<Distribution> distributions = distribute(amount);
		return new AccountContribution(getNumber(), amount, distributions);
	}

	/**
	 * Distribute the contribution amount among this account's beneficiaries.
	 * @param amount the total contribution amount
	 * @return the individual beneficiary distributions
	 */
	private Set<Distribution> distribute(MonetaryAmount amount) {
		Set<Distribution> distributions = new HashSet<>(beneficiaries.size());
		for (Beneficiary beneficiary : beneficiaries) {
			MonetaryAmount distributionAmount = amount.multiply(beneficiary.getAllocationPercentage().asBigDecimal());
			beneficiary.credit(distributionAmount);
			Distribution distribution = new Distribution(beneficiary.getName(), distributionAmount, beneficiary
					.getAllocationPercentage(), beneficiary.getSavings());
			distributions.add(distribution);
		}
		return distributions;
	}

	/**
	 * Returns the beneficiaries for this account. Callers should not attempt to hold on or modify the returned set.
	 * This method should only be used transitively; for example, called to facilitate account reporting.
	 * @return the beneficiaries of this account
	 */
	public Set<Beneficiary> getBeneficiaries() {
		return Collections.unmodifiableSet(beneficiaries);
	}

	/**
	 * Returns a single account beneficiary. Callers should not attempt to hold on or modify the returned object. This
	 * method should only be used transitively; for example, called to facilitate reporting or testing.
	 * @param name the name of the beneficiary e.g "Annabelle"
	 * @return the beneficiary object
	 */
	public Beneficiary getBeneficiary(String name) {
		for (Beneficiary b : beneficiaries) {
			if (b.getName().equals(name)) {
				return b;
			}
		}
		throw new IllegalArgumentException("No such beneficiary with name '" + name + "'");
	}

	/**
	 * Used to restore an allocated beneficiary. Should only be called by the repository responsible for reconstituting
	 * this account.
	 * @param beneficiary the beneficiary
	 */
	public void restoreBeneficiary(Beneficiary beneficiary) {
		beneficiaries.add(beneficiary);
	}

	public String toString() {
		return "id = " + id + ", number = '" + number + "', name = " + name + "', beneficiaries = " + beneficiaries;
	}
}