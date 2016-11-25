package kristianfacebookproject;

import java.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * Using singleton design pattern for Database class
 */
public class Database {
    
    private static Connection conn = null;
    private static final Logger logger = Logger.getLogger(Database.class.getName());
    
    protected Database() { }
    
    public static Connection getInstance() {
        if(conn == null) {
            
            logger.setLevel(Level.OFF); // Set to OFF to disable all logging
            try {
                Class.forName("org.sqlite.JDBC");
                conn = DriverManager.getConnection("jdbc:sqlite:facebook.db");
            } catch ( SQLException ex ) {
                logger.severe(ex.getMessage());
                System.exit(0);
            } catch (ClassNotFoundException ex) {
                logger.severe("Add sqlite-jdbc to CLASSPATH.");
                /*
                $javac SQLiteJDBC.java
                $java -classpath ".:sqlite-jdbc-3.7.2.jar" SQLiteJDBC
                */
                logger.severe(ex.getMessage());
                System.exit(0);
            }
            logger.info("Connected to database.");
        }
        return conn;
    }
    
    public static void close() {
        try {
            conn.close();
        } catch (SQLException ex) {
            logger.severe(ex.getMessage());
        }
    }
}
