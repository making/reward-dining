package lol.maki.rewarddining.reward;

import java.util.Random;

import lol.maki.rewarddining.account.AccountContribution;
import lol.maki.rewarddining.restaurant.Dining;

/**
 * A dummy reward repository implementation.
 */
public class StubRewardRepository implements RewardRepository {

	public RewardConfirmation confirmReward(AccountContribution contribution, Dining dining) {
		return new RewardConfirmation(confirmationNumber(), contribution);
	}

	private String confirmationNumber() {
		return new Random().toString();
	}
}