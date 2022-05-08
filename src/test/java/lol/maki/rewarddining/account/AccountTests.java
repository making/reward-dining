package lol.maki.rewarddining.account;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AccountTests {
	private Account account = new Account(null, "1", "Keith and Keri Donald");

	@Test
	void accountIsValid() {
		// setup account with a valid set of beneficiaries to prepare for testing
		account.addBeneficiary("Annabelle", Percentage.valueOf("50%"));
		account.addBeneficiary("Corgan", Percentage.valueOf("50%"));
		assertTrue(account.isValid());
	}

	@Test
	void accountIsInvalidWithNoBeneficiaries() {
		assertFalse(account.isValid());
	}

	@Test
	void accountIsInvalidWhenBeneficiaryAllocationsAreOver100() {
		account.addBeneficiary("Annabelle", Percentage.valueOf("50%"));
		account.addBeneficiary("Corgan", Percentage.valueOf("100%"));
		assertFalse(account.isValid());
	}

	@Test
	void accountIsInvalidWhenBeneficiaryAllocationsAreUnder100() {
		account.addBeneficiary("Annabelle", Percentage.valueOf("50%"));
		account.addBeneficiary("Corgan", Percentage.valueOf("25%"));
		assertFalse(account.isValid());
	}

	@Test
	void makeContribution() {
		account.addBeneficiary("Annabelle", Percentage.valueOf("50%"));
		account.addBeneficiary("Corgan", Percentage.valueOf("50%"));
		AccountContribution contribution = account.makeContribution(USD.valueOf("100.00"));
		assertEquals(contribution.getAmount(), USD.valueOf("100.00"));
		assertEquals(USD.valueOf("50.00"), contribution.getDistribution("Annabelle").getAmount());
		assertEquals(USD.valueOf("50.00"), contribution.getDistribution("Corgan").getAmount());
	}
}