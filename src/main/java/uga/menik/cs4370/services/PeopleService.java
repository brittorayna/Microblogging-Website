/**
Copyright (c) 2024 Sami Menik, PhD. All rights reserved.

This is a project developed by Dr. Menik to give the students an opportunity to apply database concepts learned in the class in a real world project. Permission is granted to host a running version of this software and to use images or videos of this work solely for the purpose of demonstrating the work to potential employers. Any form of reproduction, distribution, or transmission of the software's source code, in part or whole, without the prior written consent of the copyright owner, is strictly prohibited.
*/
package uga.menik.cs4370.services;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import uga.menik.cs4370.models.FollowableUser;
import uga.menik.cs4370.models.User;
import uga.menik.cs4370.utility.Utility;

/**
 * This service contains people related functions.
 */
@Service
public class PeopleService {
    // dataSource enables talking to the database.
    private final DataSource dataSource;
   
    /**
     * See AuthInterceptor notes regarding dependency injection and
     * inversion of control.
     */
    @Autowired
    public PeopleService(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * Queries the database to retrieve a list of users that can be followed.
     * 
     * @param userIdToExclude The ID of the user that should be excluded from the list.
     * @return A list of FollowableUser objects representing users that can be followed.
     */
    public List<FollowableUser> getFollowableUsers(String userIdToExclude) {
        List<FollowableUser> followableUsers = new ArrayList<>();

        // Write an SQL query to find the users that are not the current user.
            String sql = "SELECT * FROM user where userId <> ?";
        
        // Run the query with a datasource.
        // See UserService.java to see how to inject DataSource instance and
        // use it to run a query.
        try (Connection conn = dataSource.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // Following line replaces the first place holder with userIdToExclude.
            pstmt.setString(1, userIdToExclude);

            try (ResultSet rs = pstmt.executeQuery()) {
               // System.out.println("hello");
                // Traverse the result rows one at a time.
                // Note: This specific while loop will only run at most once 
                // since username is unique.
                while (rs.next()) {
                    // Note: rs.get.. functions access attributes of the current row.
                    String userId = rs.getString("userId");
                    String firstName = rs.getString("firstName");
                    String lastName = rs.getString("lastName");
                    Boolean isFollowed = isUserFollowed(userIdToExclude, userId);
                    String lastActiveDate = getLastActiveDate(userId);

                    followableUsers.add(new FollowableUser(userId, firstName, lastName, isFollowed, lastActiveDate));
                }
            }
        } catch (SQLException e) {
            // Handle SQL exception
            e.printStackTrace();
        }        
            return followableUsers;
    }

    /**
     * Checks if a user is followed by another user.
     * 
     * @param currentSessionUserId The ID of the user performing the check.
     * @param userIdToCheck The ID of the user to check if they are followed.
     * @return true if the user with userIdToCheck is followed by the user with currentSessionUserId, false otherwise.
     */ 
    public boolean isUserFollowed(String currentSessionUserId, String userIdToCheck) {
        try (Connection conn = dataSource.getConnection();
                PreparedStatement pstmt = conn.prepareStatement("SELECT * FROM follow WHERE followerUserId = ? AND followeeUserId = ?")) {
            
                pstmt.setString(1, currentSessionUserId);
                pstmt.setString(2, userIdToCheck);

            try (ResultSet rs = pstmt.executeQuery()) {
            
                if (rs.next()) {
                    System.out.println("user" + userIdToCheck + " is  followed");
                    return true; // if there exists is one row of the result table, the user is followed; otherwise, not followed.
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        // returns false if an exception occurs or if the query doesn't return any result.
        System.out.println("user" + userIdToCheck + " is NOT followed");
        return false;
    }

    /**
     * Establishes a follow relationship between two users.
     * 
     * @param currentSessionUserId The ID of the user initiating the follow.
     * @param userIdToFollow The ID of the user to be followed.
     * @return true if the follow operation was successful, false otherwise.
     */
    public boolean followUser(String currentSessionUserId, String userIdToFollow) {
        // SQL query to insert a new row into the follow table
        String sql = "INSERT INTO follow (followerUserId, followeeUserId) VALUES (?, ?)";
    
        try (Connection conn = dataSource.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            // Set the values for the parameters in the SQL query
            pstmt.setString(1, currentSessionUserId);
            pstmt.setString(2, userIdToFollow);
    
            // Execute the SQL query to insert the new follow relationship
            int rowsAffected = pstmt.executeUpdate();
    
            // If at least one row is affected, the follow operation was successful
            return rowsAffected > 0;

        } catch (SQLException e) {
            // Handle any SQL exceptions
            e.printStackTrace();

            return false; // Return false to indicate that the follow operation failed
        }
    }


    /**
     * Removes the follow relationship between two users, effectively unfollowing a user.
     * 
     * @param currentSessionUserId The ID of the user initiating the unfollow.
     * @param userIdToUnfollow The ID of the user to be unfollowed.
     * @return true if the unfollow operation was successful, false otherwise.
     */
    public boolean unfollowUser(String currentSessionUserId, String userIdToUnfollow) {
        // SQL query to insert a new row into the follow table
        String sql = "DELETE FROM follow WHERE followerUserId = ? AND followeeUserId = ?";
    
        try (Connection conn = dataSource.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            // Set the values for the parameters in the SQL query
            pstmt.setString(1, currentSessionUserId);
            pstmt.setString(2, userIdToUnfollow);
    
            // Execute the SQL query to delete the follow relationship
            int rowsAffected = pstmt.executeUpdate();
    
            // If at least one row is affected, the follow operation was successful
            return rowsAffected > 0;

        } catch (SQLException e) {
            // Handle any SQL exceptions
            e.printStackTrace();

            return false; // Return false to indicate that the follow operation failed
        }
    }


    /**
     * Removes the follow relationship between two users, effectively unfollowing a user.
     * 
     * @param currentSessionUserId The ID of the user initiating the unfollow.
     * @param userIdToUnfollow The ID of the user to be unfollowed.
     * @return true if the unfollow operation was successful, false otherwise.
     */
    public boolean unfollowUser(String currentSessionUserId, String userIdToUnfollow) {
        // SQL query to insert a new row into the follow table
        String sql = "DELETE FROM follow WHERE followerUserId = ? AND followeeUserId = ?";
    
        try (Connection conn = dataSource.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            // Set the values for the parameters in the SQL query
            pstmt.setString(1, currentSessionUserId);
            pstmt.setString(2, userIdToUnfollow);
    
            // Execute the SQL query to delete the follow relationship
            int rowsAffected = pstmt.executeUpdate();
    
            // If at least one row is affected, the follow operation was successful
            return rowsAffected > 0;

        } catch (SQLException e) {
            // Handle any SQL exceptions
            e.printStackTrace();

            return false; // Return false to indicate that the follow operation failed
        }
    }

    /**
     * Retreives last active date of a user based on their last post date.
     * 
     * @param  userId The ID of user.
     * @return String of the last active date of user.
     */ 
    public String getLastActiveDate (String userId) {
       String lastActiveDate = "this user has not made a post yet..."; 
        
       // SQL query to get a row from the post table
        String sql = "SELECT * FROM post WHERE userId = ? ORDER BY postDate DESC LIMIT 1";

        try (Connection conn = dataSource.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            // Set the values for the parameters in the SQL query
            pstmt.setString(1, userId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    lastActiveDate = rs.getString("postDate");

                    SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    SimpleDateFormat outputFormat = new SimpleDateFormat("MMM dd, yyyy, hh:mm a");
                    Date postDate = inputFormat.parse(lastActiveDate); 
                    String formattedPostDate = outputFormat.format(postDate);

                    lastActiveDate = formattedPostDate;                    
                }
            }            
        } catch (SQLException | ParseException e) {
            // Handle any SQL exceptions
            e.printStackTrace();
        }

        return lastActiveDate; // Returns default string that says the user has not posted yet
    }
 
    
}
