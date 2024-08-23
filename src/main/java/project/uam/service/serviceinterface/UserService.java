package project.uam.service.serviceinterface;

import java.sql.SQLException;
import java.util.List;

import project.uam.entity.User;
import project.uam.util.UpdatePasswordRequest;

public interface UserService {
	void registerUser(User user) throws SQLException;
	public String generateUniqueUsername(String firstname, String lastname) throws SQLException;
	User getUserByUsername(String username) throws SQLException;
	boolean isUsernameTaken(String username) throws SQLException;
	void updateUser(User user) throws SQLException;
	void updateUserDetail(String username, User user) throws SQLException;
	void deleteUser(int id) throws SQLException;
    List<User> getAllUsers() throws SQLException;
    boolean isFirstUser() throws SQLException;
    User getUserByEmail(String email) throws SQLException;
    public void updateOtp(User user) throws SQLException ;
    public void updateUserPassword(User user) throws SQLException;
}
