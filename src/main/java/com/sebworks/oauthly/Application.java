package com.sebworks.oauthly;

import com.sebworks.oauthly.filter.AuthorizationServerFilter;
import com.sebworks.oauthly.filter.ResourceServerFilter;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class Application {

	/**
	 * Registers the {@link ResourceServerFilter} to protect resources. You can tweak the regexp for your needs.
	 * Examples:
	 * ^/(?:static|app|api)(?:|(?:[\?#/].*))$ : protects endpoints starting with /api, /app or /static
	 * ^/((?!oauth(?:[\?#/].*)).)*$ : whitelist pattern that protects endpoints EXCEPT starting with /oauth[?#/].
	 * ^/((?!(?:oauth|public)(?:[\?#/].*)).)*$ : whitelist pattern that protects endpoints EXCEPT starting with /oauth[?#/] OR /public[?#/].
	 * You can use a regexp visualiser tool like https://www.debuggex.com to test the regexp (java uses python style regexp)
	 */
	@Bean
	protected FilterRegistrationBean resourceServerFilter() {
		FilterRegistrationBean bean = new FilterRegistrationBean(new ResourceServerFilter());
		bean.addUrlPatterns("/*");
		bean.addInitParameter(ResourceServerFilter.PROTECTED_URL_PATTERN, "^/(?:api)(?:|(?:[\\?#/].*))$" );
		return bean;
	}

	@Bean
	protected FilterRegistrationBean authorizationServerFilter() {
		FilterRegistrationBean bean = new FilterRegistrationBean(new AuthorizationServerFilter());
		bean.addUrlPatterns("/*");
		bean.addInitParameter(AuthorizationServerFilter.PROTECTED_URL_PATTERN, "^/(?:oauth/authorize|profile|client|discourse)(?:|(?:[\\?#/].*))$" );
		return bean;
	}

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}
}
