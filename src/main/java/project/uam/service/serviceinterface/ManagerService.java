package project.uam.service.serviceinterface;

import java.sql.SQLException;
import java.util.List;

import project.uam.entity.User;

public interface ManagerService {
	 void addUserToTeam(int userId, String managerUsername) throws Exception;
	 List<User> getTeamMembers(String managerUsername) throws Exception;
	 public List<User> getAllOrgUsers() throws SQLException;
	 void removeUserFromTeam(int userId) throws Exception;
}
