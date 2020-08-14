package com.example.demo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class Repository {
    public List<String> findEmail() {
        Connection con = DBManager.createConnection();
        String sql = "SELECT  u. as mail" +
                " FROM users as u " +
                " ORDER BY u.id";

        List<String> mailList = new ArrayList<>();
        try {
            PreparedStatement pstmt = con.prepareStatement(sql);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                mailList.add(rs.getString("mail"));
            }
        } catch (Exception e) {

        }

        mailList.forEach(System.out::println);

        return mailList;
    }
}
