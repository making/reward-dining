package lol.maki.rewarddining.restaurant.infrastructure;

import lol.maki.rewarddining.account.Percentage;
import lol.maki.rewarddining.restaurant.AlwaysAvailable;
import lol.maki.rewarddining.restaurant.Restaurant;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.EmptyResultDataAccessException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

@JdbcTest(properties = { "logging.level.sql=TRACE" })
@Import(JdbcRestaurantRepository.class)
class JdbcRestaurantRepositoryTests {
	@Autowired
	JdbcRestaurantRepository repository;

	@Test
	void testFindRestaurantByMerchantNumber() {
		Restaurant restaurant = repository.findByMerchantNumber("1234567890");
		assertNotNull(restaurant, "the restaurant should never be null");
		assertEquals("1234567890", restaurant.getNumber(), "the merchant number is wrong");
		assertEquals("AppleBees", restaurant.getName(), "the name is wrong");
		assertEquals(Percentage.valueOf("8%"), restaurant.getBenefitPercentage(), "the benefitPercentage is wrong");
		Assertions.assertEquals(AlwaysAvailable.INSTANCE,
				restaurant.getBenefitAvailabilityPolicy(), "the benefit availability policy is wrong");
	}

	@Test
	void testFindRestaurantByBogusMerchantNumber() {
		try {
			repository.findByMerchantNumber("bogus");
			fail("Should have thrown EmptyResultDataAccessException for a 'bogus' merchant number");
		}
		catch (EmptyResultDataAccessException e) {
			// expected
		}
	}
}