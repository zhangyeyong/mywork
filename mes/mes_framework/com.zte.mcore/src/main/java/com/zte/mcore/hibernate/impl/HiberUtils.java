package com.zte.mcore.hibernate.impl;

public class HiberUtils {

    /**
     * 判断数据库连接别名是否相等
     * 
     * @param alias1
     * @param alias2
     * @return
     */
    public static boolean equalsAlias(String alias1, String alias2) {
        alias1 = getShortAlias(alias1);
        alias2 = getShortAlias(alias2);
        if (alias1 != null) {
            return alias1.equalsIgnoreCase(alias2);
        } else if (alias2 != null) {
            return alias2.equalsIgnoreCase(alias1);
        } else {
            return false;
        }
    }

    public static String getShortAlias(String alias) {
        if (alias == null) {
            return null;
        }
        if (alias.startsWith("proxool.")) {
            alias = alias.substring("proxool.".length());
        }
        return alias;
    }
}
