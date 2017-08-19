package com.sebworks.oauthly.filter;

import com.sebworks.oauthly.common.TokenStatus;
import com.sebworks.oauthly.controller.OAuthController;
import com.sebworks.oauthly.entity.Grant;
import org.javatuples.Pair;
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
public class ResourceServerFilter implements Filter{

	public static final String PROTECTED_URL_PATTERN = "protectedURLPattern";
	private static final Logger logger = LoggerFactory.getLogger(ResourceServerFilter.class);
	
	private Pattern pattern;
	private OAuthController controller;

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		WebApplicationContext context = WebApplicationContextUtils.getWebApplicationContext(filterConfig.getServletContext());
		if(context == null){
			throw new ServletException("Spring context not found");
		}
		controller = context.getBean(OAuthController.class);

		String protectedURLPattern = filterConfig.getInitParameter(PROTECTED_URL_PATTERN);
		if(protectedURLPattern == null){
			throw new ServletException("ResourceServerFilter requires a "+PROTECTED_URL_PATTERN+" init parameter to work.");
		}
		try {
			pattern = Pattern.compile(protectedURLPattern);
		} catch (Exception e) {
			throw new ServletException(e.getMessage(), e);
		}
		logger.info("Initialized ResourceServerFilter");
	}

	@Override
	public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException,
			ServletException {

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
		if (authorizationHeader != null && StringUtils.startsWithIgnoreCase(authorizationHeader, "Bearer ")) {
			String token = authorizationHeader.substring("Bearer ".length()).trim();
			Pair<Grant, TokenStatus> tokenStatus = controller.getTokenStatus(token);
			if(tokenStatus.getValue1() == TokenStatus.VALID_ACCESS){
				request.setAttribute("grant", tokenStatus.getValue0());
			}
		} else { //check for access_token query param (/url?access_token=xyz)
			String token = request.getParameter("access_token");
			if (token != null && !token.isEmpty()) {
				Pair<Grant, TokenStatus> tokenStatus = controller.getTokenStatus(token);
				if(tokenStatus.getValue1() == TokenStatus.VALID_ACCESS){
					request.setAttribute("grant", tokenStatus.getValue0());
				}
			}
		}

		// check if user is currently authenticated in the request scope
		if(request.getAttribute("grant") != null){
			chain.doFilter(request, response);
			return;
		}

		response.sendError(401, "Invalid or missing bearer token");
	}

	@Override
	public void destroy() {
	}
}
