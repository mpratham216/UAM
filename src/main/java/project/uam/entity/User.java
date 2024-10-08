package project.uam.entity;

import java.util.Date;

public class User {
	private int id;
	private String firstname;
	private String lastname;
	private String username;
	private String password;
	private String email;
	private String role;
	private int managerId;
	private String managerUsername;
	private String otp;
	private Date otpExpiry; 
	//getters and setters
	public int getId() {
		return id;
	} 	
	public void setId(int id) {
		this.id = id;
	}
	public String getFirstname() {
		return firstname;
	}
	public void setFirstname(String firstname) {
		this.firstname = firstname;
	}
	public String getLastname() {
		return lastname;
	}
	public void setLastname(String lastname) {
		this.lastname = lastname;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getRole() {
		return role;
	}
	public void setRole(String role) {
		this.role = role;
	}
	public int getManagerId() {
		return managerId;
	}
	public void setManagerId(int managerId) {
		this.managerId = managerId;
	}
	public String getManagerUsername() {
		return managerUsername;
	}
	public void setManagerUsername(String managerUsername) {
		this.managerUsername = managerUsername;
	}
	public String getOtp() {
		return otp;
	}
	public void setOtp(String otp) {
		this.otp = otp;
	}
	public Date getOtpExpiry() {
		return otpExpiry;
	}
	public void setOtpExpiry(Date otpExpiry) {
		this.otpExpiry = otpExpiry;
	}
	
	
}
