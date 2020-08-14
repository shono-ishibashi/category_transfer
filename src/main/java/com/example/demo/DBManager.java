package com.example.demo;

import java.sql.Connection;
import java.sql.DriverManager;

public class DBManager {
    private final static String DB_NAME = "example";
    private final static String URL = "jdbc:postgresql://localhost:5432/" + DB_NAME;
    private final static String USER_NAME = "postgres";
    private final static String PASSWORD = "postgres";

    public static Connection createConnection(){
        try{
            Connection con = DriverManager.getConnection(URL,USER_NAME,PASSWORD);

            return con;
        }catch (Exception e){
            throw new RuntimeException("DB接続に失敗しました",e);
        }
    }

    public static void closeConnection(Connection con){
        try{
            con.close();
        }catch(Exception e){
            throw new RuntimeException("DB接続に失敗しました",e);
        }
    }
}
