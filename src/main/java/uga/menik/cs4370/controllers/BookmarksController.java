/**
Copyright (c) 2024 Sami Menik, PhD. All rights reserved.

This is a project developed by Dr. Menik to give the students an opportunity to apply database concepts learned in the class in a real world project. Permission is granted to host a running version of this software and to use images or videos of this work solely for the purpose of demonstrating the work to potential employers. Any form of reproduction, distribution, or transmission of the software's source code, in part or whole, without the prior written consent of the copyright owner, is strictly prohibited.
*/
package uga.menik.cs4370.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import uga.menik.cs4370.models.Post;
import uga.menik.cs4370.models.User;
import uga.menik.cs4370.services.PostService;
import uga.menik.cs4370.services.UserService;
import uga.menik.cs4370.utility.Utility;

/**
 * Handles /bookmarks and its sub URLs.
 * No other URLs at this point.
 * 
 * Learn more about @Controller here: 
 * https://docs.spring.io/spring-framework/reference/web/webmvc/mvc-controller.html
 */
@Controller
@RequestMapping("/bookmarks")
public class BookmarksController {

    // UserService and PostService has user webpage related functions.
    private final UserService userService;
    private final PostService postService;

    /**
     * See notes in AuthInterceptor.java regarding how this works 
     * through dependency injection and inversion of control.
     */
    @Autowired
    public BookmarksController(UserService userService, PostService postService) {
        this.userService = userService;
        this.postService = postService;
    }

    /**
     * Handles requests to the /bookmarks URL.
     *
     * @return ModelAndView object representing the posts_page template with bookmarked posts data.
     */
    @GetMapping
    public ModelAndView webpage() {
        ModelAndView mv = new ModelAndView("posts_page");

        // Gets the logged-in user
        User loggedInUser = userService.getLoggedInUser();

        // Gets all posts from the database
        List<Post> allPosts = postService.getPosts(loggedInUser, userService);

        // Gets bookmarked posts for the logged-in user
        List<Post> bookmarkedPosts = postService.getBookmarkedPosts(allPosts);

        // Adds the bookmarked posts to the model
        mv.addObject("posts", bookmarkedPosts);

        // If there aren't bookmarked posts, indicate no content
        if (bookmarkedPosts.isEmpty()){
            mv.addObject("isNoContent", true);
        }

        return mv;
    }
}

//orinal comments on file:
 // If an error occured, you can set the following property with the
        // error message to show the error message to the user.
        // String errorMessage = "Some error occured!";
        // mv.addObject("errorMessage", errorMessage);
        // Enable the following line if you want to show no content message.
        // Do that if your content list is empty.
// Following line populates sample data.
        // You should replace it with actual data from the database.
        //List<Post> posts = Utility.createSamplePostsListWithoutComments();
// posts_page is a mustache template from src/main/resources/templates.
        // ModelAndView class enables initializing one and populating placeholders
        // in the template using Java objects assigned to named properties.
