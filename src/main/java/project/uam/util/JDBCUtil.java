package project.uam.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// JDBC Connectivity is handled by this class in the whole application.
public class JDBCUtil {
	private static final Logger log = LoggerFactory.getLogger(JDBCUtil.class);
	private static final String URL = "jdbc:mysql://localhost:3306/UAM_DB";
    private static final String USER = "root";
    private static final String PASSWORD = "root";
    

    static {
        try {
            // Load MySQL JDBC Driver
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            log.error("MySQL JDBC Driver not found.", e);
            throw new RuntimeException("MySQL JDBC Driver not found.", e);
        }
    }
    
    public static Connection getConnection() throws SQLException {
    	Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
		log.info("Database Connected");
		return conn;
    }
}
