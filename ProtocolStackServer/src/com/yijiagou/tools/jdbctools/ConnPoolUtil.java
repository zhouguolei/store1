package com.yijiagou.tools.jdbctools;

import com.mchange.v2.c3p0.ComboPooledDataSource;

import javax.activation.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by zgl on 17-8-17.
 */
public class ConnPoolUtil {
    private static ComboPooledDataSource dataSource = null;

    static {
        dataSource = new ComboPooledDataSource("myc3p0");
    }
    public static void release(Connection con, PreparedStatement pst, ResultSet rs) {
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException e) {

                e.printStackTrace();
            }
        }
        if (pst != null) {
            try {
                pst.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        if (con != null) {
            try {
                rs.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static void release(Connection con, PreparedStatement pst) {
        if (pst != null) {
            try {
                pst.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        if (con != null) {
            try {
                con.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

    }

    public static int updata(String sql, Object... args) throws Exception {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        int number = 0;
            connection =dataSource.getConnection();
            preparedStatement = connection.prepareStatement(sql);
            for (int i = 0; i < args.length; i++) {
                preparedStatement.setObject(i + 1, args[i]);
            }
            number = preparedStatement.executeUpdate();

        return number;
    }
}
