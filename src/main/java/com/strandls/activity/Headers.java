/**
 * 
 */
package com.strandls.activity;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.HttpHeaders;

import com.strandls.user.controller.UserServiceApi;

/**
 * @author Abhishek Rudra
 *
 */
public class Headers {

	public UserServiceApi addUserHeader(UserServiceApi userService, HttpServletRequest request) {
		String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
		userService.getApiClient().addDefaultHeader(HttpHeaders.AUTHORIZATION, authHeader);
		return userService;
	}

}
