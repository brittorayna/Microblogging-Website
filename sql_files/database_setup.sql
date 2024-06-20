-- Create the database if it doesn't exist
CREATE DATABASE IF NOT EXISTS cs4370_mb_platform;

-- Use the created database
USE cs4370_mb_platform;

-- Create the user table with constraints and auto_increment for userId
CREATE TABLE IF NOT EXISTS user (
    userId INT AUTO_INCREMENT,
    username VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    firstName VARCHAR(255) NOT NULL,
    lastName VARCHAR(255) NOT NULL,
    PRIMARY KEY (userId),
    CONSTRAINT userName_min_length CHECK (CHAR_LENGTH(TRIM(username)) >= 2),
    CONSTRAINT firstName_min_length CHECK (CHAR_LENGTH(TRIM(firstName)) >= 2),
    CONSTRAINT lastName_min_length CHECK (CHAR_LENGTH(TRIM(lastName)) >= 2)
);

-- Create the post table
CREATE TABLE IF NOT EXISTS post (
    postId INT AUTO_INCREMENT PRIMARY KEY,
    userId INT NOT NULL,
    postDate DATETIME NOT NULL,
    postText TEXT NOT NULL,
    FOREIGN KEY (userId) REFERENCES user(userId)
);

-- Create the comment table
CREATE TABLE IF NOT EXISTS comment (
    commentId INT AUTO_INCREMENT PRIMARY KEY,
    postId INT NOT NULL,
    userId INT NOT NULL,
    commentDate DATETIME NOT NULL,
    commentText TEXT NOT NULL,
    FOREIGN KEY (postId) REFERENCES post(postId),
    FOREIGN KEY (userId) REFERENCES user(userId)
);

-- Create the heart table
CREATE TABLE IF NOT EXISTS heart (
    postId INT,
    userId INT,
    PRIMARY KEY (postId, userId),
    FOREIGN KEY (postId) REFERENCES post(postId),
    FOREIGN KEY (userId) REFERENCES user(userId)
);

-- Create the bookmark table
CREATE TABLE IF NOT EXISTS bookmark (
    postId INT,
    userId INT,
    PRIMARY KEY (postId, userId),
    FOREIGN KEY (postId) REFERENCES post(postId),
    FOREIGN KEY (userId) REFERENCES user(userId)
);

-- Create the hashtag table
CREATE TABLE IF NOT EXISTS hashtag (
    hashTag VARCHAR(255),
    postId INT,
    PRIMARY KEY (hashTag, postId),
    FOREIGN KEY (postId) REFERENCES post(postId)
);

-- Create the follow table
CREATE TABLE IF NOT EXISTS follow (
    followerUserId INT,
    followeeUserId INT,
    PRIMARY KEY (followerUserId, followeeUserId),
    FOREIGN KEY (followerUserId) REFERENCES user(userId),
    FOREIGN KEY (followeeUserId) REFERENCES user(userId)
);

-- Insert Statements.
INSERT INTO user (userId, username, password, firstName, lastName) VALUES (1, 'danand', '$2a$10$eIupKX/NgHShRW8lsmQg4uahoopBBU0WXVTL/CS0NOcUytAs9h30S', 'Diya', 'Anand');
INSERT INTO user (userId, username, password, firstName, lastName) VALUES (2, 'tkungwani', '$2a$10$F1qVwcVCiDCMeahhfyvRhONG//Nvuz6240K3zZ35EJNAM/cWoJ4C6', 'Tia', 'Kungwani');
INSERT INTO user (userId, username, password, firstName, lastName) VALUES (3, 'canand', '$2a$10$Cm.W4iTEzeMOCBSfcgCMm.8AH7vJEJl5gqA7WrknGXTnoCw.oO0Ha', 'Chirag', 'Anand');

INSERT INTO post (postId, userId, postDate, postText) VALUES (81, 1, '2024-03-20 19:40:26', ' hi #wow #ok');
INSERT INTO post (postId, userId, postDate, postText) VALUES (82, 2, '2024-03-20 19:40:26', 'heyy #new');

INSERT INTO heart (postId, userId) VALUES (81, 1);
INSERT INTO heart (postId, userId) VALUES (82, 1);

INSERT INTO hashtag (hashTag, postId) VALUES ('ok', 81);
INSERT INTO hashtag (hashTag, postId) VALUES ('wow', 81);
INSERT INTO hashtag (hashTag, postId) VALUES ('new', 82);

INSERT INTO bookmark (postId, userId) VALUES (81, 1);
INSERT INTO bookmark (postId, userId) VALUES (82, 1);

INSERT INTO follow (followerUserId, followeeUserId) VALUES (2, 1);
INSERT INTO follow (followerUserId, followeeUserId) VALUES (3, 1);
INSERT INTO follow (followerUserId, followeeUserId) VALUES (1, 2);
INSERT INTO follow (followerUserId, followeeUserId) VALUES (3, 2);

INSERT INTO comment (commentId, postId, userId, commentDate, commentText) VALUES (6, 81, 1, '2024-03-20 19:40:32', 'what lol');
INSERT INTO comment (commentId, postId, userId, commentDate, commentText) VALUES (7, 82, 2, '2024-03-20 19:40:32', 'hii');
INSERT INTO comment (commentId, postId, userId, commentDate, commentText) VALUES (8, 81, 2, '2024-03-20 19:40:32', 'wait this is me oops');
INSERT INTO comment (commentId, postId, userId, commentDate, commentText) VALUES (9, 81, 3, '2024-03-20 19:40:32', 'hi');
INSERT INTO comment (commentId, postId, userId, commentDate, commentText) VALUES (10, 82, 3, '2024-03-20 19:40:32', 'lol');



