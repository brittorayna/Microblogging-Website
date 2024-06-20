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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import uga.menik.cs4370.models.FollowableUser;
import uga.menik.cs4370.services.PeopleService;
import uga.menik.cs4370.services.UserService;
import uga.menik.cs4370.utility.Utility;
import uga.menik.cs4370.models.User;

import org.springframework.web.bind.annotation.PostMapping;


/**
 * Handles /people URL and its sub URL paths.
 */
@Controller
@RequestMapping("/people")
public class PeopleController {

    // Inject UserService and PeopleService instances.
    // See LoginController.java to see how to do this.
    // Hint: Add a constructor with @Autowired annotation.
    
    // UserService and PeopleService has user webpage related functions.
    private final UserService userService;
    private final PeopleService peopleService;
    /**
     * See notes in AuthInterceptor.java regarding how this works 
     * through dependency injection and inversion of control.
     */
    @Autowired
    public PeopleController(UserService userService, PeopleService peopleService) {
        this.userService = userService;
        this.peopleService = peopleService;
    }



    /**
     * Serves the /people web page.
     * 
     * Note that this accepts a URL parameter called error.
     * The value to this parameter can be shown to the user as an error message.
     * See notes in HashtagSearchController.java regarding URL parameters.
     *
     * @param error An optional error message to display to the user (query parameter).
     * @return ModelAndView object representing the people_page template with followable users and error message if present.
     */
    @GetMapping
    public ModelAndView webpage(@RequestParam(name = "error", required = false) String error) {
        // See notes on ModelAndView in BookmarksController.java.
        ModelAndView mv = new ModelAndView("people_page");

        // Following line populates sample data.
        // You should replace it with actual data from the database.
        // Use the PeopleService instance to find followable users.
        // Use UserService to access logged in userId to exclude.
        
        String userIdToExclude = userService.getLoggedInUser().getUserId();

        List<FollowableUser> followableUsers = peopleService.getFollowableUsers(userIdToExclude);
        mv.addObject("users", followableUsers);

        
        // If an error occured, you can set the following property with the
        // error message to show the error message to the user.
        // An error message can be optionally specified with a url query parameter too.
        String errorMessage = error;
        mv.addObject("errorMessage", errorMessage);

        // Enable the following line if you want to show no content message.
        // Do that if your content list is empty.
        // mv.addObject("isNoContent", true);
        
        return mv;
    }

    /**
     * This function handles user follow and unfollow.
     * Note the URL has parameters defined as variables ie: {userId} and {isFollow}.
     * Follow and unfollow is handled by submitting a get type form to this URL 
     * by specifing the userId and the isFollow variables.
     * Learn more here: https://www.w3schools.com/tags/att_form_method.asp
     * An example URL that is handled by this function looks like below:
     * http://localhost:8081/people/1/follow/false
     * The above URL assigns 1 to userId and false to isFollow.
     *
     * @param userId  The ID of the user to follow or unfollow.
     * @param isFollow A boolean indicating whether to follow (true) or unfollow (false) the user.
     * @return Redirects the user to the people page after the action is performed.
     */
    @GetMapping("{userId}/follow/{isFollow}")
    public String followUnfollowUser(@PathVariable("userId") String userId,
            @PathVariable("isFollow") Boolean isFollow) {
        System.out.println("User is attempting to follow/unfollow a user:");
        System.out.println("\tuserId: " + userId);
        System.out.println("\tisFollow: " + isFollow);

        //gets the current session user
        String userIdToExclude = userService.getLoggedInUser().getUserId();
        
        // Redirect the user with an error message if there was an error.
        String message = URLEncoder.encode("Failed to (un)follow the user. Please try again.",
        StandardCharsets.UTF_8);

        if (isFollow) {
            // user clicked on the "follow button"
            System.out.println("User clicked on the Follow button.");
            //calls followUser() from PeopleService to handle the follow action
            peopleService.followUser(userIdToExclude, userId);

            // Redirect the user if following is a success.
            return "redirect:/people";

        } else if (isFollow == false) {
            // user clicked on the "unfollow button"
            System.out.println("User clicked on the Unfollow button.");
            // calls unfollowUser() from PeopleService to handle the unfollow action
            peopleService.unfollowUser(userIdToExclude, userId);
            
            // Redirect the user if unfollowing is a success.
            return "redirect:/people";

        } else {
            // Redirect the user with an error message if there was an error.
            message = URLEncoder.encode("Failed to (un)follow the user. Please try again.",
            StandardCharsets.UTF_8);
            return "redirect:/people?error=" + message;
        }
 
    }

    /**
     * Handles comment submission on user profiles.
     * Users can submit comments on profiles via a POST request.
     *
     * @param userId      The ID of the user profile being commented on.
     * @param postId      The ID of the post being commented on (if applicable).
     * @param commentText The text content of the comment.
     * @return Redirects the user to the people page after the comment is submitted.
     */
    @PostMapping("/comment")
    public String submitComment(@RequestParam("userId") String userId,
                                @RequestParam("postId") String postId,
                                @RequestParam("commentText") String commentText) {
        User currentUser = userService.getLoggedInUser();
        return "redirect:/people";
    }

}
