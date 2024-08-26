package project.uam.service.serviceinterface;

import project.uam.entity.Resources;
import project.uam.entity.User;

import java.sql.SQLException;
import java.util.List;

import project.uam.entity.Request;

public interface ResourceService {
	 	void addResource(Resources resource) throws SQLException;
	    List<Resources> getAllResources(String username) throws SQLException;
	    List<Resources> getMyResources(String username);
	    public List<User> getUsersByResource(String resourceName) throws Exception;
	    void approveRequest(int requestId) throws SQLException;
	    void rejectRequest(int requestId) throws SQLException;
	    List<Request> getAllRequests() throws SQLException;
	    void requestResource(Request request) throws SQLException;
	    List<Request> getRequestsByUser(String username) throws SQLException;
	    void removeResource(int resourceId) throws SQLException;
}
