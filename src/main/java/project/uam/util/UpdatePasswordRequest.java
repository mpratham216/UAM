package project.uam.util;

public class UpdatePasswordRequest {
	String username;
	String currentpassword;
	String newPassword;
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getCurrentpassword() {
		return currentpassword;
	}
	public void setCurrentpassword(String currentpassword) {
		this.currentpassword = currentpassword;
	}
	public String getNewPassword() {
		return newPassword;
	}
	public void setNewPassword(String newPassword) {
		this.newPassword = newPassword;
	}
	
	
}
