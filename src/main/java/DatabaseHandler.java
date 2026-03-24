package main.java;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.sql.*;
import java.util.List;

class DatabaseHandler implements HttpHandler {
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        System.out.println("Request received: " + method);
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "POST, GET, PATCH");
        exchange.getResponseHeaders().add("Content-Type", "application/json");
        switch (method) {
            case "GET": {
                UserList.clearUsers();
                try {
                    Connection conn = DriverManager.getConnection("jdbc:sqlite:users.db");
                    Statement stmt = conn.createStatement();
                    ResultSet rs = stmt.executeQuery("SELECT * FROM users");
                    while (rs.next()) {
                        int id = rs.getInt(1);
                        String firstName = rs.getString(2);
                        String lastName = rs.getString(3);
                        String email = rs.getString(4);
                        int active = rs.getInt(5);
                        boolean boolActive;
                        if (active == 1) {
                            boolActive = true;
                        } else {
                            boolActive = false;
                        }
                        int points = rs.getInt(6);
                        UserList.createAndAddUser(id, firstName, lastName, email, boolActive, points);

                    }
                    String response = "Data properly read from users.db";
                    exchange.sendResponseHeaders(200, response.getBytes().length);
                    exchange.getResponseBody().write(response.getBytes());
                    exchange.getResponseBody().close();


                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
                break;
            }
            case "POST": {
                List<User> users = UserList.users;
                try {
                    Connection conn = DriverManager.getConnection("jdbc:sqlite:users.db");
                    PreparedStatement pstmt = conn.prepareStatement("INSERT or IGNORE INTO users (ID, firstName, lastName, email, active, points) VALUES (?,?,?,?,?,?)");
                    for (User user : users) {
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
            case "PATCH": {
                String body = new String(exchange.getRequestBody().readAllBytes());
                int ID = Integer.parseInt(body);
                User user = UserList.users.get(ID - 1);
                if (ID != user.getID()) {
                    String response = "ID incompatible";
                    exchange.sendResponseHeaders(403, response.getBytes().length);
                    exchange.getResponseBody().write(response.getBytes());
                    exchange.getResponseBody().close();
                    return;
                }
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
            case "OPTIONS": {
                exchange.sendResponseHeaders(204, -1);
            }
        }
    }
}
