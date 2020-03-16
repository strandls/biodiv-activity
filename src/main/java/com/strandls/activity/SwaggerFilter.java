/**
 * 
 */
package com.strandls.activity;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.HttpHeaders;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.strandls.observation.controller.ObservationServiceApi;
import com.strandls.observation.controller.RecommendationServicesApi;
import com.strandls.traits.controller.TraitsServiceApi;
import com.strandls.user.controller.UserServiceApi;
import com.strandls.userGroup.controller.UserGroupSerivceApi;
import com.strandls.utility.controller.UtilityServiceApi;

/**
 * @author Abhishek Rudra
 *
 */
@Singleton
public class SwaggerFilter implements Filter {

	@Inject
	public TraitsServiceApi traitService;

	@Inject
	public UserGroupSerivceApi userGroupService;

	@Inject
	public UtilityServiceApi utilityService;

	@Inject
	public UserServiceApi userService;

	@Inject
	private RecommendationServicesApi recoService;

	@Inject
	private ObservationServiceApi observationService;

	/**
	 * 
	 */
	public SwaggerFilter() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void destroy() {
		// TODO Auto-generated method stub
	}

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		// TODO Auto-generated method stub

	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		HttpServletRequest request2 = (HttpServletRequest) request;

		String header = request2.getHeader(HttpHeaders.AUTHORIZATION);

		traitService.getApiClient().addDefaultHeader(HttpHeaders.AUTHORIZATION, header);
		userGroupService.getApiClient().addDefaultHeader(HttpHeaders.AUTHORIZATION, header);
		utilityService.getApiClient().addDefaultHeader(HttpHeaders.AUTHORIZATION, header);
		userService.getApiClient().addDefaultHeader(HttpHeaders.AUTHORIZATION, header);
		recoService.getApiClient().addDefaultHeader(HttpHeaders.AUTHORIZATION, header);
		observationService.getApiClient().addDefaultHeader(HttpHeaders.AUTHORIZATION, header);

		chain.doFilter(request2, response);
	}

}
