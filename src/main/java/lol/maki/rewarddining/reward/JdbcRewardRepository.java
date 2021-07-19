package lol.maki.rewarddining.reward;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;

import lol.maki.rewarddining.account.AccountContribution;
import lol.maki.rewarddining.restaurant.Dining;
import lol.maki.rewarddining.util.FileLoader;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * JDBC implementation of a reward repository that records the result of a reward transaction by inserting a reward
 * confirmation record.
 */
@Repository
public class JdbcRewardRepository implements RewardRepository {

	private final JdbcTemplate jdbcTemplate;

	public JdbcRewardRepository(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	@Transactional
	public RewardConfirmation confirmReward(AccountContribution contribution, Dining dining) {
		final String sql = FileLoader.load("lol/maki/rewarddining/reward/JdbcRewardRepository/confirmReward.sql");
		final String confirmationNumber = nextConfirmationNumber();
		jdbcTemplate.update(sql, confirmationNumber, contribution.getAmount().getNumber().numberValue(BigDecimal.class),
				Date.valueOf(LocalDate.now()), contribution.getAccountNumber(), dining.getMerchantNumber(),
				Date.valueOf(dining.getDate()), dining.getAmount().getNumber().numberValue(BigDecimal.class));
		return new RewardConfirmation(confirmationNumber, contribution);
	}

	private String nextConfirmationNumber() {
		final String sql = FileLoader.load("lol/maki/rewarddining/reward/JdbcRewardRepository/nextConfirmationNumber.sql");
		return jdbcTemplate.queryForObject(sql, String.class);
	}
}