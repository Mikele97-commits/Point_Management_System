import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.sql.*;

import java.io.OutputStream;
import java.util.List;
import java.io.IOException;
import java.net.InetSocketAddress;




public class SimpleServer {

    public static void main(String[] args) throws Exception {

        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        server.createContext("/add", new AddHandler());
        server.createContext("/show", new ShowHandler());
        server.createContext("/activate", new ActivateHandler());
        server.createContext("/points", new PointsHandler());
        server.createContext("/write", new WriteHandler());
        server.createContext("/read", new ReadHandler());

        server.start();
        Connection conn = DriverManager.getConnection("jdbc:sqlite:users.db");
        DataBase.create(conn);
    }







    static class AddHandler implements HttpHandler {
        public void handle(HttpExchange exchange) throws IOException {
            exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
            exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");
            exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "POST, OPTIONS");
            exchange.getResponseHeaders().add("Content-Type", "application/json");
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

        }
    }

    static class ShowHandler implements HttpHandler {
        public void handle(HttpExchange exchange) throws IOException {
            List<User>usersShow= UserList.users;
            exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            ObjectMapper mapper = new ObjectMapper();
            String json = mapper.writeValueAsString(usersShow);
            System.out.println(json);
            exchange.sendResponseHeaders(200, json.getBytes().length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(json.getBytes());
            }
        }
    }

    static class ActivateHandler implements HttpHandler {
        public void handle(HttpExchange exchange) throws IOException {
            exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
            exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "POST, OPTIONS");
            String body = new String(exchange.getRequestBody().readAllBytes()); //Get request
            int id = Integer.parseInt(body.trim());
            User user = UserList.users.get(id-1); //Find user
            user.setActive(!user.isActive());
           String response = "User's activity changed to " + (user.isActive() ? "activated" : "deactivated");
           exchange.sendResponseHeaders(200, response.getBytes().length);
           exchange.getResponseBody().write(response.getBytes());
           exchange.getResponseBody().close();
        }
    }

    static class PointsHandler implements HttpHandler {
        public void handle(HttpExchange exchange) throws IOException {
            exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
            exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "POST, OPTIONS");
            String body = new String(exchange.getRequestBody().readAllBytes());
            String[] split = body.split(",");
            int id = Integer.parseInt(split[0]);
            int points = Integer.parseInt(split[1]);
            User user = UserList.users.get(id-1);
            if(!user.isActive()) {
                String response = "User is deactivated. Operation not allowed.";
                exchange.sendResponseHeaders(403, response.getBytes().length);
                exchange.getResponseBody().write(response.getBytes());
                exchange.getResponseBody().close();
                return;
            }
            user.setPoints(user.getPoints() + points);

            String response = "User's points changed to " + user.getPoints();
            exchange.sendResponseHeaders(200, response.getBytes().length);
            exchange.getResponseBody().write(response.getBytes());
            exchange.getResponseBody().close();
        }
    }

    static class WriteHandler implements HttpHandler{
        public void handle(HttpExchange exchange) throws IOException {
            exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
            exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "POST, OPTIONS");
            List<User> users = UserList.users;
            System.out.println("Stage 1");
            try {
                Connection conn = DriverManager.getConnection("jdbc:sqlite:users.db");
                PreparedStatement pstmt = conn.prepareStatement("INSERT INTO users (ID, firstName, lastName, email, active, points) VALUES (?,?,?,?,?,?)");
                System.out.println("Stage 2");
                for(User user : users){
                    pstmt.setInt(1, user.getID());
                    pstmt.setString(2, user.getFirstName());
                    pstmt.setString(3, user.getLastName());
                    pstmt.setString(4, user.getEmail());
                    pstmt.setInt(5, user.isActive() ? 1 : 0);
                    pstmt.setInt(6, user.getPoints());
                    pstmt.executeUpdate();
                }
                System.out.println("Stage 3");
                String response = "Data properly written to users.db";
                exchange.sendResponseHeaders(200, response.getBytes().length);
                exchange.getResponseBody().write(response.getBytes());
                exchange.getResponseBody().close();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }

    static class ReadHandler implements HttpHandler{
        public void handle(HttpExchange exchange) throws IOException {
            exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
            exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "POST, OPTIONS");

            try {
                Connection conn = DriverManager.getConnection("jdbc:sqlite:users.db");
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT * FROM users");

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

                    UserList.createAndAddUser(id,firstName,lastName,email,boolActive,points);
                    System.out.println("User added to list");

                }
                String response = "Data properly read from users.db";
                exchange.sendResponseHeaders(200, response.getBytes().length);
                exchange.getResponseBody().write(response.getBytes());
                exchange.getResponseBody().close();


            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }
}

class UserData {
    public String firstName;
    public String lastName;
    public String email;
}
