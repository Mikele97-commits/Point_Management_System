import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.InetSocketAddress;



public class SimpleServer {

    public static void main(String[] args) throws Exception {

        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);


        server.createContext("/add", new AddHandler());

        server.start();
        System.out.println("Server started on http://localhost:8080");
    }







    static class AddHandler implements HttpHandler {
        public void handle(HttpExchange exchange) throws IOException {
            exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
            exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");
            exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "POST, OPTIONS");
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            String body = new String(exchange.getRequestBody().readAllBytes());
            System.out.println("Request received" + body);
            ObjectMapper mapper = new ObjectMapper();
            UserData user = mapper.readValue(body, UserData.class);
            UserList.createAndAddUser(user.firstName, user.lastName, user.email);
            UserList.showUsers();




        }
    }
}

class UserData {
    public String firstName;
    public String lastName;
    public String email;
}
