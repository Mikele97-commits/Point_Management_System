package main.java;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

public class UserHandler implements HttpHandler {
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        System.out.println("Request received: " + method);
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "POST, GET, PATCH");
        exchange.getResponseHeaders().add("Content-Type", "application/json");
        switch (method) {
            case "GET":{
                List<User> usersShow= UserList.users;
                ObjectMapper mapper = new ObjectMapper();
                String json = mapper.writeValueAsString(usersShow);
                exchange.sendResponseHeaders(200, json.getBytes().length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(json.getBytes());
                }
                break;
            }
            case "POST":{
                String body = new String(exchange.getRequestBody().readAllBytes());
                ObjectMapper mapper = new ObjectMapper();
                UserData user = mapper.readValue(body, UserData.class);
                if (user.firstName == null || user.firstName.isBlank() || user.lastName == null||user.lastName.isBlank()||user.email == null||user.email.isBlank()) {
                    String response = "Data invalid";
                    exchange.sendResponseHeaders(403, response.getBytes().length);
                    exchange.getResponseBody().write(response.getBytes());
                    exchange.getResponseBody().close();
                    return;
                }

                if(UserList.emailExists(user.email)){
                    String response = "Email already in use";
                    exchange.sendResponseHeaders(403, response.getBytes().length);
                    exchange.getResponseBody().write(response.getBytes());
                    exchange.getResponseBody().close();
                    return;
                }
                if(!EmailValidator.isValid(user.email)){
                    String response = "Incorrect email format. Letters and numbers only";
                    exchange.sendResponseHeaders(403, response.getBytes().length);
                    exchange.getResponseBody().write(response.getBytes());
                    exchange.getResponseBody().close();
                    return;
                }
                UserList.createAndAddUser(user.firstName, user.lastName, user.email);
                String response = "User succesfully added";
                exchange.sendResponseHeaders(200, response.getBytes().length);

                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(response.getBytes());
                }
                break;
            }

            case "PATCH":{
                System.out.println("Received PATCH request");
                String path = exchange.getRequestURI().getPath();
                String[] parts = path.split("/");
                int ID = Integer.parseInt(parts[2]);
                String action = parts[3];
                switch (action) {
                    case "activate":
                    {
                        User user = UserList.users.get(ID-1); //Find user
                        user.setActive(!user.isActive());
                        String response = "User's activity changed to " + (user.isActive() ? "activated" : "deactivated");
                        exchange.sendResponseHeaders(200, response.getBytes().length);
                        exchange.getResponseBody().write(response.getBytes());
                        exchange.getResponseBody().close();
                        break;
                    }
                    case "points":
                    {
                        String body = new String(exchange.getRequestBody().readAllBytes());
                        int points = Integer.parseInt(body);
                        User user = UserList.users.get(ID-1);
                        if(!user.isActive()) {
                            String response = "User is deactivated. Operation not allowed.";
                            exchange.sendResponseHeaders(403, response.getBytes().length);
                            exchange.getResponseBody().write(response.getBytes());
                            exchange.getResponseBody().close();
                            break;
                        }
                        user.setPoints(user.getPoints() + points);

                        String response = "User's points changed to " + user.getPoints();
                        exchange.sendResponseHeaders(200, response.getBytes().length);
                        exchange.getResponseBody().write(response.getBytes());
                        exchange.getResponseBody().close();
                    }
                }
                break;
            }

            case "OPTIONS":
                exchange.sendResponseHeaders(204, -1);
        }
    }
}

