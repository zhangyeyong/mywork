package com.zte.jbundle.hibernate.framework;

public class HiberUtils {

    /**
     * 判断数据库连接别名是否相等
     * 
     * @param alias1
     * @param alias2
     * @return
     */
    public static boolean equalsAlias(String alias1, String alias2) {
        alias1 = alias1 == null ? "" : alias1.trim();
        alias2 = alias2 == null ? "" : alias2.trim();
        return alias1.equalsIgnoreCase(alias2);
    }

}
