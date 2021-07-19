package lol.maki.rewarddining.reward.infrastructure;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.util.Map;

import lol.maki.rewarddining.account.Account;
import lol.maki.rewarddining.account.AccountContribution;
import lol.maki.rewarddining.account.Percentage;
import lol.maki.rewarddining.account.USD;
import lol.maki.rewarddining.restaurant.Dining;
import lol.maki.rewarddining.reward.RewardConfirmation;
import org.javamoney.moneta.Money;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@JdbcTest(properties = { "logging.level.sql=TRACE" })
@Import(JdbcRewardRepository.class)
class JdbcRewardRepositoryTests {
	@Autowired
	JdbcRewardRepository repository;

	@Autowired
	JdbcTemplate jdbcTemplate;

	@Test
	public void testCreateReward() {
		Dining dining = Dining.createDining("100.00", "1234123412341234", "0123456789");

		Account account = new Account("1", "Keith and Keri Donald");
		account.setId(0L);
		account.addBeneficiary("Annabelle", Percentage.valueOf("50%"));
		account.addBeneficiary("Corgan", Percentage.valueOf("50%"));

		AccountContribution contribution = account.makeContribution(USD.valueOf("8.00"));
		RewardConfirmation confirmation = repository.confirmReward(contribution, dining);
		assertNotNull(confirmation, "confirmation should not be null");
		assertNotNull(confirmation.getConfirmationNumber(), "confirmation number should not be null");
		assertEquals(contribution, confirmation.getAccountContribution(), "wrong contribution object");
		verifyRewardInserted(confirmation, dining);
	}

	private void verifyRewardInserted(RewardConfirmation confirmation, Dining dining) {
		assertEquals(1, getRewardCount());
		String sql = "select * from t_reward where confirmation_number = ?";
		Map<String, Object> values = jdbcTemplate.queryForMap(sql, confirmation.getConfirmationNumber());
		verifyInsertedValues(confirmation, dining, values);
	}

	private void verifyInsertedValues(RewardConfirmation confirmation, Dining dining, Map<String, Object> values) {
		Date today = Date.valueOf(LocalDate.now());
		assertEquals(confirmation.getAccountContribution().getAmount(), Money.of((BigDecimal) values
				.get("REWARD_AMOUNT"), "USD"));
		assertEquals(today, values.get("REWARD_DATE"));
		assertEquals(confirmation.getAccountContribution().getAccountNumber(), values.get("ACCOUNT_NUMBER"));
		assertEquals(dining.getAmount(), Money.of((BigDecimal) values.get("DINING_AMOUNT"), "USD"));
		assertEquals(dining.getMerchantNumber(), values.get("DINING_MERCHANT_NUMBER"));
		assertEquals(today, values.get("DINING_DATE"));
	}

	private int getRewardCount() {
		String sql = "select count(*) from t_reward";
		return jdbcTemplate.queryForObject(sql, Integer.class);
	}
}