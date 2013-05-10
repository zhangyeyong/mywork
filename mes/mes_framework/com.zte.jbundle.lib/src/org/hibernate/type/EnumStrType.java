/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * Copyright (c) 2008, Red Hat Middleware LLC or third-party contributors as
 * indicated by the @author tags or express copyright attribution
 * statements applied by the authors.  All third-party contributions are
 * distributed under license by Red Hat Middleware LLC.
 *
 * This copyrighted material is made available to anyone wishing to use, modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU
 * Lesser General Public License, as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this distribution; if not, write to:
 * Free Software Foundation, Inc.
 * 51 Franklin Street, Fifth Floor
 * Boston, MA  02110-1301  USA
 */
package org.hibernate.type;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Properties;

import org.hibernate.HibernateException;
import org.hibernate.usertype.EnhancedUserType;
import org.hibernate.usertype.ParameterizedType;
import org.hibernate.util.ReflectHelper;
import org.hibernate.util.StringHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 枚举类型《==》数据库字符串 互相转换插件；用法：
 * 
 * <pre>
 * &lt;column name="CODE_USAGE" /&gt;
 * &lt;type name="org.hibernate.type.EnumStrType"&gt;
 *      &lt;param name="enum"&gt;
 *            com.zte.mes.model.BasCode$CodeUsage
 *      &lt;/param&gt;
 * &lt;/type&gt;
 * </pre>
 * 
 * @author PanJun
 */
@SuppressWarnings({ "unchecked", "rawtypes", "serial" })
public class EnumStrType implements EnhancedUserType, ParameterizedType, Serializable {
    /**
     * This is the old scheme where logging of parameter bindings and value
     * extractions was controlled by the trace level enablement on the
     * 'org.hibernate.type' package...
     * <p/>
     * Originally was cached such because of performance of looking up the
     * logger each time in order to check the trace-enablement. Driving this via
     * a central Log-specific class would alleviate that performance hit, and
     * yet still allow more "normal" logging usage/config.
     */
    private static final boolean IS_VALUE_TRACING_ENABLED = LoggerFactory.getLogger(StringHelper.qualifier(Type.class.getName()))
            .isTraceEnabled();
    private transient Logger log;

    private Logger log() {
        if (log == null) {
            log = LoggerFactory.getLogger(getClass());
        }
        return log;
    }

    public static final String ENUM = "enum";
    private Class<? extends Enum> enumClass;

    public int[] sqlTypes() {
        return new int[] { Types.VARCHAR };
    }

    public Class<? extends Enum> returnedClass() {
        return enumClass;
    }

    public boolean equals(Object x, Object y) throws HibernateException {
        return x == y;
    }

    public int hashCode(Object x) throws HibernateException {
        return x == null ? 0 : x.hashCode();
    }

    public Object nullSafeGet(ResultSet rs, String[] names, Object owner) throws HibernateException, SQLException {
        String s = rs.getString(names[0]);
        if (s == null) {
            if (IS_VALUE_TRACING_ENABLED) {
                log().debug("Returning null as column {}", names[0]);
            }
            return null;
        }

        if (IS_VALUE_TRACING_ENABLED) {
            log().debug("Returning '{}' as column {}", s, names[0]);
        }
        try {
            return Enum.valueOf(enumClass, s);
        } catch (IllegalArgumentException iae) {
            throw new IllegalArgumentException("Unknown name value for enum " + enumClass + ": " + s, iae);
        }
    }

    public void nullSafeSet(PreparedStatement st, Object value, int index) throws HibernateException, SQLException {
        if (value == null) {
            if (IS_VALUE_TRACING_ENABLED)
                log().debug("Binding null to parameter: {}", index);
            st.setString(index, null);
        } else {
            String enumString = ((Enum<?>) value).name();
            if (IS_VALUE_TRACING_ENABLED) {
                log().debug("Binding '{}' to parameter: {}", enumString, index);
            }
            st.setString(index, enumString);
        }
    }

    public Object deepCopy(Object value) throws HibernateException {
        return value;
    }

    public boolean isMutable() {
        return false;
    }

    public Serializable disassemble(Object value) throws HibernateException {
        return (Serializable) value;
    }

    public Object assemble(Serializable cached, Object owner) throws HibernateException {
        return cached;
    }

    public Object replace(Object original, Object target, Object owner) throws HibernateException {
        return original;
    }

    public void setParameterValues(Properties parameters) {
        String enumClassName = parameters.getProperty(ENUM);
        try {
            enumClass = ReflectHelper.classForName(enumClassName, this.getClass()).asSubclass(Enum.class);
        } catch (ClassNotFoundException exception) {
            throw new HibernateException("Enum class not found", exception);
        }
    }

    public String objectToSQLString(Object value) {
        return '\'' + ((Enum) value).name() + '\'';
    }

    public String toXMLString(Object value) {
        return ((Enum) value).name();
    }

    public Object fromXMLString(String xmlValue) {
        try {
            return Enum.valueOf(enumClass, xmlValue);
        } catch (IllegalArgumentException iae) {
            throw new IllegalArgumentException("Unknown name value for enum " + enumClass + ": " + xmlValue, iae);
        }
    }

}
