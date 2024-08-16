package project.uam.util;

import org.mindrot.jbcrypt.BCrypt;

//Hashing of the password is done by this class. This is a by default package used.
public class PasswordUtil {
	public static String hashPassword(String password) {
		return BCrypt.hashpw(password, BCrypt.gensalt());
	}
	
	
	public static boolean checkPassword(String password, String hashedPassword) {
		return BCrypt.checkpw(password, hashedPassword);
	}
	
	public static boolean isPasswordComplex(String password) {
		String regex = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=]).{8,}$";
		return password.matches(regex);
	}
	
}
