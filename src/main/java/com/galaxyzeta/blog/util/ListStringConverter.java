package com.galaxyzeta.blog.util;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeHandler;
import org.springframework.stereotype.Component;

@Component
public class ListStringConverter implements TypeHandler<List<String>> {
    @Override
    public void setParameter(PreparedStatement preparedStatement, int i, List<String> strings, JdbcType jdbcType) throws SQLException {
        StringBuffer sb = new StringBuffer();
        for (String s : strings) {
            sb.append(s).append(",");
        }
        preparedStatement.setString(i, sb.toString().substring(0, sb.toString().length() - 1));
    }

    @Override
    public List<String> getResult(ResultSet resultSet, String s) throws SQLException {
        String[] arr = resultSet.getString(s).split(",");
        return Arrays.asList(arr);
    }

    @Override
    public List<String> getResult(ResultSet resultSet, int i) throws SQLException {
        String[] arr = resultSet.getString(i).split(",");
        return Arrays.asList(arr);
    }

    @Override
    public List<String> getResult(CallableStatement callableStatement, int i) throws SQLException {
        String[] arr = callableStatement.getString(i).split(",");
        return Arrays.asList(arr);
    }
}
