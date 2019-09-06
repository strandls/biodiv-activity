/**
 * 
 */
package com.strandls.activity.controller;

import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.google.inject.Inject;
import com.strandls.activity.ApiConstants;
import com.strandls.activity.pojo.ActivityResult;
import com.strandls.activity.pojo.ShowActivity;
import com.strandls.activity.service.ActivityService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
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
	@Path("/{objectType}/{objectId}")
	@Consumes(MediaType.TEXT_PLAIN)
	@Produces(MediaType.APPLICATION_JSON)

	@ApiOperation(value = "Find activity by ID", notes = "Returns activity details", response = ShowActivity.class, responseContainer = "List")
	@ApiResponses(value = { @ApiResponse(code = 404, message = "Traits not found", response = String.class) })
	public Response getActivity(@PathParam("objectType") String objectType, @PathParam("objectId") String objectId) {

		try {
			Long id = Long.parseLong(objectId);

			List<ShowActivity> showActivities = service.fetchActivity(objectType, id);

			return Response.status(Status.OK).entity(showActivities).build();

		} catch (Exception e) {
			return Response.status(Status.BAD_REQUEST).build();
		}
	}

	@GET
	@Path(ApiConstants.IBP + "/{objectType}/{objectId}")
	@Consumes(MediaType.TEXT_PLAIN)
	@Produces(MediaType.APPLICATION_JSON)

	@ApiOperation(value = "Find activity by ID for IBP", notes = "Returns activity details", response = ActivityResult.class)
	@ApiResponses(value = { @ApiResponse(code = 404, message = "Traits not found", response = String.class) })

	public Response getIbpActivity(@PathParam("objectType") String objectType, @PathParam("objectId") String objectId) {
		try {
			Long id = Long.parseLong(objectId);
			ActivityResult activityResult = service.fetchActivityIbp(objectType, id);
			return Response.status(Status.OK).entity(activityResult).build();

		} catch (Exception e) {
			return Response.status(Status.BAD_REQUEST).build();
		}
	}

}
