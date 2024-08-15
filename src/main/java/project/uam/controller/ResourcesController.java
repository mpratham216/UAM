package project.uam.controller;

import java.util.List;
import java.sql.SQLException;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import project.uam.entity.Request;
import project.uam.entity.Resources;
import project.uam.service.ResourceServiceImpl;
import project.uam.service.serviceinterface.ResourceService;
import project.uam.util.ApiResponse;

@Path("resources")
public class ResourcesController {
	private final ResourceService resourceService = new ResourceServiceImpl();
	private final ObjectMapper objectMapper = new ObjectMapper();
	private static final Logger log = LoggerFactory.getLogger(ResourcesController.class);
	
	
	@POST
	@Path("addresource")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response addResource(Resources resource) throws JsonProcessingException {
	    try {
	        resourceService.addResource(resource);
	        return Response.ok(createSuccessResponse("Resource added successfully")).build();
	    } catch (SQLException e) {
	        if (e.getMessage().contains("Resource with this name already exists")) {
	            return Response.status(Status.CONFLICT)
	                           .entity(createErrorResponse("A resource with this name already exists."))
	                           .build();
	        } else {
	            return Response.status(Status.INTERNAL_SERVER_ERROR)
	                           .entity(createErrorResponse("Error adding resource"))
	                           .build();
	        }
	    } catch (Exception e) {
	        log.error("Unexpected error occurred while adding resource", e);
	        return Response.status(Status.INTERNAL_SERVER_ERROR)
	                       .entity(createErrorResponse("Unexpected error occurred while adding resource"))
	                       .build();
	    }
	}

	
	@GET
	@Path("allresource")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getAllResources(@QueryParam("username") String username) throws JsonProcessingException {
		try {
			List<Resources> resources = resourceService.getAllResources(username);	
			String jsonResponse = objectMapper.writeValueAsString(resources);
			return Response.ok(jsonResponse).build();
		}catch(Exception e) {
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity(createErrorResponse("Error getting all resources")).build();
		}
	}
	
	@GET
	@Path("myresources")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getResourcesByUser(@QueryParam("username") String username) throws JsonProcessingException {
	    try {
	        List<Resources> resources = resourceService.getMyResources(username);
	        if (resources.isEmpty()) {
	        	log.info("resource is empty");
	            return Response.status(Response.Status.NOT_FOUND).entity(createErrorResponse("No resources found for user")).build();
	        }
	        
	        String jsonResponse = objectMapper.writeValueAsString(resources);
	        
	        return Response.ok(jsonResponse).build();
	    } catch (Exception e) {
	        log.error("Error in fetching myresources", e.getMessage());
	        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(createErrorResponse("Error getting users resources")).build();
	    }
	}
	
	@POST
    @Path("request")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
	public Response requestResource(Request request) throws JsonProcessingException {
	    try {
	        resourceService.requestResource(request);
	        return Response.ok(createSuccessResponse("Resource request submitted successfully.")).build();
	    } catch (SQLException e) {
	        if (e.getMessage().contains("Resource request already exists")) {
	            return Response.status(Status.CONFLICT)
	                           .entity(createErrorResponse("Resource request already exists for this user."))
	                           .build();
	        } else {
	            log.error("Database error occurred while submitting resource request", e);
	            return Response.status(Status.INTERNAL_SERVER_ERROR)
	                           .entity(createErrorResponse("Error submitting request due to a database issue."))
	                           .build();
	        }
	    } catch (Exception e) {
	        log.error("Unexpected error occurred while submitting resource request", e);
	        return Response.status(Status.INTERNAL_SERVER_ERROR)
	                       .entity(createErrorResponse("Unexpected error submitting request."))
	                       .build();
	    }
	}


    @GET
    @Path("allrequests")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllRequests() throws JsonProcessingException {
        try {
            List<Request> requests = resourceService.getAllRequests();
            String jsonResponse = objectMapper.writeValueAsString(requests);
            return Response.ok(jsonResponse).build();
        } catch (Exception e) {
        	log.error("Error in getting all requests", e.getMessage());
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(createErrorResponse("Error fetching all requests")).build();
        }
    }

    @POST
    @Path("approve/{requestId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response approveRequest(@PathParam("requestId") int requestId) throws JsonProcessingException {
        try {
            resourceService.approveRequest(requestId);
            return Response.ok(createSuccessResponse("Request approved")).build();
        } catch (Exception e) {
        	log.error("Request Approval failed", e.getMessage());
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(createErrorResponse("Error approving request")).build();
        }
    }
    
    
    @POST
    @Path("reject/{requestId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response rejectRequest(@PathParam("requestId") int requestId) throws JsonProcessingException {
        try {
            resourceService.rejectRequest(requestId);
            return Response.ok(createSuccessResponse("Request Rejected")).build();
        } catch (Exception e) {
        	log.error("Request Rejection failed", e.getMessage());
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(createErrorResponse("Error approving request")).build();
        }
    }
    
    
    @GET
    @Path("myrequests")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getRequestsByUser(@QueryParam("username") String username) throws JsonProcessingException {
        try {
            List<Request> requests = resourceService.getRequestsByUser(username);
            String jsonResponse = objectMapper.writeValueAsString(requests);
            return Response.ok(jsonResponse).build();
        } catch (Exception e) {
            log.error("Error in fetching user requests", e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                           .entity(createErrorResponse("Error getting user requests"))
                           .build();
        }
    }

    
    
    private String createErrorResponse(String message) throws JsonProcessingException {
        return objectMapper.writeValueAsString(new ApiResponse("ERROR", message));
    }

    private String createSuccessResponse(String message) throws JsonProcessingException {
        return objectMapper.writeValueAsString(new ApiResponse("SUCCESS", message));
    }
	

	
}
