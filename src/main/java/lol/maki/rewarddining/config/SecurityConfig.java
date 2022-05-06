package lol.maki.rewarddining.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.web.filter.CommonsRequestLoggingFilter;

@Configuration
public class SecurityConfig {
	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		http
				.addFilterBefore(commonsRequestLoggingFilter(), CsrfFilter.class)
				.formLogin()
				.loginPage("/login")
				.loginProcessingUrl("/login")
				.permitAll()
				.and()
				.exceptionHandling()
				.accessDeniedPage("/denied")
				.and()
				.authorizeRequests()
				.mvcMatchers("/edit*").hasRole("EDITOR")
				.mvcMatchers("/accountDetails", "/accountList").hasAnyRole("VIEWER", "EDITOR")
				.mvcMatchers("/accounts/**").permitAll() // TODO
				.mvcMatchers("/actuator/**").permitAll() // TODO
				.mvcMatchers("/**").authenticated()
				.and()
				.csrf().ignoringAntMatchers("/accounts/**") // TODO
				.and()
				.logout()
				.permitAll()
				.logoutSuccessUrl("/");
		return http.build();
	}

	@Bean
	public WebSecurityCustomizer webSecurityCustomizer() {
		return (web) -> web.debug(false).ignoring().mvcMatchers("/resources/**");
	}

	@Bean
	public InMemoryUserDetailsManager userDetailsService() {
		return new InMemoryUserDetailsManager(
				User.withUsername("vince")
						.password("{noop}vince")
						.roles("VIEWER")
						.build(),
				User.withUsername("edith")
						.password("{noop}edith")
						.roles("EDITOR")
						.build());
	}

	CommonsRequestLoggingFilter commonsRequestLoggingFilter() {
		final CommonsRequestLoggingFilter loggingFilter = new CommonsRequestLoggingFilter();
		loggingFilter.setIncludeHeaders(false);
		loggingFilter.setIncludePayload(true);
		loggingFilter.setIncludeQueryString(true);
		loggingFilter.setIncludeClientInfo(true);
		return loggingFilter;
	}
}
