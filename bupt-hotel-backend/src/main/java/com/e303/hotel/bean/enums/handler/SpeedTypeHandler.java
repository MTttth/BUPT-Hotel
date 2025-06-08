package com.e303.hotel.bean.enums.handler;


import com.e303.hotel.bean.enums.Speed;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.sql.*;

public class SpeedTypeHandler extends BaseTypeHandler<Speed> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, Speed speed, JdbcType jdbcType) throws SQLException {
        ps.setString(i, speed.getDbValue());
    }

    @Override
    public Speed getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return Speed.fromDbValue(rs.getString(columnName));
    }

    @Override
    public Speed getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return Speed.fromDbValue(rs.getString(columnIndex));
    }

    @Override
    public Speed getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return Speed.fromDbValue(cs.getString(columnIndex));
    }
}
