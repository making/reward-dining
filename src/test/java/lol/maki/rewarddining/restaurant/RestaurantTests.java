package lol.maki.rewarddining.restaurant;

import javax.money.MonetaryAmount;

import lol.maki.rewarddining.account.Account;
import lol.maki.rewarddining.account.Percentage;
import lol.maki.rewarddining.account.USD;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit tests for exercising the behavior of the Restaurant aggregate entity. A restaurant calculates a benefit to award
 * to an account for dining based on an availability policy and benefit percentage.
 */
public class RestaurantTests {

	private Restaurant restaurant;

	private Account account;

	private Dining dining;

	@BeforeEach
	void setUp() {
		// configure the restaurant, the object being tested
		restaurant = new Restaurant("1234567890", "AppleBee's");
		restaurant.setBenefitPercentage(Percentage.valueOf("8%"));
		restaurant.setBenefitAvailabilityPolicy(new StubBenefitAvailabilityPolicy(true));
		// configure supporting objects needed by the restaurant
		account = new Account(null, "123456789", "Keith and Keri Donald");
		account.addBeneficiary("Annabelle");
		dining = Dining.createDining("100.00", "1234123412341234", "1234567890");
	}

	@Test
	void testCalcuateBenefitFor() {
		MonetaryAmount benefit = restaurant.calculateBenefitFor(account, dining);
		// assert 8.00 eligible for reward
		assertEquals(USD.valueOf("8.00"), benefit);
	}

	@Test
	void testNoBenefitAvailable() {
		// configure stub that always returns false
		restaurant.setBenefitAvailabilityPolicy(new StubBenefitAvailabilityPolicy(false));
		MonetaryAmount benefit = restaurant.calculateBenefitFor(account, dining);
		// assert zero eligible for reward
		assertEquals(USD.valueOf("0.00"), benefit);
	}

	/**
	 * A simple "dummy" benefit availability policy containing a single flag used to determine if benefit is available.
	 * Only useful for testing--a real availability policy might consider many factors such as the day of week of the
	 * dining, or the account's reward history for the current month.
	 */
	private static class StubBenefitAvailabilityPolicy implements BenefitAvailabilityPolicy {

		private boolean isBenefitAvailable;

		public StubBenefitAvailabilityPolicy(boolean isBenefitAvailable) {
			this.isBenefitAvailable = isBenefitAvailable;
		}

		public boolean isBenefitAvailableFor(Account account, Dining dining) {
			return isBenefitAvailable;
		}
	}
}