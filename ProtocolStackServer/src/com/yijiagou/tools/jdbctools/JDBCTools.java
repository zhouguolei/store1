package com.yijiagou.tools.jdbctools;

import java.io.InputStream;
import java.sql.*;
import java.util.Properties;

/**
 * Created by wangwei on 17-6-4.
 */
public class JDBCTools {

    public static void release(Connection connection, Statement statement,ResultSet resultSet){
        if (resultSet != null){
            try {
                resultSet.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        if(statement != null){
            try {
                statement.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        if(connection != null){
            try {
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static void release(Connection connection, Statement statement){
        if(statement != null){
            try {
                statement.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        if(connection != null){
            try {
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static Connection getConnection() throws Exception {
        String clazz = null;
        String url = null;
        String user = null;
        String password = null;

        InputStream in = JDBCTools.class.getClassLoader().getResourceAsStream("JDBC.properties");
        Properties properties = new Properties();
        properties.load(in);

        clazz = properties.getProperty("class");
        url = properties.getProperty("url");
        user = properties.getProperty("user");
        password = properties.getProperty("password");

        Class.forName(clazz);

        Connection connection = DriverManager.getConnection(url,user,password);

        return connection;
    }

    public static int updata(String sql,Object...args) throws Exception {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        int number = 0;
        try {
            connection = JDBCTools.getConnection();
            preparedStatement = connection.prepareStatement(sql);

            for (int i = 0; i < args.length; i++) {
                preparedStatement.setObject(i + 1, args[i]);
            }

            number = preparedStatement.executeUpdate();
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            release(connection,preparedStatement);
        }

        return number;
    }

}
