package project.uam.controller;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import project.uam.entity.User;
import project.uam.service.UserServiceImpl;
import project.uam.service.serviceinterface.UserService;
import project.uam.util.ApiResponse;
import project.uam.util.PasswordUtil;
import project.uam.util.RegisterResponse;
import project.uam.util.UpdatePasswordRequest;

import org.glassfish.jersey.client.RequestProcessingInitializationStage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("users")
public class UserController {
	private static final Logger log = LoggerFactory.getLogger(UserController.class);
	private final UserService userService = new UserServiceImpl();
	private final ObjectMapper objectMapper = new ObjectMapper();
	
	@POST
	@Path("register")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response registerUser(User user) throws JsonProcessingException {
		try {
			String username = userService.generateUniqueUsername(user.getFirstname(), user.getLastname());
	        user.setUsername(username);
	        
//			if(userService.isUsernameTaken(user.getUsername())) {
//				return Response.status(Response.Status.CONFLICT).entity("User Already Exist!").build();
//			}
	        
//	        if (!user.getPassword().equals(user.getConfirmPassword())) {
//                return Response.status(Response.Status.BAD_REQUEST).entity("Passwords do not match").build();
//            }
//	        
			if(!PasswordUtil.isPasswordComplex(user.getPassword())) {
				return Response.status(Response.Status.BAD_REQUEST).entity("Password must be 8 characters and must have One Digit, One LowerCase Letter , One UpperCase Letter and One Special character(@#$%^&+=).").build();
			}
			user.setPassword(PasswordUtil.hashPassword(user.getPassword()));
            user.setRole(userService.isFirstUser() ? "admin" : "user");
            userService.registerUser(user);
            return Response.ok().entity(createRegistrationResponse(("User registered Successfully!"), user.getUsername())).build();
		}catch(SQLException e) {
			log.error("Error in registering user", e.getMessage());
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(createErrorResponse("Error Occurred in registering user")).build();
		}catch (IllegalArgumentException e) {
            log.error("Invalid password", e.getMessage());
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        }
	}
	
	@POST
	@Path("login")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response loginUser(User user) throws JsonProcessingException {
		try {
			User existingUser = userService.getUserByUsername(user.getUsername());
	        
			if(existingUser == null || !PasswordUtil.checkPassword(user.getPassword(), existingUser.getPassword())) {
				return Response.status(Response.Status.UNAUTHORIZED).entity(createErrorResponse("Invalid Credentials")).build();
			}
			
			// Check if the password needs to be updated
	        boolean isPasswordDefault = user.getPassword().equals("Password@123$");
			
			Map<String, Object> response  = new HashMap<>();
			if (isPasswordDefault) {
	            response.put("status", "PASSWORD_UPDATE_REQUIRED");
	            response.put("message", "Password update required");
	        } else {
	            response.put("status", "SUCCESS");
	            response.put("message", "User Authorized");
	            response.put("role", existingUser.getRole());
	        }

	        
	        String jsonResponse = objectMapper.writeValueAsString(response);
			return Response.ok().entity(jsonResponse).build();
		}catch(SQLException e) {
			log.error("Error Loging users", e.getMessage());
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(createErrorResponse("Error Occured in loggin user")).build();
		}
	}
	
	@POST
	@Path("forgotPassword")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response forgotPassword(User user) throws JsonProcessingException {
		try {
			User existingUser = userService.getUserByEmail(user.getEmail());
			if(existingUser == null) {
				return Response.status(Response.Status.NOT_FOUND).entity(createErrorResponse("Email not found")).build();
			}
			
//			if (!user.getPassword().equals(user.getConfirmPassword())) {
//                return Response.status(Response.Status.BAD_REQUEST).entity("Passwords do not match").build();
//            }
			
			existingUser.setPassword(PasswordUtil.hashPassword(user.getPassword()));
			userService.updateUser(existingUser);
			return Response.ok().entity(createSuccessResponse("Successfully Reseted the Password")).build();
		}catch(SQLException e) {
			log.error("Error Handling forgot password", e.getMessage());
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(createErrorResponse("Error occured in forgot password")).build();
		}
	}
	
	@POST
	@Path("updatepassword")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response updatePassword(UpdatePasswordRequest request) throws JsonProcessingException {
		try {
	        // Fetch the user by username
	        User existingUser = userService.getUserByUsername(request.getUsername());
	        if (existingUser == null) {
	            return Response.status(Response.Status.NOT_FOUND).entity(createErrorResponse("User not found")).build();
	        }

	        // Verify the current password
	        boolean isPasswordValid = PasswordUtil.checkPassword(request.getCurrentpassword(), existingUser.getPassword());
	        if (!isPasswordValid) {
	            return Response.status(Response.Status.UNAUTHORIZED).entity(createErrorResponse("Current password is incorrect")).build();
	        }

	        // Update to the new password
	        existingUser.setPassword(PasswordUtil.hashPassword(request.getNewPassword()));
	        userService.updateUser(existingUser);

	        return Response.ok().entity(createSuccessResponse("Password updated successfully")).build();
	    } catch (SQLException e) {
	        log.error("Error updating password", e.getMessage());
	        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(createErrorResponse("An error occurred while updating the password")).build();
	    }
	}

	
	 private String createErrorResponse(String message) throws JsonProcessingException {
        return objectMapper.writeValueAsString(new ApiResponse("ERROR", message));
    }
	 
	 private String createRegistrationResponse(String message, String username) throws JsonProcessingException {
	        return objectMapper.writeValueAsString(new RegisterResponse("SUCCESS", message, username));
	    }

    private String createSuccessResponse(String message) throws JsonProcessingException {
        return objectMapper.writeValueAsString(new ApiResponse("SUCCESS", message));
    }
    
    

	
}
