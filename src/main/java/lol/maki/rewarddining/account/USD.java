package lol.maki.rewarddining.account;

import java.math.BigDecimal;

import javax.money.MonetaryAmount;

import org.javamoney.moneta.Money;

public class USD {
	public static MonetaryAmount valueOf(String number) {
		return Money.of(new BigDecimal(number), "USD");
	}
}
