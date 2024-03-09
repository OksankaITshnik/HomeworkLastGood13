package org.example;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import java.io.File;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Comparator;
import java.util.List;

public class UserService {

    private static final String BASE_URL = "https://jsonplaceholder.typicode.com";
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public UserService() {
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper();
    }

    @SneakyThrows
    public User createUser(User user) {
        String createUserJson = objectMapper.writeValueAsString(user);
        HttpRequest createUserRequest = HttpRequest.newBuilder(new URI(BASE_URL + "/users"))
                .POST(HttpRequest.BodyPublishers.ofString(createUserJson))
                .header("Accept", "application/json")
                .header("Content-type", "application/json")
                .build();

        HttpResponse<String> createUserResponse = httpClient.send(createUserRequest, HttpResponse.BodyHandlers.ofString());

        if (createUserResponse.statusCode() == 201) {
            return objectMapper.readValue(createUserResponse.body(), User.class);
        } else {
            throw new RuntimeException("Failed to create user. Status code: " + createUserResponse.statusCode());
        }
    }

    @SneakyThrows
    public User updateUser(User user) {
        String updateUserJson = objectMapper.writeValueAsString(user);
        user.setId(1);
        HttpRequest updateUserRequest = HttpRequest.newBuilder(new URI(BASE_URL + "/users/" + user.getId()))
                .PUT(HttpRequest.BodyPublishers.ofString(updateUserJson))
                .header("Accept", "application/json")
                .header("Content-type", "application/json")
                .build();

        HttpResponse<String> updateUserResponse = httpClient.send(updateUserRequest, HttpResponse.BodyHandlers.ofString());

        if (updateUserResponse.statusCode() == 200) {
            return objectMapper.readValue(updateUserResponse.body(), User.class);
        } else {
            throw new RuntimeException("Failed to update user. Status code: " + updateUserResponse.statusCode());
        }
    }
    @SneakyThrows
    public void deleteUser(long userId) {
        HttpRequest deleteUserRequest = HttpRequest.newBuilder(new URI(BASE_URL + "/users/" + userId))
                .DELETE()
                .build();

        HttpResponse<Void> deleteUserResponse = httpClient.send(deleteUserRequest, HttpResponse.BodyHandlers.discarding());

        if (deleteUserResponse.statusCode() / 100 == 2) {
            System.out.println("User deleted successfully.");
        } else {
            throw new RuntimeException("Failed to delete user. Status code: " + deleteUserResponse.statusCode());
        }
    }


    @SneakyThrows
    public List<User> getAllUsers() throws RuntimeException {
        HttpRequest getAllUsersRequest = HttpRequest.newBuilder(new URI(BASE_URL + "/users"))
                .GET()
                .header("Accept", "application/json")
                .build();

        HttpResponse<String> getAllUsersResponse = httpClient.send(getAllUsersRequest, HttpResponse.BodyHandlers.ofString());

        if (getAllUsersResponse.statusCode() == 200) {
            return objectMapper.readValue(getAllUsersResponse.body(), new TypeReference<List<User>>(){});
        } else {
            throw new RuntimeException("Failed to get all users. Status code: " + getAllUsersResponse.statusCode());
        }
    }

    @SneakyThrows
    public User getUserById(long userId) {
        HttpRequest getUserByIdRequest = HttpRequest.newBuilder(new URI(BASE_URL + "/users/" + userId))
                .GET()
                .header("Accept", "application/json")
                .build();

        HttpResponse<String> getUserByIdResponse = httpClient.send(getUserByIdRequest, HttpResponse.BodyHandlers.ofString());

        if (getUserByIdResponse.statusCode() == 200) {
            return objectMapper.readValue(getUserByIdResponse.body(), User.class);
        } else {
            throw new RuntimeException("Failed to get user by id. Status code: " + getUserByIdResponse.statusCode());
        }
    }

    // Метод для отримання інформації про користувача за username
    @SneakyThrows
    public User getUserByUsername(String username) {
        HttpRequest getUserByUsernameRequest = HttpRequest.newBuilder(new URI(BASE_URL + "/users?username=" + username))
                .GET()
                .header("Accept", "application/json")
                .build();

        HttpResponse<String> getUserByUsernameResponse = httpClient.send(getUserByUsernameRequest, HttpResponse.BodyHandlers.ofString());

        if (getUserByUsernameResponse.statusCode() == 200) {
            List<User> users = objectMapper.readValue(getUserByUsernameResponse.body(), objectMapper.getTypeFactory().constructCollectionType(List.class, User.class));
            return users.isEmpty() ? null : users.get(0);
        } else {
            throw new RuntimeException("Failed to get user by username. Status code: " + getUserByUsernameResponse.statusCode());
        }
    }

    // Метод для отримання всіх коментарів до останнього поста певного користувача та запису їх у файл
    @SneakyThrows
    public void getAndSaveCommentsForLastPost(long userId) {
        // Отримання інформації про останній пост користувача
        HttpRequest getPostsRequest = HttpRequest.newBuilder(new URI(BASE_URL + "/users/" + userId + "/posts"))
                .GET()
                .header("Accept", "application/json")
                .build();

        HttpResponse<String> getPostsResponse = httpClient.send(getPostsRequest, HttpResponse.BodyHandlers.ofString());

        if (getPostsResponse.statusCode() == 200) {
            List<Post> posts = objectMapper.readValue(getPostsResponse.body(), new TypeReference<List<Post>>(){});

            if (!posts.isEmpty()) {
                Post lastPost = posts.stream().max(Comparator.comparing(Post::getId)).orElseThrow();
                long postId = lastPost.getId();

                HttpRequest getCommentsRequest = HttpRequest.newBuilder(new URI(BASE_URL + "/posts/" + postId + "/comments"))
                        .GET()
                        .header("Accept", "application/json")
                        .build();

                HttpResponse<String> getCommentsResponse = httpClient.send(getCommentsRequest, HttpResponse.BodyHandlers.ofString());
                if (getCommentsResponse.statusCode() == 200) {
                    List<Comment> comments = objectMapper.readValue(getCommentsResponse.body(), new TypeReference<List<Comment>>(){});


                    String fileName = "user-" + userId + "-post-" + postId + "-comments.json";
                    objectMapper.writeValue(new File(fileName), comments);

                    System.out.println("Comments saved to file: " + fileName);
                } else {
                    throw new RuntimeException("Failed to get comments. Status code: " + getCommentsResponse.statusCode());
                }
            } else {
                System.out.println("No posts found for the user.");
            }
        } else {
            throw new RuntimeException("Failed to get posts. Status code: " + getPostsResponse.statusCode());
        }
    }
}

