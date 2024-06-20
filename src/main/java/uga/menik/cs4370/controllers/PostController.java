/**
Copyright (c) 2024 Sami Menik, PhD. All rights reserved.

This is a project developed by Dr. Menik to give the students an opportunity to apply database concepts learned in the class in a real world project. Permission is granted to host a running version of this software and to use images or videos of this work solely for the purpose of demonstrating the work to potential employers. Any form of reproduction, distribution, or transmission of the software's source code, in part or whole, without the prior written consent of the copyright owner, is strictly prohibited.
*/
package uga.menik.cs4370.controllers;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import uga.menik.cs4370.models.Comment;
import uga.menik.cs4370.models.ExpandedPost;
import uga.menik.cs4370.services.PostService;
import uga.menik.cs4370.services.UserService;
import uga.menik.cs4370.utility.Utility;

/**
 * Handles /post URL and its sub urls.
 */
@Controller
@RequestMapping("/post")
public class PostController {

    // UserService and PostService has user webpage related functions.
    private final UserService userService;
    private final PostService postService;

    /**
     * See notes in AuthInterceptor.java regarding how this works 
     * through dependency injection and inversion of control.
     */
    @Autowired
    public PostController(UserService userService, PostService postService) {
        this.userService = userService;
        this.postService = postService;
    }

    /**
     * This function handles the /post/{postId} URL.
     * This handlers serves the web page for a specific post.
     * Note there is a path variable {postId}.
     * An example URL handled by this function looks like below:
     * http://localhost:8081/post/1
     * The above URL assigns 1 to postId.
     * 
     * See notes from HomeController.java regardig error URL parameter.
     *
     * @param postId The ID of the post to display.
     * @param error  An optional error message to display to the user (query parameter).
     * @return ModelAndView object representing the posts_page template with post details and comments.
     */
    @GetMapping("/{postId}")
    public ModelAndView webpage(@PathVariable("postId") String postId,
            @RequestParam(name = "error", required = false) String error) {
        System.out.println("The user is attempting to view post with id: " + postId);
        // See notes on ModelAndView in BookmarksController.java.
        ModelAndView mv = new ModelAndView("posts_page");

        // Following line populates sample data.
        // You should replace it with actual data from the database.
        //List<ExpandedPost> posts = Utility.createSampleExpandedPostWithComments();
        
        //temp comment list for testing REMOVE LATER
        //List<Comment> comments = postService.getComments(userService.getLoggedInUser().g);        
        List<ExpandedPost> expandedPosts = postService.getExpandedPosts(userService.getLoggedInUser(), userService);
        mv.addObject("posts", expandedPosts);

        // If an error occured, you can set the following property with the
        // error message to show the error message to the user.
        // An error message can be optionally specified with a url query parameter too.
        //String errorMessage = error;
        //mv.addObject("errorMessage", errorMessage);

        // Enable the following line if you want to show no content message.
        // Do that if your content list is empty.
        // mv.addObject("isNoContent", true);

        return mv;
    }

    /**
     * Handles comments added on posts.
     * See comments on webpage function to see how path variables work here.
     * This function handles form posts.
     * See comments in HomeController.java regarding form submissions.
     *
     * @param postId      The ID of the post to add the comment to.
     * @param commentText The text content of the comment.
     * @return Redirects the user to the same post page after adding the comment.
     */
    @PostMapping("/{postId}/comment")
    public String postComment(@PathVariable("postId") String postId,
        @RequestParam(name = "comment") String comment) {
    System.out.println("The user is attempting to add a comment:");
    System.out.println("\tpostId: " + postId);
    System.out.println("\tcomment: " + comment);

    postService.addComment(postId, comment, userService.getLoggedInUser());

    //redirect back to the same post page after adding the comment
    return "redirect:/post/" + postId;
}


    /**
     * Handles likes added on posts.
     * See comments on webpage function to see how path variables work here.
     * See comments in PeopleController.java in followUnfollowUser function regarding 
     * get type form submissions and how path variables work.
     *
     * @param postId The ID of the post to add or remove the heart from.
     * @param isAdd  A boolean indicating whether to add (true) or remove (false) the heart.
     * @return Redirects the user to the home page after the action is performed.
     */
    @GetMapping("/{postId}/heart/{isAdd}")
    public String addOrRemoveHeart(@PathVariable("postId") String postId,
            @PathVariable("isAdd") Boolean isAdd) {
        System.out.println("The user is attempting add or remove a heart:");
        System.out.println("\tpostId: " + postId);
        System.out.println("\tisAdd: " + isAdd);

        //adds or removes heart based on the status of isAdd
        if (isAdd) {
            postService.addHeart(postId, userService.getLoggedInUser());
        } else {
            postService.removeHeart(postId, userService.getLoggedInUser());
        }
        
        //checks if heart was added/removed successfully, else prints error message
        if (postService.isHearted(postId, userService.getLoggedInUser()) == true
            || postService.isHearted(postId, userService.getLoggedInUser()) == false) {
            return "redirect:/";
        
        } else {
            //redirect user with an error message if there was an error.
            String message = URLEncoder.encode("Failed to (un)like the post. Please try again.",
                StandardCharsets.UTF_8);
            return "redirect:/?error=" + message;
        }

    }

    /**
     * Handles bookmarking posts.
     * See comments on webpage function to see how path variables work here.
     * See comments in PeopleController.java in followUnfollowUser function regarding 
     * get type form submissions.
     *
     * @param postId The ID of the post to add or remove the bookmark from.
     * @param isAdd  A boolean indicating whether to add (true) or remove (false) the bookmark.
     * @return Redirects the user to the home page after the action is performed.
     */
    @GetMapping("/{postId}/bookmark/{isAdd}")
    public String addOrRemoveBookmark(@PathVariable("postId") String postId,
            @PathVariable("isAdd") Boolean isAdd) {
        System.out.println("The user is attempting add or remove a bookmark:");
        System.out.println("\tpostId: " + postId);
        System.out.println("\tisAdd: " + isAdd);

        //adds or removes bookmark depending on status of isAdd
        if (isAdd) {
            postService.addBookmark(postId, userService.getLoggedInUser());
        } else {
            postService.removeBookmark(postId, userService.getLoggedInUser());
        }

        //checks if bookmark was added/removed successfully, else prints error message
        if (postService.isBookmarked(postId, userService.getLoggedInUser()) == true
            || postService.isBookmarked(postId, userService.getLoggedInUser()) == false) {
            return "redirect:/";
            //return "redirect:/post/" + postId; 
            //feels unecessary to redirect user to post everytime they bookmark/unbookmark it
        
        } else {
            //redirect user with an error message if there was an error.
            String message = URLEncoder.encode("Failed to (un)bookmark the post. Please try again.",
                    StandardCharsets.UTF_8);
            return "redirect:/?error=" + message;
        }
        
    }

}
