package project.uam.service.serviceinterface;

import project.uam.entity.Resources;

import java.sql.SQLException;
import java.util.List;

import project.uam.entity.Request;

public interface ResourceService {
	 	void addResource(Resources resource) throws SQLException;
	    List<Resources> getAllResources(String username) throws SQLException;
	    List<Resources> getMyResources(String username);
	    void approveRequest(int requestId) throws SQLException;
	    void rejectRequest(int requestId) throws SQLException;
	    List<Request> getAllRequests() throws SQLException;
	    void requestResource(Request request) throws SQLException;
}
