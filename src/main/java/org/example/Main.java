package org.example;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        UserService userService = new UserService();

        User user = new User();
        user.setName("Oleg");
        User createdUser = userService.createUser(user);
        System.out.println("Created user: " + createdUser);

        createdUser.setName("John Doe Updated");
        User updatedUser = userService.updateUser(createdUser);
        System.out.println("Updated user: " + updatedUser);

        List<User> allUsers = userService.getAllUsers();
        System.out.println("All users: " + allUsers);


        long userIdToGet = 1;
        User userById = userService.getUserById(userIdToGet);
        System.out.println("User by ID " + userIdToGet + ": " + userById);


        String usernameToGet = "john.doe";
        User userByUsername = userService.getUserByUsername(usernameToGet);
        System.out.println("User by username " + usernameToGet + ": " + userByUsername);


        long userIdForComments = 1;
        userService.getAndSaveCommentsForLastPost(userIdForComments);
    }
}