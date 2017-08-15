package com.sebworks.oauthly;

import com.sebworks.oauthly.controller.OAuthAuthorizationController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.regex.Pattern;

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
	private WebApplicationContext context;

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		context = WebApplicationContextUtils.getWebApplicationContext(filterConfig.getServletContext());
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
		SessionData sessionData = context.getBean(SessionData.class);
		if (authorizationHeader != null && StringUtils.startsWithIgnoreCase(authorizationHeader, "Bearer ")) {
			String token = authorizationHeader.substring("Bearer ".length());
			sessionData.authenticated = controller.getTokenStatus(token) == TokenStatus.VALID_ACCESS;
		} else { //check for access_token query param (/url?access_token=xyz)
			String token = request.getParameter("access_token");
			if (token != null && !token.isEmpty()) {
				sessionData.authenticated = controller.getTokenStatus(token) == TokenStatus.VALID_ACCESS;
			}
		}

		// check if user is currently authenticated in the session
		if(sessionData.authenticated){
			chain.doFilter(request, response);
			return;
		}

		response.sendError(401, "Invalid or missing bearer token");
	}

	@Override
	public void destroy() {
	}
}
