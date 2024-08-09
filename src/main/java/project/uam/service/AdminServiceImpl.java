package project.uam.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import project.uam.entity.User;
import project.uam.service.serviceinterface.AdminService;
import project.uam.util.JDBCUtil;
import project.uam.util.PasswordUtil;

public class AdminServiceImpl implements AdminService {
	private static final Logger log = LoggerFactory.getLogger(AdminServiceImpl.class);

	@Override
	public List<User> getAllUsers() throws SQLException {
		List<User> userList = new ArrayList<>();
		String sql = "SELECT user_id, firstname, lastname, username, email, role FROM users";
		
		try(Connection conn = JDBCUtil.getConnection(); 
			PreparedStatement pstmt = conn.prepareStatement(sql);
			ResultSet rs = pstmt.executeQuery()){
			
			while(rs.next()) {
				User user = new User();
				user.setId(rs.getInt("user_id"));
				user.setFirstname(rs.getString("firstname"));
				user.setLastname(rs.getString("lastname"));
				user.setUsername(rs.getString("username"));
				user.setEmail(rs.getString("email"));
				user.setRole(rs.getString("role"));
				userList.add(user);
			}
		}catch(Exception e) {
			log.error("Error in getting all users in Admin", e.getMessage());
		}
		return userList;
	}

	@Override
	public void changeRole(int userId, String newRole) throws SQLException {
		String sql = "UPDATE users SET role = ? WHERE user_id = ?";
		String managerIdSql = "UPDATE users SET manager_id = ? WHERE user_id = ?";
	    String getAdminIdSql = "SELECT user_id FROM users WHERE role = 'admin' LIMIT 1";
		try(Connection conn = JDBCUtil.getConnection(); 
			PreparedStatement pstmt = conn.prepareStatement(sql)){
				pstmt.setString(1, newRole);
				pstmt.setInt(2, userId);
				pstmt.executeUpdate();
				
				if("manager".equalsIgnoreCase(newRole)) {
					PreparedStatement pstmtGetAdminId = conn.prepareStatement(getAdminIdSql);
					ResultSet rs = pstmtGetAdminId.executeQuery();
					if (rs.next()) {
						int adminId = rs.getInt("user_id");
						PreparedStatement pstmtManagerId = conn.prepareStatement(managerIdSql);
						pstmtManagerId.setInt(1, adminId);
		                pstmtManagerId.setInt(2, userId);
		                pstmtManagerId.executeUpdate();
					}
				}
			}catch(Exception e) {
				log.error("Role Change Failed", e.getMessage());
			}
	}
	
	@Override
	public void processCsvFile(InputStream inputStream) throws IOException, SQLException {
	    try (BufferedReader br = new BufferedReader(new InputStreamReader(inputStream))) {
	        String line;
	        while ((line = br.readLine()) != null) {
	            String[] values = line.split(",");
	            if (values.length == 3) {
	                // Assuming the CSV for resources: resource_name, description, manager_only
	                addResourceFromCsv(values[0], values[1], Boolean.parseBoolean(values[2]));
	            } else if (values.length == 5) {
	                // Assuming the CSV for users: firstname, lastname, username, password, email
	                addUserFromCsv(values[0], values[1], values[2], values[3], values[4]);
	            }
	        }
	    }catch(Exception e) {
	    	log.error("Error while reading csv file", e.getMessage());
	    }
	}
	
	private void addResourceFromCsv(String resourceName, String description, boolean managerOnly) throws SQLException {
	    String sql = "INSERT INTO resources (resource_name, description, manager_only) VALUES (?, ?, ?)";
	    try (Connection conn = JDBCUtil.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
	        pstmt.setString(1, resourceName);
	        pstmt.setString(2, description);
	        pstmt.setBoolean(3, managerOnly);
	        pstmt.executeUpdate();
	    }catch(Exception e) {
	    	log.error("Error while reading csv file", e.getMessage());
	    }
	}

	private void addUserFromCsv(String firstname, String lastname, String username, String password, String email) throws SQLException {
		if (!PasswordUtil.isPasswordComplex(password)) {
	        throw new IllegalArgumentException("Password does not meet complexity requirements.");
	    }

	    String hashedPassword = PasswordUtil.hashPassword(password);
		String sql = "INSERT INTO users (firstname, lastname, username, password, email) VALUES (?, ?, ?, ?, ?)";
	    try (Connection conn = JDBCUtil.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
	        pstmt.setString(1, firstname);
	        pstmt.setString(2, lastname);
	        pstmt.setString(3, username);
	        pstmt.setString(4, hashedPassword);  // Ensure you hash the password before storing
	        pstmt.setString(5, email);
	        pstmt.executeUpdate();
	    }catch(Exception e) {
	    	log.error("Error while reading csv file", e.getMessage());
	    }
	}


}
