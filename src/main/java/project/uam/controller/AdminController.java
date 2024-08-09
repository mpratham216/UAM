package project.uam.controller;


import java.util.Base64;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import project.uam.entity.FileUploadRequest;
import project.uam.entity.User;
import project.uam.service.AdminServiceImpl;
import project.uam.service.UserServiceImpl;
import project.uam.service.serviceinterface.AdminService;
import project.uam.service.serviceinterface.UserService;
import project.uam.util.ApiResponse;

@Path("admin")
public class AdminController {
	private final AdminService adminService = new AdminServiceImpl();
	private final UserService userService = new UserServiceImpl();
	private final ObjectMapper objectMapper = new ObjectMapper();
	
	private static final Logger log = LoggerFactory.getLogger(AdminController.class);
	@GET
	@Path("users")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getAllUsers() throws JsonProcessingException {
		try {
			List<User> users = adminService.getAllUsers();
			String jsonResponse = objectMapper.writeValueAsString(users);
			return Response.ok().entity(jsonResponse).build();
		}catch(Exception ex) {
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity(createErrorResponse("Error Fetching user")).build();
		}
	}
	
	@POST
    @Path("changerole")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response changeUserRole(User user) throws JsonProcessingException {
        try {
            adminService.changeRole(user.getId(), user.getRole());
            return Response.ok(createSuccessResponse("Role updated successfully")).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(createErrorResponse("Error updating role")).build();
        }
    }
	
	@PUT
	@Path("updateuser")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateUser(User user) throws JsonProcessingException {
        try {
            userService.updateUser(user);
            
            return Response.ok(createSuccessResponse("User updated Successfully!")).build();
        } catch (Exception e) {
        	log.error("Error Updating the user", e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(createErrorResponse("Error updating the user")).build();
        }
    }
	
	
	
	@DELETE
    @Path("deleteuser")
    @Produces(MediaType.APPLICATION_JSON)
    public Response removeUser(User user) throws JsonProcessingException {
        try {
            userService.deleteUser(user.getId());
            return Response.ok().entity(createSuccessResponse("User removed from organisation")).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                           .entity(createErrorResponse("Error occured while removig user"))
                           .build();
        }
    }
	
	@POST
	@Path("upload")
	@Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
	public Response uploadCsv(FileUploadRequest request) {
	    try {
	        // Process the uploaded CSV file
	    	byte[] decodedBytes = Base64.getDecoder().decode(request.getBase64Content());
            InputStream uploadedInputStream = new ByteArrayInputStream(decodedBytes);
            
	        adminService.processCsvFile(uploadedInputStream);
	        return Response.ok("File uploaded successfully").build();
	    } catch (Exception e) {
	        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Error processing file").build();
	    }
	}

	
	 private String createErrorResponse(String message) throws JsonProcessingException {
	        return objectMapper.writeValueAsString(new ApiResponse("ERROR", message));
	    }

	    private String createSuccessResponse(String message) throws JsonProcessingException {
	        return objectMapper.writeValueAsString(new ApiResponse("SUCCESS", message));
	    }
	
}
