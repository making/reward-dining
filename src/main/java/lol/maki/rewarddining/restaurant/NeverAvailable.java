package lol.maki.rewarddining.restaurant;

import lol.maki.rewarddining.account.Account;

/**
 * A benefit availabilty policy that returns false at all times.
 */
public class NeverAvailable implements BenefitAvailabilityPolicy {
	public static final BenefitAvailabilityPolicy INSTANCE = new NeverAvailable();

	public boolean isBenefitAvailableFor(Account account, Dining dining) {
		return false;
	}

	public String toString() {
		return "neverAvailable";
	}
}
