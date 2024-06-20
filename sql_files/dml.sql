-- Retrieves all posts that contain a certain hashtag
-- used to filter posts by hashtag
-- http://localhost:8080/hashtagsearch?hashtags=Hello
SELECT p.* FROM post p INNER JOIN hashtag h ON p.postId = h.postId WHERE h.hashTag = ?

-- Getting All Users that do not have a specific userID
-- used to get the list of all followable users
-- http://localhost:8080/people
SELECT * FROM user where userId <> ?

-- Inserting a new row into the follow table
-- used when the user follows someone
-- http://localhost:8080/people
INSERT INTO follow (followerUserId, followeeUserId) VALUES (?, ?)

-- Removing a row from the follow table
-- used when the user unfollows someone
-- http://localhost:8080/people
DELETE FROM follow WHERE followerUserId = ? AND followeeUserId = ?

-- Inserting a new post into the post table
-- used when the user creats a post
-- http://localhost:8080/people
INSERT INTO post (userId, postDate, postText) VALUES (?, CURRENT_TIMESTAMP, ?)

-- Inserts a new row into the hashtag table
-- used to associate a post with a hashtag
-- http://localhost:8080/
INSERT INTO hashtag (postId, hashTag) VALUES (?, ?)

-- Counts the number of posts that have a certain postID
-- used to check if a post with a certain postId exists
-- http://localhost:8080/
SELECT COUNT(*) AS count FROM post WHERE postId = ?

-- Gets a list of all posts in a descending order
-- used to get all of the posts to then further get the bookmarked posts
-- http://localhost:8080/bookmarks
SELECT * FROM post ORDER BY postDate DESC

-- Gets the posts of a user by userId
-- used to display the posts of a specific user
-- http://localhost:8080/profile/{userId}
SELECT * FROM post WHERE userId = ? ORDER BY postDate DESC

-- Gets a list of all posts
-- used to get all of the posts and adds the list of comments associated with that post
-- http://localhost:8080/post/{postId}
SELECT * FROM post

-- Gets a list of posts from a certain user in descending order
-- used when looking at a profile of a user
-- http://localhost:8080/
SELECT * FROM post WHERE userId = ? ORDER BY postDate DESC

-- Selects posts that are from users that the currently logged in user follows
-- used when loading the home page
-- http://localhost:8080/
SELECT * FROM post WHERE userId IN (SELECT followeeUserId FROM follow WHERE followerUserId = ?) ORDER BY postDate DESC

-- Selects a post from bookmarked if the post is bookmarked by the user
-- used to check if a post is bookmarked by a user
-- http://localhost:8080/bookmarks
SELECT * FROM bookmark where postId = ? AND userId = ?

-- Inserts a new now into the bookmark table
-- used when a user bookmarks a post
-- http://localhost:8080/
INSERT INTO bookmark (postId, userId) VALUES (?, ?)

-- Removes a row from the bookmark table
-- used when a user unbookmarks a post
-- http://localhost:8080/
DELETE FROM bookmark WHERE postId = ? AND userId = ?

-- Selects the rows from heart by specific postId and userId
-- used when loading posts
-- http://localhost:8080/
SELECT * FROM heart where postId = ? AND userId = ?

-- Inserts row into the heart table
-- used to add a post to the user's liked posts
-- http://localhost:8080/
INSERT INTO heart (postId, userId) VALUES (?, ?)

-- Deletes row from the heart table
-- used to remove a post from the user's liked posts
-- http://localhost:8080/
DELETE FROM heart WHERE postId = ? AND userId = ?

-- Returns the number of hearts the post has 
-- used to display the amount of likes of a certain post
-- http://localhost:8080/
SELECT COUNT(*) AS heartsCount FROM heart WHERE postId = ?

-- Selects a user from the user table by username
-- used when authenticating the user
-- http://localhost:8080/login
select * from user where username = ?

-- Selects posts based on the post text
-- used to select posts based on the hashtag
-- http://localhost:8080/
SELECT * FROM post WHERE postText LIKE ?

-- Selects a user from the user table by userId
-- used when looking at the profile of any user
-- http://localhost:8080/profile/{userId}
SELECT * FROM user WHERE userId = ?
