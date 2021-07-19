package lol.maki.rewarddining.config;

import java.security.Principal;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class MvcConfig implements WebMvcConfigurer {
	/**
	 * These views are so simple they do not need a controller:
	 */
	public void addViewControllers(ViewControllerRegistry registry) {
		registry.addViewController("/").setViewName("index");
		registry.addViewController("/index");
		registry.addViewController("/login");
		registry.addViewController("/denied");
		registry.addViewController("/hidden");
		registry.addViewController("/accounts/hidden").setViewName("hidden");
	}

	/**
	 * Add a custom HandlerInterceptor to ensure the Principal is always added to
	 * every request (if there is one).
	 */
	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(new HandlerInterceptor() {
			@Override
			public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
				Principal principal = request.getUserPrincipal();
				if (principal != null && modelAndView != null) {
					modelAndView.addObject("principal", principal);
				}
			}
		});
	}

}
