package org.hibernate.type;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import org.hibernate.dialect.Dialect;

@SuppressWarnings("serial")
public class BooleanType extends PrimitiveType implements DiscriminatorType {

    public Serializable getDefaultValue() {
        return null;
    }

    private static Boolean toBoolean(String s) {
        if ("1".equals(s)) {
            return Boolean.TRUE;
        } else if ("0".equals(s)) {
            return Boolean.FALSE;
        } else {
            return null;
        }
    }

    private static String fromBoolean(Boolean b) {
        if (b == null) {
            return null;
        } else {
            return b.booleanValue() ? "1" : "0";
        }
    }

    public Object get(ResultSet rs, String name) throws SQLException {
        return toBoolean(rs.getString(name));
    }

    public Class<?> getPrimitiveClass() {
        return boolean.class;
    }

    public Class<?> getReturnedClass() {
        return Boolean.class;
    }

    public void set(PreparedStatement st, Object value, int index) throws SQLException {
        st.setString(index, fromBoolean((Boolean) value));
    }

    public int sqlType() {
        return Types.CHAR;
    }

    public String getName() {
        return "boolean";
    }

    public String objectToSQLString(Object value, Dialect dialect) throws Exception {
        return "'" + fromBoolean((Boolean) value) + "'";
    }

    public Object stringToObject(String xml) throws Exception {
        return toBoolean(xml);
    }

    public Object fromStringValue(String xml) {
        return toBoolean(xml);
    }

}
