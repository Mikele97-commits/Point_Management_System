import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import com.fasterxml.jackson.databind.ObjectMapper;

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
        server.start();
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
}

class UserData {
    public String firstName;
    public String lastName;
    public String email;
}
