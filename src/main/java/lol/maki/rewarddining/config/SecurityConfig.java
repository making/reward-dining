package lol.maki.rewarddining.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.web.filter.CommonsRequestLoggingFilter;

@Configuration
public class SecurityConfig extends WebSecurityConfigurerAdapter {

	@Override
	public void configure(WebSecurity web) throws Exception {
		web.debug(false).ignoring().mvcMatchers("/resources/**");
	}


	@Override
	protected void configure(HttpSecurity http) throws Exception {
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
				.mvcMatchers("/**").authenticated()
				.and()
				.csrf().ignoringAntMatchers("/accounts/**") // TODO
				.and()
				.logout()
				.permitAll()
				.logoutSuccessUrl("/");
	}

	@Override
	protected void configure(AuthenticationManagerBuilder auth) throws Exception {
		auth
				.inMemoryAuthentication()
				.withUser("vince")
				.password("{noop}vince")
				.roles("VIEWER")
				.and()
				.withUser("edith")
				.password("{noop}edith")
				.roles("EDITOR");
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
