package com.sebworks;

import java.io.IOException;
import java.util.regex.Pattern;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * This filter is responsible for protecting resources. It sends 401 if no token found in the request.
 *
 * @author Selim Eren Bekçe
 *
 */
public class OAuthFilter implements Filter{

	public static final String PROTECTED_URL_PATTERN = "protectedURLPattern";
	private static final Logger logger = LoggerFactory.getLogger(OAuthFilter.class);
	
	private Pattern pattern;
	private boolean oauthFilterEnabled;
	private OAuthAuthorizationController controller;
	
	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		WebApplicationContext context = WebApplicationContextUtils.getWebApplicationContext(filterConfig.getServletContext());
		if(context == null){
			throw new ServletException("Spring context not found");
		}
		oauthFilterEnabled = context.getEnvironment().getRequiredProperty("oauth.server.filter.enabled", Boolean.class);
		controller = context.getBean(OAuthAuthorizationController.class);

		String protectedURLPattern = filterConfig.getInitParameter(PROTECTED_URL_PATTERN);
		if(protectedURLPattern == null){
			throw new ServletException("OAuthFilter requires a "+PROTECTED_URL_PATTERN+" init parameter to work.");
		}
		try {
			pattern = Pattern.compile(protectedURLPattern);
		} catch (Exception e) {
			throw new ServletException(e.getMessage(), e);
		}
		logger.info("Initialized OAuthFilter");
	}

	@Override
	public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException,
			ServletException {

		if (!oauthFilterEnabled) {
			chain.doFilter(req, res);
			return;
		}

		HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;

		// Protected pattern does not match: allow
		String requestUri = request.getRequestURI().replace(request.getContextPath(), "");
		if (!pattern.matcher(requestUri).matches()) {
			chain.doFilter(request, response);
			return;
		}

		// Check for Authorization header
		String authorizationHeader = request.getHeader("Authorization");
		if(authorizationHeader != null && StringUtils.startsWithIgnoreCase(authorizationHeader, "Bearer ")){
			String token = authorizationHeader.substring("Bearer ".length());
			if(controller.getTokenStatus(token) == TokenStatus.VALID_ACCESS){
				chain.doFilter(request, response);
				return;
			}
		} else { //check for access_token query param (/url?access_token=xyz)
			String token = request.getParameter("access_token");
			if(controller.getTokenStatus(token) == TokenStatus.VALID_ACCESS){
				chain.doFilter(request, response);
				return;
			}
		}

		response.sendError(401, "Invalid or missing bearer token");
	}

	@Override
	public void destroy() {
	}
}
