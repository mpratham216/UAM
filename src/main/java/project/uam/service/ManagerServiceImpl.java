package project.uam.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import project.uam.entity.User;
import project.uam.service.serviceinterface.ManagerService;
import project.uam.util.JDBCUtil;

public class ManagerServiceImpl implements ManagerService{
	private static final Logger log = LoggerFactory.getLogger(ManagerServiceImpl.class);
	
	
	// Add the user to team.
	@Override
	public void addUserToTeam(int userId, String managerUsername) throws Exception {
		//
		String checkManager = "SELECT u.manager_id, m.username as manager_name FROM users u LEFT JOIN users m ON u.manager_id = m.user_id WHERE u.user_id = ?"; 
		String query = "UPDATE users u JOIN (SELECT user_id FROM users WHERE username = ?) m ON 1=1 SET u.manager_id = m.user_id WHERE u.user_id = ?";
        
		try (Connection conn = JDBCUtil.getConnection();
		         PreparedStatement checkStmt = conn.prepareStatement(checkManager);
		         PreparedStatement updateStmt = conn.prepareStatement(query)) {
		         
		        // Check if the user is already in a team
		        checkStmt.setInt(1, userId);
		        try (ResultSet rs = checkStmt.executeQuery()) {
		            if (rs.next()) {
		                int managerId = rs.getInt("manager_id");
		                String managerName = rs.getString("manager_name");
		                
		                if (managerId != 0) {
		                    throw new Exception("User is already in the team of manager: " + managerName);
		                }
		            }
		        }
		        
		        // Update the user's manager if not already in a team
		        updateStmt.setString(1, managerUsername);
		        updateStmt.setInt(2, userId);
		        updateStmt.executeUpdate();
		    } catch (Exception e) {
		        log.error("Error in adding user to team", e);
		        throw e; // Re-throw the exception to be caught in the controller
		    }
		}

	//Get all team members
	@Override
	public List<User> getTeamMembers(String managerUsername) throws Exception {
		List<User> teamMembers = new ArrayList<>();
		try(Connection conn = JDBCUtil.getConnection()){
			String sql = "SELECT user_id, firstname, lastname, username, email FROM users WHERE manager_id = (SELECT user_id FROM users WHERE username = ?)";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, managerUsername);
                ResultSet resultSet = stmt.executeQuery();
                while (resultSet.next()) {
                    User user = new User();
                    user.setId(resultSet.getInt("user_id"));
                    user.setFirstname(resultSet.getString("firstname"));
                    user.setLastname(resultSet.getString("lastname"));
                    user.setUsername(resultSet.getString("username"));
                    user.setEmail(resultSet.getString("email"));
                    teamMembers.add(user);
                }
            }
		}catch(Exception e) {
			log.error("Error in getting team members", e.getMessage());
		}
		return teamMembers;
	}
	
	//Get all organisation users whose role is user 
	@Override
	public List<User> getAllOrgUsers() throws SQLException {
		List<User> userList = new ArrayList<>();
	    String sql = "SELECT user_id, firstname, lastname, username, email, role, manager_id FROM users WHERE role = 'user'";
	    
	    try (Connection conn = JDBCUtil.getConnection(); 
	         PreparedStatement pstmt = conn.prepareStatement(sql);
	         ResultSet rs = pstmt.executeQuery()) {
	        
	        while (rs.next()) {
	            User user = new User();
	            user.setId(rs.getInt("user_id"));
	            user.setFirstname(rs.getString("firstname"));
	            user.setLastname(rs.getString("lastname"));
	            user.setUsername(rs.getString("username"));
	            user.setEmail(rs.getString("email"));
	            user.setRole(rs.getString("role"));
	            
	            int managerId = rs.getInt("manager_id");
	            if (managerId != 0) {
	                String managerUsername = getUsernameById(managerId);
	                user.setManagerUsername(managerUsername);
	            }
	            
	            userList.add(user);
	        }
	    } catch (Exception e) {
	        log.error("Error in getting all users in Manager Page", e.getMessage());
	    }
	    return userList;
	}
	
	// Helper method to get a unique username by a unique user_id.
	public String getUsernameById(int userId) throws SQLException {
	    String sql = "SELECT username FROM users WHERE user_id = ?";
	    try (Connection conn = JDBCUtil.getConnection();
	         PreparedStatement pstmt = conn.prepareStatement(sql)) {
	        pstmt.setInt(1, userId);
	        try (ResultSet rs = pstmt.executeQuery()) {
	            if (rs.next()) {
	                return rs.getString("username");
	            }
	        }
	    } catch (Exception e) {
	        log.error("Error in getting username by ID", e.getMessage());
	    }
	    return null;
	}
	
	// Method to remove user from the manager team.
	@Override
	public void removeUserFromTeam(int userId) throws Exception {
		String sql = "UPDATE users SET manager_id = NULL WHERE user_id = ?";
		try (Connection conn = JDBCUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            int rowsUpdated = stmt.executeUpdate();
            if (rowsUpdated == 0) {
                throw new Exception("User not found or already not part of any team");
            }
    	} catch (SQLException e) {
            log.error("Error in removing user from team: " + e.getMessage());
        }
	}

}
