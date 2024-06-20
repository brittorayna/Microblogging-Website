/**
 * Copyright (c) 2024 Sami Menik, PhD. All rights reserved.
 *
 * This is a project developed by Dr. Menik to give the students an opportunity to apply database concepts learned in the class in a real world project.
 * Permission is granted to host a running version of this software and to use images or videos of this work solely for the purpose of demonstrating the work to potential employers.
 * Any form of reproduction, distribution, or transmission of the software's source code, in part or whole, without the prior written consent of the copyright owner, is strictly prohibited.
 */
package uga.menik.cs4370.services;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import uga.menik.cs4370.models.Post;
import uga.menik.cs4370.models.User;

/**
 * This service contains methods related to searching posts by hashtags.
 */
@Service
public class HashtagService {

   private final DataSource dataSource;
   private final UserService userService;

   @Autowired
   public HashtagService(DataSource dataSource, UserService userService) {
       this.dataSource = dataSource;
       this.userService = userService;
   }

/**
 * Searches for posts containing one or more specified hashtags.
 *
 * @param hashtags The hashtags to search for (separated by spaces).
 * @return A list of posts containing the specified hashtags.
 */
public List<Post> searchPostsByHashtag(String hashtags) {
       List<Post> posts = new ArrayList<>();

       // Splits the input string containing hashtags by space
       String[] hashtagArray = hashtags.split("\\s+");
       
       // Writes an SQL query to retrieve posts containing the given hashtags
       String sql = "SELECT p.* FROM post p INNER JOIN hashtag h ON p.postId = h.postId WHERE h.hashTag IN (";
       
       // Appends placeholders for each hashtag
       for (int i = 0; i < hashtagArray.length; i++) {
           sql += "?";
           if (i < hashtagArray.length - 1) {
               sql += ",";
           }
       }
       sql += ")";
       
       try (Connection conn = dataSource.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {
           
           // Sets the hashtags as parameters in the SQL query
           for (int i = 0; i < hashtagArray.length; i++) {
               pstmt.setString(i + 1, hashtagArray[i]);
           }
           
           try (ResultSet rs = pstmt.executeQuery()) {
               while (rs.next()) {
                   String postId = rs.getString("postId");
                   String userId = rs.getString("userId");
                   String postDateStr = rs.getString("postDate");
                   String postText = rs.getString("postText");
                   User postUser = userService.getUser(userId);
                   
                   SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                   SimpleDateFormat outputFormat = new SimpleDateFormat("MMM dd, yyyy, hh:mm a");
                   Date postDate = inputFormat.parse(postDateStr);
                   String formattedPostDate = outputFormat.format(postDate);
                   
                   posts.add(new Post(postId, postText, formattedPostDate, postUser, 0, 0, false, false));
               }
           }
       } catch (SQLException | ParseException e) {
           e.printStackTrace();
       }
       
       return posts;
   }
}
