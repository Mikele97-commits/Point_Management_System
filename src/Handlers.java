import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.sql.*;
import java.util.List;

  class UserHandler implements HttpHandler {
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

class DatabaseHandler implements HttpHandler {
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        System.out.println("Request received: " + method);
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "POST, GET, PATCH");
        exchange.getResponseHeaders().add("Content-Type", "application/json");
        switch (method) {
            case "GET":{
                UserList.clearUsers();
                System.out.println("Part 1");
                try {
                    Connection conn = DriverManager.getConnection("jdbc:sqlite:users.db");
                    Statement stmt = conn.createStatement();
                    ResultSet rs = stmt.executeQuery("SELECT * FROM users");
                    System.out.println("Part 2");
                    while(rs.next()){
                        int id = rs.getInt(1);
                        String firstName = rs.getString(2);
                        String lastName = rs.getString(3);
                        String email = rs.getString(4);
                        int active = rs.getInt(5);
                        boolean boolActive;
                        if(active == 1){
                            boolActive = true;
                        }
                        else{
                            boolActive = false;
                        }
                        int points = rs.getInt(6);
                        System.out.println("Part 3");
                        UserList.createAndAddUser(id,firstName,lastName,email,boolActive,points);

                    }
                    System.out.println("Part 4");
                    String response = "Data properly read from users.db";
                    exchange.sendResponseHeaders(200, response.getBytes().length);
                    exchange.getResponseBody().write(response.getBytes());
                    exchange.getResponseBody().close();


                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
                break;
            }
            case "POST":{
                List<User> users = UserList.users;
                try {
                    Connection conn = DriverManager.getConnection("jdbc:sqlite:users.db");
                    PreparedStatement pstmt = conn.prepareStatement("INSERT or IGNORE INTO users (ID, firstName, lastName, email, active, points) VALUES (?,?,?,?,?,?)");
                    for(User user : users){
                        pstmt.setInt(1, user.getID());
                        pstmt.setString(2, user.getFirstName());
                        pstmt.setString(3, user.getLastName());
                        pstmt.setString(4, user.getEmail());
                        pstmt.setInt(5, user.isActive() ? 1 : 0);
                        pstmt.setInt(6, user.getPoints());
                        pstmt.addBatch();
                    }
                    pstmt.executeBatch();
                    String response = "Data properly written to users.db";
                    exchange.sendResponseHeaders(200, response.getBytes().length);
                    exchange.getResponseBody().write(response.getBytes());
                    exchange.getResponseBody().close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                break;
            }
            case "PATCH":{
                String body = new String(exchange.getRequestBody().readAllBytes());
                int ID = Integer.parseInt(body);
                System.out.println("ID: " + ID);
                User user = UserList.users.get(ID-1);
                System.out.println("Stage 1, got userid:" + ID + "users.getID=" + user.getID());
                if(ID!=user.getID()){
                    String response = "ID incompatible";
                    exchange.sendResponseHeaders(403, response.getBytes().length);
                    exchange.getResponseBody().write(response.getBytes());
                    exchange.getResponseBody().close();
                    return;
                }
                System.out.println("New activity:" + user.isActive() + "new Points:" + user.getPoints());
                try {
                    Connection conn = DriverManager.getConnection("jdbc:sqlite:users.db");
                    PreparedStatement pstmt = conn.prepareStatement("UPDATE users SET active = ?, points = ? WHERE ID = ?");

                    pstmt.setInt(1, user.isActive() ? 1 : 0);
                    pstmt.setInt(2, user.getPoints());
                    pstmt.setInt(3, ID);

                    pstmt.executeUpdate();

                    String response = "Data properly edited in users.db";
                    exchange.sendResponseHeaders(200, response.getBytes().length);
                    exchange.getResponseBody().write(response.getBytes());
                    exchange.getResponseBody().close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                break;
            }
            case "OPTIONS":{
                exchange.sendResponseHeaders(204, -1);
            }
        }
    }
}

