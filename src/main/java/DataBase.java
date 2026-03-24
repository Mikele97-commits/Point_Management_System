package main.java;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class DataBase {
    public static void create(Connection conn) throws SQLException {
        String sql = """
CREATE TABLE IF NOT EXISTS users (
    ID INTEGER PRIMARY KEY,
    firstName VARCHAR(30) NOT NULL,
    lastName VARCHAR(30) NOT NULL,
    email VARCHAR(50) NOT NULL UNIQUE,
    active BIT NOT NULL,
    points INTEGER NOT NULL
);""";
        Statement stmt = conn.createStatement();
        stmt.execute(sql);
    }
}
