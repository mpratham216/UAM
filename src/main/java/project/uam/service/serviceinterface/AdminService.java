package project.uam.service.serviceinterface;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.List;

import project.uam.entity.User;

public interface AdminService {
	List<User> getAllUsers() throws SQLException;
	void changeRole(int userId, String newRole) throws SQLException;
	void processCsvFile(InputStream inputStream) throws IOException, SQLException;
}
