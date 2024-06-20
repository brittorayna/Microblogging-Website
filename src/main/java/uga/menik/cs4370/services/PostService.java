/**
Copyright (c) 2024 Sami Menik, PhD. All rights reserved.

This is a project developed by Dr. Menik to give the students an opportunity to apply database concepts learned in the class in a real world project. Permission is granted to host a running version of this software and to use images or videos of this work solely for the purpose of demonstrating the work to potential employers. Any form of reproduction, distribution, or transmission of the software's source code, in part or whole, without the prior written consent of the copyright owner, is strictly prohibited.
*/
package uga.menik.cs4370.services;

import java.sql.Connection;
import java.util.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.text.SimpleDateFormat;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import uga.menik.cs4370.models.FollowableUser;
import uga.menik.cs4370.models.Post;
import uga.menik.cs4370.models.Comment;
import uga.menik.cs4370.models.ExpandedPost;
import uga.menik.cs4370.models.User;
import uga.menik.cs4370.utility.Utility;

import java.sql.Statement;

/**
 * This service contains people related functions.
 */
@Service
public class PostService {
    // dataSource enables talking to the database.
    private final DataSource dataSource;

    /**
     * See AuthInterceptor notes regarding dependency injection and
     * inversion of control.
     */
    @Autowired
    public PostService(DataSource dataSource) {
        this.dataSource = dataSource;
    }


