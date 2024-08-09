package project.uam.util;

public class RegisterResponse {
	private String status;
    private String message;
    private String username;
    public RegisterResponse(String status, String message, String username) {
        this.status = status;
        this.message = message;
        this.username = username;
    }

    // Getters and setters
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}
    
}
