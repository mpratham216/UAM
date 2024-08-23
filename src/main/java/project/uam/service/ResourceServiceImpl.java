package project.uam.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import project.uam.entity.Request;
import project.uam.entity.Resources;
import project.uam.service.serviceinterface.ResourceService;
import project.uam.util.JDBCUtil;

public class ResourceServiceImpl implements ResourceService{
	private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);
	
	//Add unique resources in the organisations assets list.
	@Override
	public void addResource(Resources resource) throws SQLException {
	    String checkResourceSql = "SELECT COUNT(*) FROM resources WHERE resource_name = ?";
	    String insertResourceSql = "INSERT INTO resources (resource_name, description, manager_only) VALUES (?, ?, ?)";

	    try (Connection conn = JDBCUtil.getConnection();
	         PreparedStatement checkStmt = conn.prepareStatement(checkResourceSql);
	         PreparedStatement insertStmt = conn.prepareStatement(insertResourceSql)) {

	        // Check if the resource name already exists
	        checkStmt.setString(1, resource.getName());
	        try (ResultSet rs = checkStmt.executeQuery()) {
	            if (rs.next() && rs.getInt(1) > 0) {
	                throw new SQLException("Resource with this name already exists.");
	            }
	        }

	        // Insert the new resource
	        insertStmt.setString(1, resource.getName());
	        insertStmt.setString(2, resource.getDescription());
	        insertStmt.setBoolean(3, resource.isManagerOnly());
	        insertStmt.executeUpdate();
	    } catch (SQLException e) {
	        log.error("Error during adding resource", e.getMessage());
	        throw e; // Rethrow to be caught in the controller
	    }
	}

	
	// Get all the resources of organisation.
	@Override
	public List<Resources> getAllResources(String username) throws SQLException {
		List<Resources> resourceList = new ArrayList<>();
		String userRole = getUserRoleByUsername(username);
		String sql;
		// Role based resources can be allocated. Few roles are for admin and manager only.
		if ("manager".equalsIgnoreCase(userRole) || "admin".equalsIgnoreCase(userRole)) {
			sql = "SELECT resource_id, resource_name, description FROM resources";
		}else {
			sql = "SELECT resource_id, resource_name, description FROM resources WHERE manager_only = FALSE";
		}
		try(Connection conn = JDBCUtil.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql);ResultSet rs = pstmt.executeQuery()){
			while(rs.next()) {
				Resources resources = new Resources();
				resources.setId(rs.getInt("resource_id"));
				 resources.setName(rs.getString("resource_name"));
	             resources.setDescription(rs.getString("description"));
	             resourceList.add(resources);
			}
		}catch(Exception e) {
			log.error("Error Fetching all resources", e.getMessage());
		}
		return resourceList;
	}
	
	
	// Helper method to get a role by username.
	public String getUserRoleByUsername(String username) throws SQLException {
	    String role = null;
	    String sql = "SELECT role FROM users WHERE username = ?";
	    
	    try (Connection conn = JDBCUtil.getConnection(); 
	         PreparedStatement pstmt = conn.prepareStatement(sql)) {
	        pstmt.setString(1, username);
	        
	        try (ResultSet rs = pstmt.executeQuery()) {
	            if (rs.next()) {
	                role = rs.getString("role");
	            }
	        }
	    } catch (Exception e) {
	        log.error("Error fetching user role", e.getMessage());
	    }
	    
	    return role;
	}

	// Approving a request.
	@Override
	public void approveRequest(int requestId) throws SQLException {
		String sql = "UPDATE requests SET request_status = 'Approved' WHERE request_id = ?";
		String usersql = "INSERT INTO user_resources (user_id, resource_id) VALUES (?, ?)";
		try(Connection conn = JDBCUtil.getConnection();PreparedStatement pstmt = conn.prepareStatement(sql); 
				PreparedStatement updateUserStmt = conn.prepareStatement(usersql)){
			pstmt.setInt(1, requestId);
			pstmt.executeUpdate();
			String selectsql = "SELECT user_id, resource_id FROM requests WHERE request_id = ?";
			try (PreparedStatement selectStmt = conn.prepareStatement(selectsql)) {
	            selectStmt.setInt(1, requestId);
	            try (ResultSet rs = selectStmt.executeQuery()) {
	                if (rs.next()) {
	                    int userId = rs.getInt("user_id");
	                    int resourceId = rs.getInt("resource_id");
	                    
	                    // Insert into user_resources
	                    updateUserStmt.setInt(1, userId);
	                    updateUserStmt.setInt(2, resourceId);
	                    updateUserStmt.executeUpdate();
	                }
	            }
	        }
		}catch(Exception e) {
			log.error("Error during request approval",e.getMessage());
		}
	}
	
	// Rejecting a request.
	@Override
	public void rejectRequest(int requestId) throws SQLException {
	    String sql = "UPDATE requests SET request_status = 'Rejected' WHERE request_id = ?";
	    try (Connection conn = JDBCUtil.getConnection();
	         PreparedStatement pstmt = conn.prepareStatement(sql)) {
	        pstmt.setInt(1, requestId);
	        pstmt.executeUpdate();
	    } catch (Exception e) {
	        log.error("Error during rejecting request", e.getMessage());
	    }
	}

	// Get all the requests from the request list whose stauts is PENDING.
	@Override
	public List<Request> getAllRequests() throws SQLException {
		List<Request> requestList = new ArrayList<>();
		String sql = "SELECT r.request_id, r.user_id, r.resource_id, res.resource_name, r.request_status, u.username " +
                "FROM requests r " +
                "JOIN users u ON r.user_id = u.user_id " +
                "JOIN resources res ON r.resource_id = res.resource_id " +
                "WHERE r.request_status = 'pending'";
		try(Connection conn = JDBCUtil.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql);ResultSet rs = pstmt.executeQuery()){
			
			while(rs.next()) {
				Request request = new Request();
				request.setId(rs.getInt("request_id"));
                request.setUserId(rs.getInt("user_id"));
                request.setResourceId(rs.getInt("resource_id"));
                request.setResourceName(rs.getString("resource_name"));
                request.setStatus(rs.getString("request_status"));
                request.setUsername(rs.getString("username"));
                requestList.add(request);
			}
		}catch(Exception e) {
			log.error("Error Fetching all request", e.getMessage());
		}
		return requestList;
	}
	
	// Request resources 
	@Override
	public void requestResource(Request request) throws SQLException {
		// First get the user id from the username.
	    String userIdSql = "SELECT user_id FROM users WHERE username = ?";
	    
	    // Then check the status of the request from on basis of user_id and resource_id.
	    String checkRequestSql = "SELECT request_status FROM requests WHERE user_id = ? AND resource_id = ?";
	    
	    // For requesting INSERTING data in requests table and keeping the intial status as PENDING default.
	    String insertRequestSql = "INSERT INTO requests (user_id, resource_id, request_status) VALUES (?, ?, 'PENDING')";
	   
	    // Conditional when a request is rejected.
	    // Update the requests on the basis of admin control. So if a request is rejected a user can request the resource again so turn the status from REJECTED to PENDING.
	    String updateRequestSql = "UPDATE requests SET request_status = 'PENDING' WHERE user_id = ? AND resource_id = ? AND request_status = 'REJECTED'";

	    try (Connection conn = JDBCUtil.getConnection();
	         PreparedStatement userIdStmt = conn.prepareStatement(userIdSql);
	         PreparedStatement checkRequestStmt = conn.prepareStatement(checkRequestSql);
	         PreparedStatement insertRequestStmt = conn.prepareStatement(insertRequestSql);
	         PreparedStatement updateRequestStmt = conn.prepareStatement(updateRequestSql)) {

	        // Get the user_id from username
	        userIdStmt.setString(1, request.getUsername());
	        try (ResultSet rs = userIdStmt.executeQuery()) {
	            if (rs.next()) {
	                int userId = rs.getInt("user_id");

	                // Check if the request already exists and its status
	                checkRequestStmt.setInt(1, userId);
	                checkRequestStmt.setInt(2, request.getResourceId());
	                try (ResultSet checkRs = checkRequestStmt.executeQuery()) {
	                    if (checkRs.next()) {
	                        String status = checkRs.getString("request_status");
	                        if ("REJECTED".equals(status)) {
	                            // Update the rejected request to pending again
	                            updateRequestStmt.setInt(1, userId);
	                            updateRequestStmt.setInt(2, request.getResourceId());
	                            updateRequestStmt.executeUpdate();
	                        } else {
	                            throw new SQLException("Resource request already exists and is not rejected.");
	                        }
	                    } else {
	                        // Insert the request with user_id and resource_id if no previous request exists
	                        insertRequestStmt.setInt(1, userId);
	                        insertRequestStmt.setInt(2, request.getResourceId());
	                        insertRequestStmt.executeUpdate();
	                    }
	                }
	            } else {
	                throw new SQLException("User not found");
	            }
	        }
	    } catch (SQLException e) {
	        log.error("Error occurred while requesting resources", e.getMessage());
	        throw e;
	    }
	}

	// Get the user's resources by there username. 
	@Override
	public List<Resources> getMyResources(String username) {
		List<Resources> resources = new ArrayList<>();
        String sql = "SELECT r.resource_id, r.resource_name, r.description " +
        		"FROM resources r " +
        		"JOIN user_resources ur ON r.resource_id = ur.resource_id " +
        		"JOIN users u ON ur.user_id = u.user_id " +
        		"WHERE u.username = ?";

        try (Connection conn = JDBCUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, username);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Resources resource = new Resources();
                    resource.setId(rs.getInt("resource_id"));
                    resource.setName(rs.getString("resource_name"));
                    resource.setDescription(rs.getString("description"));
                    resources.add(resource);
                }
            }
        } catch (SQLException e) {
        	log.error("Error in getting users resources", e.getMessage());
            e.printStackTrace();
        }
        return resources;
	}
	
	// Fetch user's requests to determine the status
	@Override
	public List<Request> getRequestsByUser(String username) throws SQLException {
	    List<Request> requestList = new ArrayList<>();
	    String sql = "SELECT r.request_id, r.resource_id, r.request_status " +
	                 "FROM requests r " +
	                 "JOIN users u ON r.user_id = u.user_id " +
	                 "WHERE u.username = ?";

	    try (Connection conn = JDBCUtil.getConnection();
	         PreparedStatement pstmt = conn.prepareStatement(sql)) {

	        pstmt.setString(1, username);
	        
	        try (ResultSet rs = pstmt.executeQuery()) {
	            while (rs.next()) {
	                Request request = new Request();
	                request.setId(rs.getInt("request_id"));
	                request.setResourceId(rs.getInt("resource_id"));
	                request.setStatus(rs.getString("request_status"));
	                requestList.add(request);
	            }
	        }
	    } catch (SQLException e) {
	        log.error("Error in getting user requests", e.getMessage());
	        e.printStackTrace();
	    }
	    return requestList;
	}


	@Override
	public void removeResource(int resourceId) throws SQLException {
		String checkSql = "SELECT COUNT(*) FROM requests WHERE resource_id = ?";
		String sql = "DELETE FROM resources WHERE resource_id = ?";
		try (Connection conn = JDBCUtil.getConnection();
				PreparedStatement checkStmt = conn.prepareStatement(checkSql);
				PreparedStatement pstmt = conn.prepareStatement(sql)) {
			
			// Check if any requests exist for this resource
	        checkStmt.setInt(1, resourceId);
	        try (ResultSet rs = checkStmt.executeQuery()) {
	            if (rs.next() && rs.getInt(1) > 0) {
	                throw new SQLException("Cannot delete resource as it has associated requests.");
	            }
	        }
	        
	        // Proceed to delete the resource if no requests exist
			pstmt.setInt(1, resourceId);
			int affectedRows = pstmt.executeUpdate();
			 if (affectedRows == 0) {
	           throw new SQLException("Resource not found or couldn't be deleted");
	         }
		}catch (SQLException e) {
            log.error("Error in removing resource", e.getMessage());
            throw e; // Re-throw the exception to be handled by the caller
        }
	}


}
