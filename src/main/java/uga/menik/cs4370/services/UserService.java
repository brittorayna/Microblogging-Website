/**
Copyright (c) 2024 Sami Menik, PhD. All rights reserved.

This is a project developed by Dr. Menik to give the students an opportunity to apply database concepts learned in the class in a real world project. Permission is granted to host a running version of this software and to use images or videos of this work solely for the purpose of demonstrating the work to potential employers. Any form of reproduction, distribution, or transmission of the software's source code, in part or whole, without the prior written consent of the copyright owner, is strictly prohibited.
*/
package uga.menik.cs4370.services;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.SessionScope;

import uga.menik.cs4370.models.User;

/**
 * This is a service class that enables user related functions.
 * The class interacts with the database through a dataSource instance.
 * See authenticate and registerUser functions for examples.
 * This service object is spcial. It's lifetime is limited to a user session.
 * Usual services generally have application lifetime.
 */
@Service
@SessionScope
public class UserService {

    // dataSource enables talking to the database.
    private final DataSource dataSource;
    // passwordEncoder is used for password security.
    private final BCryptPasswordEncoder passwordEncoder;
    // This holds user of the current session user. 
    private User loggedInUser = null;

    /**
     * See AuthInterceptor notes regarding dependency injection and
     * inversion of control.
     */
    @Autowired
    public UserService(DataSource dataSource) {
        this.dataSource = dataSource;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }


    /**
     * Authenticates a user given the username and password and
     * stores the user object for the logged-in user in session scope.
     * Returns true if authentication is successful, false otherwise.
     *
     * @param username The username of the user attempting to authenticate.
     * @param password The password of the user attempting to authenticate.
     * @return true if authentication is successful, false otherwise.
     * @throws SQLException If an SQL exception occurs during the authentication process.
     */
    public boolean authenticate(String username, String password) throws SQLException {
        // Note the ? mark in the query. It is a place holder that we will later replace.
        final String sql = "select * from user where username = ?";
        try (Connection conn = dataSource.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // Following line replaces the first place holder with username.
            pstmt.setString(1, username);

            try (ResultSet rs = pstmt.executeQuery()) {
                // Traverse the result rows one at a time.
                // Note: This specific while loop will only run at most once 
                // since username is unique.
                while (rs.next()) {
                    // Note: rs.get.. functions access attributes of the current row.
                    String storedPasswordHash = rs.getString("password");
                    boolean isPassMatch = passwordEncoder.matches(password, storedPasswordHash);
                    // Note: 
                    if (isPassMatch) {
                        String userId = rs.getString("userId");
                        String firstName = rs.getString("firstName");
                        String lastName = rs.getString("lastName");

                        // Initialize and retain the logged in user.
                        loggedInUser = new User(userId, firstName, lastName);
                    }
                    return isPassMatch;
                }
            }
        }
        return false;
    }

    /**
     * Logs out the user by clearing the currently logged-in user session.
     */
    public void unAuthenticate() {
        loggedInUser = null;
    }

    /**
     * Checks if a user is currently authenticated.
     *
     * @return true if a user is authenticated, false otherwise.
     */
    public boolean isAuthenticated() {
        return loggedInUser != null;
    }

    /**
     * Retrieves the currently logged-in user.
     *
     * @return The User object representing the currently logged-in user, or null if no user is logged in.
     */
    public User getLoggedInUser() {
        return loggedInUser;
    }

    /**
     * Registers a new user with the given details.
     * Returns true if registration is successful. If the username already exists,
     * a SQLException is thrown due to the unique constraint violation, which should
     * be handled by the caller.
     *
     * @param username The username of the new user.
     * @param password The password of the new user.
     * @param firstName The first name of the new user.
     * @param lastName The last name of the new user.
     * @return true if registration is successful, false otherwise.
     * @throws SQLException If an SQL exception occurs during the registration process.
     */
    public boolean registerUser(String username, String password, String firstName, String lastName)
            throws SQLException {
        // Note the ? marks in the SQL statement. They are placeholders like mentioned above.
        final String registerSql = "insert into user (username, password, firstName, lastName) values (?, ?, ?, ?)";

        try (Connection conn = dataSource.getConnection();
                PreparedStatement registerStmt = conn.prepareStatement(registerSql)) {
            // Following lines replace the placeholders 1-4 with values.
            registerStmt.setString(1, username);
            registerStmt.setString(2, passwordEncoder.encode(password));
            registerStmt.setString(3, firstName);
            registerStmt.setString(4, lastName);

            // Execute the statement and check if rows are affected.
            int rowsAffected = registerStmt.executeUpdate();
            return rowsAffected > 0;
        }
    }

    /**
     * Retrieves a User object from the database based on the given userId.
     *
     * @param userId The unique identifier of the user to retrieve.
     * @return The User object corresponding to the given userId, or null if no user with the specified ID is found.
     */
    public User getUser(String userId) {
        User user = null;
        String sql = "SELECT * FROM user WHERE userId = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, userId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String id = rs.getString("userId");
                    String firstName = rs.getString("firstName");
                    String lastName = rs.getString("lastName");
                    user = new User(id, firstName, lastName);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return user;
    }


}