    /**
     * Creates a new post for the current session user with the given post text.
     * 
     * @param currentSessionUser The user who is creating the post.
     * @param postText The text content of the post.
     * @return A list of posts (typically containing only the newly created post).
     * If the post creation is successful, the list contains the created post; otherwise, an empty list is returned.
     */ 
    public List<Post> addPost(User currentSessionUser, String postText) {
        List<Post> posts = new ArrayList<>();
    
        // Writes an SQL query to add a new post.
        String insertPostSql = "INSERT INTO post (userId, postDate, postText) VALUES (?, CURRENT_TIMESTAMP, ?)";
    
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(insertPostSql, Statement.RETURN_GENERATED_KEYS)) {
    
            // Sets parameters for the post insertion query
            pstmt.setString(1, currentSessionUser.getUserId());
            pstmt.setString(2, postText);
    
            int rowsAffected = pstmt.executeUpdate();
    
            // Checks if the post insertion was successful
            if (rowsAffected > 0) {
                // Retrieves the generated postId and postDate
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        String postId = generatedKeys.getString(1);  // Assuming postId is of type String
                        // Parses postText to find hashtags and insert into hashtag table
                        String[] words = postText.split("\\s+");
                        for (String word : words) {
                            if (word.startsWith("#")) {
                                // Inserts the hashtag into the hashtag table
                                String hashTagText = word; // Keeps '#' with the word
                                insertHashtag(postId, hashTagText);
                            }
                        }
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return posts;
    }
    
    /** 
     * Retrieves a list of posts from the database.
     * @param currentSessionUser The user currently logged in, for whom the posts are being retrieved.
     * @param userService An instance of the UserService class, used to fetch user details.
     * @return A list of Post objects representing the posts retrieved from the database.
     */ 
    public List<Post> getPosts(User currentSessionUser, UserService userService) {
        List<Post> posts = new ArrayList<>();

            // Write an SQL query to add new post to list.
            String sql = "SELECT * FROM post ORDER BY postDate DESC";

            try (Connection conn = dataSource.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {       

            try (ResultSet rs = pstmt.executeQuery()) {
                // System.out.println("hello");
                // Traverse the result rows one at a time.
  
                while (rs.next()) {
                    // Note: rs.get.. functions access attributes of the current row.
                    String postId = rs.getString("postId");
                    String userId =  rs.getString("userId");
                    
                    // Assuming you have a method to get user details from the userId
                    User postUser = userService.getUser(userId);
                    String postDateStr = rs.getString("postDate");
                    
                    String postText = rs.getString("postText");
                    Boolean isBookmarked = isBookmarked(postId, currentSessionUser);
                    Boolean isHearted = isHearted(postId, currentSessionUser);
                    int heartsCount = getHeartsCount(postId);
                    int commentsCount = getCommentsCount(postId);

                    SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    SimpleDateFormat outputFormat = new SimpleDateFormat("MMM dd, yyyy, hh:mm a");
                    Date postDate = inputFormat.parse(postDateStr); //change to stirng?
                    String formattedPostDate = outputFormat.format(postDate);

                    //change currentSessionUser to user associated with each post; find/make a method getUser?
                    posts.add(new Post(postId, postText, formattedPostDate, postUser, heartsCount, commentsCount, isHearted, isBookmarked));
                }
            }
        } catch (SQLException | ParseException e) {
            // Handle SQL exception
            e.printStackTrace();
        }

        return posts;
    }

    /**
     * Retrieves the posts of a user identified by their user ID.
     *
     * @param userId The user ID of the user whose posts are being retrieved.
     * @param userService An instance of the UserService class, used to fetch user details.
     * @return A list of Post objects representing the posts of the specified user.
     */
    public List<Post> getUserIdPosts(String userId, UserService userService) {
        List<Post> posts = new ArrayList<>();

        String sql = "SELECT * FROM post WHERE userId = ? ORDER BY postDate DESC";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    String postId = rs.getString("postId");
                    String postDateStr = rs.getString("postDate");
                    String postText = rs.getString("postText");
                    User postUser = userService.getUser(userId);
                    Boolean isBookmarked = isBookmarked(postId, userService.getLoggedInUser());
                    Boolean isHearted = isHearted(postId, userService.getLoggedInUser());
                    int heartsCount = getHeartsCount(postId);
                    int commentsCount = getCommentsCount(postId);

                    SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    SimpleDateFormat outputFormat = new SimpleDateFormat("MMM dd, yyyy, hh:mm a");
                    Date postDate = inputFormat.parse(postDateStr); 
                    String formattedPostDate = outputFormat.format(postDate);
                    
                    posts.add(new Post(postId, postText, formattedPostDate, postUser, heartsCount, commentsCount, isHearted, isBookmarked));
                }
            }
        } catch (SQLException | ParseException e) {
            e.printStackTrace();
        }
        return posts;
    }
    
    /**
     * Retrieves an expanded version of posts, including associated comments for each post.
     * 
     * @param currentSessionUser The user currently logged in, for whom the posts are being retrieved.
     * @param userService An instance of the UserService class, used to fetch user details and comments.
     * @return A list of ExpandedPost objects representing the posts, each containing associated comments.
     */ 
    public List<ExpandedPost> getExpandedPosts(User currentSessionUser, UserService userService) {
        List<ExpandedPost> expandedPosts = new ArrayList<>();
    
        String sql = "SELECT * FROM post";
    
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
    
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    String postId = rs.getString("postId");
                    String postDateStr = rs.getString("postDate");
                    String postText = rs.getString("postText");
                    Boolean isBookmarked = isBookmarked(postId, currentSessionUser);
                    Boolean isHearted = isHearted(postId, currentSessionUser);
                    int heartsCount = getHeartsCount(postId);
                    List<Comment> comments = getComments(postId, userService);
                    int commentsCount = getCommentsCount(postId);
    
                    SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    SimpleDateFormat outputFormat = new SimpleDateFormat("MMM dd, yyyy, hh:mm a");
                    Date postDate = inputFormat.parse(postDateStr);
                    String formattedPostDate = outputFormat.format(postDate);
    
                    // Fetch user details from UserService
                    User postUser = userService.getUser(rs.getString("userId"));
    
                    expandedPosts.add(new ExpandedPost(postId, postText, formattedPostDate, postUser, heartsCount, commentsCount, isHearted, isBookmarked, comments));
                }
            }
        } catch (SQLException | ParseException e) {
            e.printStackTrace();
        }
    
        return expandedPosts;
    }

    /**
     * Creates a new comment for a specified post.
     * 
     * @param postId The ID of the post for which the comment is being added.
     * @param commentText The text content of the comment.
     * @param currentUser The user who is adding the comment.
     * @return true if the comment is successfully added, false otherwise.
     */
    public boolean addComment(String postId, String commentText, User currentUser) {
        String sql = "INSERT INTO comment (postId, commentDate, commentText, userId) VALUES (?, CURRENT_TIMESTAMP, ?, ?)";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
    
            pstmt.setString(1, postId);
            pstmt.setString(2, commentText);
            pstmt.setString(3, currentUser.getUserId());
    
            int rowsAffected = pstmt.executeUpdate();
    
            if (rowsAffected > 0) {
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    /**
     * Retrieves the count of comments for a specified post.
     *
     * @param postId The ID of the post for which the comment count is being retrieved.
     * @return The count of comments associated with the specified post.
     */
    public int getCommentsCount(String postId) {
        int commentsCount = 0;
        String sql = "SELECT COUNT(*) AS commentsCount FROM comment WHERE postId = ?";
    
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, postId);
    
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    commentsCount = rs.getInt("commentsCount");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    
        return commentsCount;
    }
    
    /**
     * Retrieves comments associated with a specific post.
     * 
     * @param postId The ID of the post for which to retrieve comments.
     * @param userService An instance of the UserService to fetch user details.
     * @return A list of comments associated with the specified post.
     */
    public List<Comment> getComments(String postId, UserService userService) {
        List<Comment> comments = new ArrayList<>();
        String sql = "SELECT c.commentId, c.commentText, c.commentDate, c.userId, u.username " +
                    "FROM comment c JOIN user u ON c.userId = u.userId " +
                    "WHERE c.postId = ? ORDER BY c.commentDate ASC";

        try (Connection conn = dataSource.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, postId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    String commentId = rs.getString("commentId");
                    String commentText = rs.getString("commentText");
                    String commentDateStr = rs.getString("commentDate");
                    String userId = rs.getString("userId");
                    String username = rs.getString("username");

                    // Fetch user details from UserService
                    User commentUser = userService.getUser(userId);

                    // If user details are not found, create a placeholder user
                    if (commentUser == null) {
                        commentUser = new User(userId, "Unknown", "User");
                    }

                    // Parse comment date
                    SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    SimpleDateFormat outputFormat = new SimpleDateFormat("MMM dd, yyyy, hh:mm a");
                    Date commentDate = inputFormat.parse(commentDateStr);
                    String formattedCommentDate = outputFormat.format(commentDate);

                    // Create a Comment object and add it to the list
                    comments.add(new Comment(commentId, commentText, formattedCommentDate, commentUser));
                }
            }
        } catch (SQLException | ParseException e) {
            e.printStackTrace();
        }
        return comments;
    }
    
    /**
     * Sets the comment count on a post by updating the current count with a given update value.
     * 
     * @param currentCommentsCount The current comment count on the post.
     * @param commentsUpdate The value by which the current comment count is updated.
     * @return The updated comment count after applying the update.
     */    
    public int setCommentsCount(int currentCommentsCount, int commentsUpdate) {
        return currentCommentsCount + commentsUpdate;
    }
    

    /**
     * Retrieves posts from users that the current logged-in user follows, sorted by post date from most recent to oldest.
     * 
     * @param currentSessionUser The user currently logged in, whose followed users' posts are being retrieved.
     * @param userService An instance of the UserService class, used to fetch user details.
     * @return A list of Post objects representing the posts from followed users, sorted by post date.
     */
    public List<Post> getFollowingPosts(User currentSessionUser, UserService userService) {
        List<Post> posts = new ArrayList<>();

        // SQL query to retrieve posts from users followed by the current user, ordered by post date
        String sql = "SELECT p.postId, p.userId, p.postDate, p.postText FROM post p " +
                    "JOIN follow f ON p.userId = f.followeeUserId " +
                    "WHERE f.followerUserId = ? " +
                    "ORDER BY p.postDate DESC";

        try (Connection conn = dataSource.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, currentSessionUser.getUserId());

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    String postId = rs.getString("postId");
                    String postDateStr = rs.getString("postDate");
                    String postText = rs.getString("postText");
                    String userId = rs.getString("userId");
                    Boolean isBookmarked = isBookmarked(postId, currentSessionUser);
                    Boolean isHearted = isHearted(postId, currentSessionUser);
                    int heartsCount = getHeartsCount(postId);
                    int commentsCount = getCommentsCount(postId);
                    User postUser = userService.getUser(userId);

                    SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    SimpleDateFormat outputFormat = new SimpleDateFormat("MMM dd, yyyy, hh:mm a");
                    Date postDate = inputFormat.parse(postDateStr);
                    String formattedPostDate = outputFormat.format(postDate);

                    posts.add(new Post(postId, postText, formattedPostDate, postUser, heartsCount, commentsCount, isHearted, isBookmarked));
                }
            }
        } catch (SQLException | ParseException e) {
            e.printStackTrace();
        }

        return posts;
    } 

    
    /**
     * Retrieves a list of bookmarked posts from the given list of posts.
     * 
     * @param posts The list of posts from which bookmarked posts are to be retrieved.
     * @return A list of Post objects representing the bookmarked posts.
     */ 
    public List<Post> getBookmarkedPosts (List<Post> posts) {
        List<Post> bookmarkedPosts = new ArrayList<>();
        
        for (Post post : posts) {
            if (post.isBookmarked()) {
                bookmarkedPosts.add(post);
            }
        }    
        return bookmarkedPosts;
    }


    /**
     * checks if a post is bookmarked by currentSessionUser
     */
    public boolean isBookmarked(String postId, User currentSessionUser) {
        // Write an SQL query to add new post to list.
        String sql = "SELECT * FROM bookmark where postId = ? AND userId = ?";
        
        try (Connection conn = dataSource.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {       
                // Set the values for the parameters in the SQL query
                pstmt.setString(1, postId);
                pstmt.setString(2, currentSessionUser.getUserId());

            try (ResultSet rs = pstmt.executeQuery()) {
                // Traverse the result rows one at a time.
                // if a result exists this mean th euser bookmarked the post
                while (rs.next()) {
                  return true;
                }
            }
        } catch (SQLException e) {
            // Handle SQL exception
            e.printStackTrace();
        }
        
        return false;
    }

    /**
     * Checks if a post is bookmarked by the specified user.
     * 
     * @param postId The ID of the post to check for bookmarking.
     * @param currentSessionUser The user whose bookmark status is being checked.
     * @return true if the post is bookmarked by the user, false otherwise.
     */
    public boolean addBookmark(String postId, User currentSessionUser) {
        // Write an SQL query to add new post to list.
        String sql = "INSERT INTO bookmark (postId, userId) VALUES (?, ?)";
        
        try (Connection conn = dataSource.getConnection();
        PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // Following line replaces the first place holder with userIdToExclude.
            pstmt.setString(1, postId);
            pstmt.setString(2, currentSessionUser.getUserId());        

            // Execute the SQL query to insert the new post
            int rowsAffected = pstmt.executeUpdate();
            
            //if at least one row was affected, operation was sucessful
            if (rowsAffected > 0) {
                return true;
            }
        } catch (SQLException e) {
            // Handle SQL exception
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Removes the bookmark of a post from the database for the specified user.
     * 
     * @param postId The ID of the post from which the bookmark is to be removed.
     * @param currentSessionUser The user whose bookmark is being removed.
     * @return true if the bookmark was successfully removed, false otherwise.
     */ 
    public boolean removeBookmark(String postId, User currentSessionUser) {
        // Write an SQL query to add new post to list.
        String sql = "DELETE FROM bookmark WHERE postId = ? AND userId = ?";

        try (Connection conn = dataSource.getConnection();
        PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // Following line replaces the first place holder with userIdToExclude.
            pstmt.setString(1, postId);
            pstmt.setString(2, currentSessionUser.getUserId());        

            // Execute the SQL query to insert the new post
            int rowsAffected = pstmt.executeUpdate();
            
            //if at least one row was affected, operation was sucessful
            if (rowsAffected > 0) {
                return true;
            }
        } catch (SQLException e) {
            // Handle SQL exception
            e.printStackTrace();
        }
        return false;
    }


    /**
     * Checks if a post is hearted by the specified user.
     * 
     * @param postId The ID of the post to check for being hearted.
     * @param currentSessionUser The user whose heart status is being checked.
     * @return true if the post is hearted by the user, false otherwise.
     */
    public boolean isHearted(String postId, User currentSessionUser) {
        // Write an SQL query to add new post to list.
        String sql = "SELECT * FROM heart where postId = ? AND userId = ?";
        
        try (Connection conn = dataSource.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {       
                // Set the values for the parameters in the SQL query
                pstmt.setString(1, postId);
                pstmt.setString(2, currentSessionUser.getUserId());

            try (ResultSet rs = pstmt.executeQuery()) {
                // Traverse the result rows one at a time.
                // if a result exists this mean the user hearted the post
                while (rs.next()) {
                  return true;
                }
            }
        } catch (SQLException e) {
            // Handle SQL exception
            e.printStackTrace();
        }
        
        return false;        
    }


    /**
     * adds the hearted/liked post to the database
     * returns true if heart was added/post was liked, else returns false
     */ 
    public boolean addHeart(String postId, User currentSessionUser) {
        // Write an SQL query to add new post to list.
        String sql = "INSERT INTO heart (postId, userId) VALUES (?, ?)";
        
        try (Connection conn = dataSource.getConnection();
        PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // Following line replaces the first place holder with userIdToExclude.
            pstmt.setString(1, postId);
            pstmt.setString(2, currentSessionUser.getUserId());        

            // Execute the SQL query to insert the new post
            int rowsAffected = pstmt.executeUpdate();
            
            //if at least one row was affected, operation was sucessful
            if (rowsAffected > 0) {
                setHeartsCount(getHeartsCount(postId), 1);
                return true;
            }
        } catch (SQLException e) {
            // Handle SQL exception
            e.printStackTrace();
        }

        return false;
    }
    
    /**
     * Adds a heart/like to the specified post in the database.
     * 
     * @param postId The ID of the post to which the heart is being added.
     * @param currentSessionUser The user who is adding the heart to the post.
     * @return true if the heart was successfully added (post was liked), false otherwise.
     */
    public boolean removeHeart(String postId, User currentSessionUser) {
        // Write an SQL query to add new post to list.
        String sql = "DELETE FROM heart WHERE postId = ? AND userId = ?";

        try (Connection conn = dataSource.getConnection();
        PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // Following line replaces the first place holder with userIdToExclude.
            pstmt.setString(1, postId);
            pstmt.setString(2, currentSessionUser.getUserId());        

            // Execute the SQL query to insert the new post
            int rowsAffected = pstmt.executeUpdate();
            
            //if at least one row was affected, operation was sucessful
            if (rowsAffected > 0) {
                setHeartsCount(getHeartsCount(postId), -1);
                return true;
            }
        } catch (SQLException e) {
            // Handle SQL exception
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Sets the heart count on a post by updating the current count with a given update value.
     * 
     * @param currentHeartsCount The current heart count on the post.
     * @param heartsUpdate The value by which the current heart count is updated.
     * @return The updated heart count after applying the update.
     */    
    public int setHeartsCount(int currentHeartsCount, int heartsUpdate) {
        return currentHeartsCount + heartsUpdate;
    }

    /**
     * Retrieves the current count of hearts on a post.
     * 
     * @param postId The ID of the post for which the heart count is being retrieved.
     * @return The current count of hearts associated with the specified post.
     */
    public int getHeartsCount(String postId) {
        int heartsCount = 0;

        // Write an SQL query to add new post to list.
        String sql = "SELECT COUNT(*) AS heartsCount FROM heart WHERE postId = ?";
        
        try (Connection conn = dataSource.getConnection();
        PreparedStatement pstmt = conn.prepareStatement(sql)) {       
        // Following line replaces the first place holder with userIdToExclude.
        pstmt.setString(1, postId);

            try (ResultSet rs = pstmt.executeQuery()) {
            // Traverse the result rows one at a time.
                if (rs.next()) {
                    // Note: rs.get.. functions access attributes of the current row.
                    return heartsCount = rs.getInt("heartsCount");
                }
            }
        } catch (SQLException e) {
            // Handle SQL exception
            e.printStackTrace();
        }

        //returns 0 if there are no hearts for the post :(
        return heartsCount;
    }


    /**
    * Inserts a hashtag associated with a specific post into the hashtag table.
    * 
    * @param postId The ID of the post.
    * @param hashTagText The text of the hashtag to be inserted.
    */
    private void insertHashtag(String postId, String hashTagText) {
        // Checks if the post exists in the post table
        if (!postExists(postId)) {
            System.err.println("Error: Post with postId " + postId + " does not exist.");
            return;
        }
        
        // Inserts the hashtag into the hashtag table
        String sql = "INSERT INTO hashtag (postId, hashTag) VALUES (?, ?)";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, postId);
            pstmt.setString(2, hashTagText);
            
            pstmt.executeUpdate();
            System.out.println("Hashtag '" + hashTagText + "' inserted successfully for postId " + postId);
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    /**
    * Checks if a post with the specified postId exists in the database.
    * 
    * @param postId The ID of the post to check.
    * @return true if the post exists, false otherwise.
    */
    private boolean postExists(String postId) {
        String sql = "SELECT COUNT(*) AS count FROM post WHERE postId = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, postId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    int count = rs.getInt("count");
                    return count > 0;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }


    /**
     * Searches for posts containing a specific hashtag.
     * @param hashtag The hashtag to search for.
     * @return A list of posts containing the specified hashtag.
     */
    public List<Post> searchPostsByHashtag(String hashtag, UserService userService) {
        List<Post> posts = new ArrayList<>();
        
        // SQL query to retrieve posts containing the given hashtag.
        String sql = "SELECT * FROM post WHERE postText LIKE ?";

        
        try (Connection conn = dataSource.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            // Sets the parameter in the SQL query to search for the hashtag
            pstmt.setString(1, "%" + hashtag + "%");
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    // Populate post details from the result set
                    String postId = rs.getString("postId");
                    String postText = rs.getString("postText");
                    String postDateStr = rs.getString("postDate");
                    User postUser = userService.getUser(rs.getString("userId")); 
                    Boolean isBookmarked = isBookmarked(postId, userService.getLoggedInUser()); 
                    Boolean isHearted = isHearted(postId, userService.getLoggedInUser());
                    int heartsCount = getHeartsCount(postId);

                    SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    SimpleDateFormat outputFormat = new SimpleDateFormat("MMM dd, yyyy, hh:mm a");
                    Date postDate = inputFormat.parse(postDateStr);
                    String formattedPostDate = outputFormat.format(postDate);
                    
                    // Creates a Post object and add it to the list
                    posts.add(new Post(postId, postText, formattedPostDate, postUser, heartsCount, 0, isHearted, isBookmarked));
                }
            }
        } catch (SQLException | ParseException e) {
            e.printStackTrace();
        }
        
        return posts;
    }

}
