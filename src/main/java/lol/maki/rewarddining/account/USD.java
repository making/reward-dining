package lol.maki.rewarddining.account;

import java.math.BigDecimal;

import javax.money.MonetaryAmount;

import am.ik.yavi.builder.BigDecimalValidatorBuilder;
import am.ik.yavi.builder.StringValidatorBuilder;
import am.ik.yavi.core.ValueValidator;
import org.javamoney.moneta.Money;

public class USD {
	static ValueValidator<String, MonetaryAmount> validator = StringValidatorBuilder
			.of("number", c -> c.notBlank().isBigDecimal())
			.build(BigDecimal::new)
			.andThen(BigDecimalValidatorBuilder
					.of("number", c -> c.greaterThanOrEqual(BigDecimal.ZERO))
					.build(number -> Money.of(number, "USD")));

	public static MonetaryAmount valueOf(String number) {
		return validator.validated(number);
	}
}
