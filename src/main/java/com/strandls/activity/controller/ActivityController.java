/**
 * 
 */
package com.strandls.activity.controller;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.pac4j.core.profile.CommonProfile;

import com.google.inject.Inject;
import com.strandls.activity.ActivityEnums;
import com.strandls.activity.ApiConstants;
import com.strandls.activity.pojo.Activity;
import com.strandls.activity.pojo.ActivityLoggingData;
import com.strandls.activity.pojo.ActivityResult;
import com.strandls.activity.pojo.CommentLoggingData;
import com.strandls.activity.service.ActivityService;
import com.strandls.activity.service.impl.MigrateThread;
import com.strandls.authentication_utility.filter.ValidateUser;
import com.strandls.authentication_utility.util.AuthUtil;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

/**
 * @author Abhishek Rudra
 *
 */

@Api("Activity Serivce")
@Path(ApiConstants.V1 + ApiConstants.SERVICE)
public class ActivityController {

	@Inject
	private ActivityService service;

	@GET
	@Path(ApiConstants.PING)
	@Produces(MediaType.TEXT_PLAIN)
	public Response ping() {
		return Response.status(Status.OK).entity("PONG").build();
	}

	@GET
	@Path(ApiConstants.IBP + "/{objectType}/{objectId}")
	@Consumes(MediaType.TEXT_PLAIN)
	@Produces(MediaType.APPLICATION_JSON)

	@ApiOperation(value = "Find activity by ID for IBP", notes = "Returns activity details", response = ActivityResult.class)
	@ApiResponses(value = { @ApiResponse(code = 404, message = "Traits not found", response = String.class) })

	public Response getIbpActivity(@PathParam("objectType") String objectType, @PathParam("objectId") String objectId,
			@DefaultValue(value = "0") @QueryParam("offset") String offset,
			@DefaultValue(value = "10") @QueryParam("limit") String limit) {
		try {
			if (objectType.equalsIgnoreCase("observation"))
				objectType = ActivityEnums.observation.getValue();
			Long id = Long.parseLong(objectId);
			ActivityResult activityResult = service.fetchActivityIbp(objectType, id, offset, limit);
			return Response.status(Status.OK).entity(activityResult).build();

		} catch (Exception e) {
			return Response.status(Status.BAD_REQUEST).build();
		}
	}

	@POST
	@Path(ApiConstants.LOG)
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)

	@ValidateUser

	@ApiOperation(value = "Logs activity", notes = "Return the activity", response = Activity.class)
	@ApiResponses(value = { @ApiResponse(code = 400, message = "Unable to post the data", response = String.class) })

	public Response logActivity(@Context HttpServletRequest request,
			@ApiParam(name = "activityLogging") ActivityLoggingData activityLogging) {

		try {

			CommonProfile profile = AuthUtil.getProfileFromRequest(request);
			Long userId = Long.parseLong(profile.getId());
			Activity result = service.logActivities(userId, activityLogging);
			return Response.status(Status.OK).entity(result).build();

		} catch (Exception e) {
			return Response.status(Status.BAD_REQUEST).entity(e.getMessage()).build();
		}

	}

	@POST
	@Path(ApiConstants.ADD + ApiConstants.COMMENT)
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)

	@ValidateUser

	@ApiOperation(value = "Adds a comment", notes = "Return the current activity", response = Activity.class)
	@ApiResponses(value = { @ApiResponse(code = 400, message = "Unable to log a comment", response = String.class) })

	public Response addComment(@Context HttpServletRequest request,
			@ApiParam(name = "commentData") CommentLoggingData commentData) {
		try {

			CommonProfile profile = AuthUtil.getProfileFromRequest(request);
			Long userId = Long.parseLong(profile.getId());

			Activity result = service.addComment(userId, commentData);
			return Response.status(Status.OK).entity(result).build();
		} catch (Exception e) {
			return Response.status(Status.BAD_REQUEST).entity(e.getMessage()).build();
		}
	}

	@POST
	@Path(ApiConstants.MIGRATE)
	@Produces(MediaType.TEXT_PLAIN)

	public Response migrateData() {
		try {
			Thread thread = new Thread(new MigrateThread());
			thread.start();
			return Response.status(Status.OK).entity("Migration Started").build();
		} catch (Exception e) {
			return Response.status(Status.BAD_REQUEST).entity(e.getMessage()).build();
		}
	}

}
