package project.uam.service;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import project.uam.entity.User;
import project.uam.service.serviceinterface.UserService;
import project.uam.util.JDBCUtil;
import project.uam.util.PasswordUtil;
import project.uam.util.UpdatePasswordRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserServiceImpl implements UserService {
	private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);
	
	
	//Register the user.
	@Override
	public void registerUser(User user) throws SQLException {
		String sql = "INSERT INTO USERS (firstname, lastname, username, password, email, role) VALUES(?,?,?,?,?,?)";
		try(Connection conn = JDBCUtil.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)){
			pstmt.setString(1, user.getFirstname());
			pstmt.setString(2, user.getLastname());
			pstmt.setString(3, user.getUsername());
			pstmt.setString(4, user.getPassword());
			pstmt.setString(5, user.getEmail());
			pstmt.setString(6, user.getRole());
			pstmt.executeUpdate();
		}catch(Exception e) {
			log.error("Error Registering user", e.getMessage());
			throw e;
		}
		
	}
	
	//Generate unique username.
	@Override
	public String generateUniqueUsername(String firstname, String lastname) throws SQLException {
        String baseUsername = firstname.toLowerCase() + "." + lastname.toLowerCase();
        String username = baseUsername;
        int counter = 1;

        // Check if the username already exists
        while (isUsernameTaken(username)) {
            username = baseUsername + counter++;
        }
        return username;
    }

	
	@Override
	public User getUserByUsername(String username) throws SQLException {
		String sql = "SELECT * FROM USERS WHERE username = ?";
		try(Connection conn = JDBCUtil.getConnection();PreparedStatement pstmt = conn.prepareStatement(sql)){
			pstmt.setString(1, username);
			try(ResultSet rs = pstmt.executeQuery()){
				if(rs.next()) {
					User user = new User();
					user.setId(rs.getInt("user_id"));
					user.setFirstname(rs.getString("firstname"));
					user.setLastname(rs.getString("lastname"));
					user.setUsername(rs.getString("username"));
					user.setPassword(rs.getString("password"));
					user.setEmail(rs.getString("email"));
					user.setRole(rs.getString("role"));
					return user;
				}
			}
		}catch(SQLException e) {
			log.error("Error Fetching user by username", e.getMessage());
			throw e; 	
		}
		return null;
	}
	
	// Helper method to check if the username is taken or not.
	@Override
	public boolean isUsernameTaken(String username) throws SQLException {
		String sql = "SELECT COUNT(*) FROM USERS WHERE username = ?";
		try(Connection conn = JDBCUtil.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)){
			pstmt.setString(1, username);
			try(ResultSet rs = pstmt.executeQuery()){
				if(rs.next()) {
					return rs.getInt(1) > 0;
				}
			}
		}catch(SQLException e) {
			log.error("Error checking if username is taken", e.getMessage());
			throw e;
		}
		return false;
	}

	
	// Update user details.
	@Override
	public void updateUser(User user) throws SQLException {
		String currentUserName = getUsernameById(user.getId());
		User currentUser = getUserByUsername(currentUserName);
		
		// Update user details by providing username.
		String sql = "UPDATE users SET firstname = ?, lastname = ?, email = ? WHERE username = ?";
		if (!currentUser.getFirstname().equals(user.getFirstname()) || !currentUser.getLastname().equals(user.getLastname())) {
	        
			// If the firstname or lastname has changed, generate a new unique username
	        String newUsername = generateUniqueUsername(user.getFirstname(), user.getLastname());
	        sql = "UPDATE users SET firstname = ?, lastname = ?, email = ?, username = ? WHERE username = ?";
	        try (Connection conn = JDBCUtil.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
	            pstmt.setString(1, user.getFirstname());
	            pstmt.setString(2, user.getLastname());
	            pstmt.setString(3, user.getEmail());
	            pstmt.setString(4, newUsername);
	            pstmt.setString(5, currentUserName);
	            pstmt.executeUpdate();
	            
	        } catch (SQLException e) {
	            log.error("Error updating the user", e);
	            throw e;
	        }
	    } else {
	        
	    	// If the firstname and lastname haven't changed, update without changing the username
	        try (Connection conn = JDBCUtil.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
	            pstmt.setString(1, user.getFirstname());
	            pstmt.setString(2, user.getLastname());
	            pstmt.setString(3, user.getEmail());
	            pstmt.setString(4, currentUserName);
	            pstmt.executeUpdate();
	            
	        } catch (SQLException e) {
	            log.error("Error updating the user", e);
	            throw e;
	        }
	    }
	}
	
	@Override
	public void updateOtp(User user) throws SQLException {
		 String sql = "UPDATE users SET otp = ?, otp_expiry = ? WHERE email = ?";
		    
	    try (Connection conn = JDBCUtil.getConnection(); 
	         PreparedStatement pstmt = conn.prepareStatement(sql)) {
	        pstmt.setString(1, user.getOtp());
	        pstmt.setTimestamp(2, new Timestamp(user.getOtpExpiry().getTime()));
	        pstmt.setString(3, user.getEmail());
	        pstmt.executeUpdate();
	    } catch (SQLException e) {
	        log.error("Error updating OTP for user", e);
	        throw e;
	    }
	    
	}
	
	@Override
	public void updateUserPassword(User user) throws SQLException {
	    String sql = "UPDATE users SET password = ? WHERE user_id = ?";
	    
	    try (Connection conn = JDBCUtil.getConnection(); 
	         PreparedStatement pstmt = conn.prepareStatement(sql)) {
	        // Hash the new password
	        String hashedPassword = PasswordUtil.hashPassword(user.getPassword());

	        pstmt.setString(1, hashedPassword);
	        pstmt.setInt(2, user.getId());
	        
	        int rowsAffected = pstmt.executeUpdate();
	        if (rowsAffected == 0) {
	            throw new SQLException("No user found with ID: " + user.getId());
	        }
	    } catch (SQLException e) {
	        log.error("Error updating user password for user ID: " + user.getId(), e);
	        throw e;
	    }
	}

	
	// Helper method to get username by id.
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

	
	// Delete user. Or remove user from oragnisation.
	@Override
	public void deleteUser(int id) throws SQLException {
	    // Delete user resources, requests, and user by id.
		String updateManagedUsersSQL = "UPDATE Users SET manager_id = NULL WHERE manager_id = ?";
	    String deleteUserResourcesSQL = "DELETE FROM user_resources WHERE user_id = ?";
	    String deleteUserRequestsSQL = "DELETE FROM requests WHERE user_id = ?";
	    String deleteUserSQL = "DELETE FROM users WHERE user_id = ?";

	    try (Connection conn = JDBCUtil.getConnection()) {
	        // Disable auto-commit to ensure transactional behavior
	        conn.setAutoCommit(false);
	        
	        try (PreparedStatement pstmtUpdateManagedUsers = conn.prepareStatement(updateManagedUsersSQL);
	        	 PreparedStatement pstmtResources = conn.prepareStatement(deleteUserResourcesSQL);
	             PreparedStatement pstmtRequests = conn.prepareStatement(deleteUserRequestsSQL);
	             PreparedStatement pstmtUser = conn.prepareStatement(deleteUserSQL)) {
	             
	        	
	        	// Update managed users to nullify the manager_id
	            pstmtUpdateManagedUsers.setInt(1, id);
	            pstmtUpdateManagedUsers.executeUpdate();
	            
	            // Delete user resources
	            pstmtResources.setInt(1, id);
	            pstmtResources.executeUpdate();
	            
	            // Delete user requests
	            pstmtRequests.setInt(1, id);
	            pstmtRequests.executeUpdate();
	            
	            // Delete user
	            pstmtUser.setInt(1, id);
	            pstmtUser.executeUpdate();
	            
	            // Commit the transaction
	            conn.commit();
	        } catch (SQLException e) {
	            // Rollback the transaction in case of an error
	            conn.rollback();
	            log.error("Error deleting user, resources, or requests", e);
	            throw e;
	        } finally {
	            // Re-enable auto-commit
	            conn.setAutoCommit(true);
	        }
	    }
	}

	
	@Override
	public List<User> getAllUsers() throws SQLException {
		String sql = "SELECT * FROM USERS";
		List<User> users = new ArrayList<>();
		
		try(Connection conn = JDBCUtil.getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)){
			while(rs.next()) {
				User user = new User();
				user.setId(rs.getInt("user_id"));
				user.setFirstname(rs.getString("firstname"));
				user.setLastname(rs.getString("lastname"));
				user.setUsername(rs.getString("username"));
				user.setPassword(rs.getString("password"));
				user.setEmail(rs.getString("email"));
				user.setRole(rs.getString("role"));
				users.add(user);
			}
		}catch(SQLException e) {
			log.error("Error is getting all users", e.getMessage());
		}
		return users;
	}
	
	
	// If the organisation is registering a first user then that person will be admin by default.
	@Override
	public boolean isFirstUser() throws SQLException {
		String sql = "SELECT COUNT(*) FROM Users";
        try (Connection conn = JDBCUtil.getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt(1) == 0;
            }
        }
        return false;
	}

	@Override
	public User getUserByEmail(String email) throws SQLException {
		String sql = "SELECT * FROM Users WHERE email = ?";
        try (Connection conn = JDBCUtil.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, email);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    User user = new User();
                    user.setId(rs.getInt("user_id"));
                    user.setFirstname(rs.getString("firstname"));
                    user.setLastname(rs.getString("lastname"));
                    user.setUsername(rs.getString("username"));
                    user.setPassword(rs.getString("password"));
                    user.setEmail(rs.getString("email"));
                    user.setRole(rs.getString("role"));
                    user.setOtp(rs.getString("otp"));
                    user.setOtpExpiry(rs.getTimestamp("otp_expiry")); 
                    return user;
                }
            }
        }catch(SQLException e) {
			log.error("Error in getting all users", e.getMessage());
		}
        return null;
	}

	@Override
	public void updateUserDetail(String username, User user) throws SQLException {
		String sql = "UPDATE users SET firstname = ?, lastname = ?, email = ? WHERE username = ?";
	    
	    try (Connection conn = JDBCUtil.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
	        pstmt.setString(1, user.getFirstname());
	        pstmt.setString(2, user.getLastname());
	        pstmt.setString(3, user.getEmail());
	        pstmt.setString(4, username);
	        int affectedRows = pstmt.executeUpdate();
	        
	        if (affectedRows == 0) {
	            throw new SQLException("No user found with the username: " + username);
	        }
	    } catch (SQLException e) {
	        log.error("Error updating the user with username: " + username, e);
	        throw e;
	    }
		
	}
	
}
