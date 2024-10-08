package project.uam.controller;

import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import project.uam.entity.User;
import project.uam.service.ManagerServiceImpl;
import project.uam.service.UserServiceImpl;
import project.uam.service.serviceinterface.ManagerService;
import project.uam.util.ApiResponse;
import project.uam.util.ManagerRequest;


@Path("manager")
public class ManagerController {
	// Initial service injections of objects. 
	private final ManagerService managerService = new ManagerServiceImpl();
	private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);
	// ObjectMapper for json processing.
	private final ObjectMapper objectMapper = new ObjectMapper();
	
	// Add the normal user in the team of the manager.
	@POST
    @Path("addToTeam")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response addUserToTeam(ManagerRequest request) throws JsonProcessingException {
        try {
            managerService.addUserToTeam(request.getUserId(), request.getManagerUsername());
            return Response.ok(createSuccessResponse("User added to team successfully")).build();
        } catch (Exception e) {
        	String errorMessage = e.getMessage();
            if (errorMessage != null && errorMessage.contains("User is already in the team of manager")) {
                return Response.status(Response.Status.CONFLICT)
                               .entity(createErrorResponse(errorMessage))
                               .build();
            }
            log.error("Error in add to team method", e.getMessage());
            return Response.status(Response.Status.BAD_REQUEST)
                           .entity(createErrorResponse("Error adding user to team"))
                           .build();
        }
    }
	
	
	// Get all team members
	@POST
    @Path("teamMembers")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getTeamMembers(ManagerRequest request) throws JsonProcessingException {
        try {
            List<User> teamMembers = managerService.getTeamMembers(request.getUsername());
            String jsonResponse = objectMapper.writeValueAsString(teamMembers);
            return Response.ok(jsonResponse).build();
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(createErrorResponse("Error in getting all team memvbers")).build();
        }
    }
	
	//Get users of the oraganinsation having role user.
	@GET
	@Path("orgusers")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getAllUsers() throws JsonProcessingException {
		try {
			List<User> users = managerService.getAllOrgUsers();
			 String jsonResponse = objectMapper.writeValueAsString(users);	
			return Response.ok().entity(jsonResponse).build();
		}catch(Exception ex) {
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity(createErrorResponse("Error Fetching user")).build();
		}
	}
	
	//Remove the user from the team.
	@DELETE
    @Path("remove")
    @Produces(MediaType.APPLICATION_JSON)
    public Response removeUserFromTeam(ManagerRequest request) throws JsonProcessingException {
        try {
            managerService.removeUserFromTeam(request.getUserId());
            return Response.ok().entity(createSuccessResponse("User removed from team")).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                           .entity(createErrorResponse("Error occured while removig teamMember"))
                           .build();
        }
    }
 
	// Helper json converters.
	 private String createErrorResponse(String message) throws JsonProcessingException {
	        return objectMapper.writeValueAsString(new ApiResponse("ERROR", message));
	    }

	    private String createSuccessResponse(String message) throws JsonProcessingException {
	        return objectMapper.writeValueAsString(new ApiResponse("SUCCESS", message));
	    }
	
}
