package main.java;

import com.sun.net.httpserver.HttpServer;
import java.sql.*;
import java.net.InetSocketAddress;




public class SimpleServer {

    public static void main(String[] args) throws Exception {

        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        server.createContext("/user", new UserHandler());
        server.createContext("/database", new DatabaseHandler());

        server.start();
        Connection conn = DriverManager.getConnection("jdbc:sqlite:users.db");
        System.out.println("Server started successfully on http://localhost:8080");
        System.out.println("Press Ctrl+C to stop the server...");
        DataBase.create(conn);
    }
}

