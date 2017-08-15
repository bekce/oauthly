package com.sebworks.oauthly;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class Oauth2ServerDemoApplication {

	/**
	 * Registers the {@link OAuthFilter} to protect resources. You can tweak the regexp for your needs.
	 * Examples:
	 * ^/(?:static|app|api)(?:|(?:[\?#/].*))$ : protects endpoints starting with /api, /app or /static
	 * ^/((?!oauth(?:[\?#/].*)).)*$ : whitelist pattern that protects endpoints EXCEPT starting with /oauth[?#/].
	 * ^/((?!(?:oauth|public)(?:[\?#/].*)).)*$ : whitelist pattern that protects endpoints EXCEPT starting with /oauth[?#/] OR /public[?#/].
	 * You can use a regexp visualiser tool like https://www.debuggex.com to test the regexp (java uses python style regexp)
	 */
	@Bean
	protected FilterRegistrationBean oauthFilter() {
		FilterRegistrationBean bean = new FilterRegistrationBean(new OAuthFilter());
		bean.addUrlPatterns("/*");
		bean.addInitParameter(OAuthFilter.PROTECTED_URL_PATTERN, "^/(?:static|app|api)(?:|(?:[\\?#/].*))$" );
		bean.setName("Authentication Filter");
		return bean;
	}

	public static void main(String[] args) {
		SpringApplication.run(Oauth2ServerDemoApplication.class, args);
	}
}
