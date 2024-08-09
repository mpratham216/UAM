package project.uam.entity;

import java.sql.Timestamp;

public class Request {
	private int id;
	private int userId;
	private int resourceId;
	private String status;
	private Timestamp requestDate;
	private String username;
	private String resourceName;
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int getUserId() {
		return userId;
	}
	public void setUserId(int userId) {
		this.userId = userId;
	}
	public int getResourceId() {
		return resourceId;
	}
	public void setResourceId(int resourceId) {
		this.resourceId = resourceId;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public Timestamp getRequestDate() {
		return requestDate;
	}
	public void setRequestDate(Timestamp requestDate) {
		this.requestDate = requestDate;
	}
	
	 public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	
	public String getResourceName() {
		return resourceName;
	}
	public void setResourceName(String resourceName) {
		this.resourceName = resourceName;
	}
	@Override
	    public String toString() {
	        return "ResourceRequest{" +
	                "id=" + id +
	                ", userId=" + userId +
	                ", resourceId=" + resourceId +
	                ", status='" + status + '\'' +
	                ", requestDate=" + requestDate +
	                '}';
	    }
}
