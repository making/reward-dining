package lol.maki.rewarddining.config;

import org.zalando.jackson.datatype.money.MoneyModule;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JacksonConfig {
	@Bean
	public MoneyModule moneyModule() {
		return new MoneyModule().withMoney();
	}
}
