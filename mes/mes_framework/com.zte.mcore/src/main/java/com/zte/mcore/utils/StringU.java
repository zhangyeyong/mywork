package com.zte.mcore.utils;

import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

/**
 * 字符串工具类，方法签名一定要直白、简洁，尽量作到不需要注释
 * 
 * @author PanJun
 * 
 */
@SuppressWarnings("restriction")
public final class StringU {

    public static String toString(Object o) {
        return o == null ? null : o.toString();
    }

    public static Long toLong(Object s) {
        if (s == null) {
            return null;
        } else {
            String str = s.toString().trim();
            if (str.length() == 0) {
                return null;
            }

            try {
                return Long.parseLong(str);
            } catch (Exception ex) {
                return null;
            }
        }
    }

    public static Integer toInteger(Object s) {
        if (s == null) {
            return null;
        } else {
            String str = s.toString().trim();
            if (str.length() == 0) {
                return null;
            }

            try {
                return Integer.parseInt(str);
            } catch (Exception ex) {
                return null;
            }
        }
    }

    public static boolean isBlank(String s) {
        return s == null || s.trim().equals("");
    }

    public static boolean hasText(String s) {
        return s != null && s.trim().length() > 0;
    }

    public static String trim(String s) {
        return s == null ? null : s.trim();
    }

    public static String upper(String s) {
        return s == null ? null : s.toUpperCase();
    }

    public static String lower(String s) {
        return s == null ? null : s.toLowerCase();
    }

    public static char upper(char c) {
        if (c < MAPPER_LEN) {
            return charMapper[c].upper;
        } else {
            return c;
        }
    }

    public static char lower(char c) {
        if (c < MAPPER_LEN) {
            return charMapper[c].lower;
        } else {
            return c;
        }
    }

    public static String nullToEmpty(String s) {
        if (s == null)
            return "";
        else
            return s;
    }

    public static boolean equals(String s1, String s2) {
        if (s1 != null)
            return s1.equals(s2);
        else if (s2 != null)
            return s2.equals(s1);
        else
            return true;
    }

    /** 限制字符长度，不会返回空 */
    public static String limit(String s, int maxLen) {
        if (s == null || s.length() == 0 || maxLen <= 0) {
            return "";
        }
        if (s.length() <= maxLen) {
            return s;
        }

        return s.substring(0, maxLen);
    }

    public static int compare(String s1, String s2) {
        if (s1 == null && s2 == null)
            return 0;
        else if (s1 == null)
            return -1;
        else if (s2 == null)
            return 1;
        else
            return s1.compareTo(s2);
    }

    /** 不区分大小写 */
    public static int compareText(String s1, String s2) {
        if (s1 == null && s2 == null)
            return 0;
        else if (s1 == null)
            return -1;
        else if (s2 == null)
            return 1;
        else
            return s1.compareToIgnoreCase(s2);
    }

    public static boolean equalsText(String s1, String s2) {
        if (s1 != null)
            return s1.equalsIgnoreCase(s2);
        else if (s2 != null)
            return s2.equalsIgnoreCase(s1);
        else
            return true;
    }

    public static boolean equalsAny(String s, Object... texts) {
        for (Object o : texts) {
            String text = o == null ? null : o.toString();
            if (equals(s, text))
                return true;
        }

        return false;
    }

    public static boolean equalsAny(String s, Collection<?> texts) {
        if (texts != null) {
            for (Object o : texts) {
                String text = o == null ? null : o.toString();
                if (equals(s, text))
                    return true;
            }
        }

        return false;
    }

    public static boolean equalsAnyText(String s, Object... texts) {
        for (Object o : texts) {
            String text = o == null ? null : o.toString();
            if (equalsText(s, text))
                return true;
        }

        return false;
    }

    public static boolean equalsAnyText(String s, Collection<?> texts) {
        if (texts != null) {
            for (Object o : texts) {
                String text = o == null ? null : o.toString();
                if (equalsText(s, text))
                    return true;
            }
        }

        return false;
    }

    public static String uuid() {
        String ret = UUID.randomUUID().toString();
        return ret.replaceAll("-", "");
    }

    /**
     * 拼接REST形式的url，eg: <br>
     * linkUrl("http://127.0.0.1/", "/1") -> http://127.0.0.1/1<br>
     * linkUrl("http://127.0.0.1", "1") -> http://127.0.0.1/1<br>
     * 
     * @param urls
     * @return
     */
    public static String linkUrl(String... urls) {
        StringBuilder ret = new StringBuilder();
        for (int i = 0, len = urls.length; i < len; i++) {
            String s = urls[i];
            if (s == null)
                s = "";

            if (i < len - 1) {
                while (s.endsWith("/")) {
                    s = s.substring(0, s.length() - 1);
                }
            }

            if (i > 0) {
                while (s.startsWith("/")) {
                    s = s.substring(1);
                }
            }

            if (ret.length() > 0 && s.length() > 0)
                ret.append("/");
            ret.append(s);
        }
        return ret.toString();
    }

    /**
     * 把给定的字符串转换为base64编码，同时转义非数字、字母字符
     * 
     * @param s
     * @return
     */
    public static String toBase64Ext(String s) {
        if (s == null)
            return null;

        BASE64Encoder encoder = new BASE64Encoder();
        try {
            String encodedStr = encoder.encode(s.getBytes("utf-8"));
            String ret = encodeChars(encodedStr);
            return ret;
        } catch (Exception e) {
            throw new IllegalArgumentException("toBase64Ext(" + s + ")", e);
        }
    }

    /**
     * 把通过toBase64Ext转换后的字符串还原成原始 字符串
     * 
     * @param s
     * @return
     */
    public static String fromBase64Ext(String s) {
        if (s == null)
            return null;

        BASE64Decoder decoder = new BASE64Decoder();
        try {
            String arg = decodeChars(s);

            String ret = new String(decoder.decodeBuffer(arg.toString()), "utf-8");
            return ret;
        } catch (Exception e) {
            throw new IllegalArgumentException("fromBase64Ext(" + s + ")", e);
        }
    }

    /**
     * 转码a..z,A..Z,0..9集合以外的特殊字符
     * 
     * @param encodedStr
     * @return
     */
    public static String encodeChars(String encodedStr) {
        // 处理windows到linux回车换行符替换符_d__a_
        encodedStr = encodedStr.replaceAll("\\\r", "");
        StringBuilder ret = new StringBuilder();
        for (int i = 0; i < encodedStr.length(); i++) {
            char c = encodedStr.charAt(i);
            if ((c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z') || (c >= '0' && c <= '9')) {
                ret.append(c);
            } else {
                ret.append("_").append(Integer.toHexString((int) c)).append("_");
            }
        }
        return ret.toString();
    }

    /**
     * encodeChars方法的恢复方法
     * 
     * @param s
     * @return
     */
    public static String decodeChars(String s) {
        StringBuilder arg = new StringBuilder((int) (s.length() * 1.3));
        int i = 0;
        int len = s.length();
        while (i < len) {
            char c = s.charAt(i);
            if (c != '_') {
                arg.append(c);
                i++;
            } else {
                int hi = -1;
                for (int k = i + 1; k < len; k++) {
                    if (s.charAt(k) == '_') {
                        hi = k;
                        break;
                    }
                }

                if (hi == -1 || i + 1 == hi) {
                    arg.append(c);
                    i++;
                } else {
                    String digital = s.substring(i + 1, hi);
                    try {
                        char reChar = (char) (Integer.parseInt(digital, 16));
                        arg.append(reChar);
                    } catch (Exception e) {
                        arg.append(c).append(digital).append(c);
                    }
                    i = hi + 1;
                }
            }
        }
        return arg.toString();
    }

    public static String toMd5(String s) {
        if (s == null) {
            return null;
        }

        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] bytes = md.digest(s.getBytes("utf-8"));

            StringBuilder ret = new StringBuilder();
            String stmp = "";
            for (int n = 0; n < bytes.length; n++) {
                stmp = (java.lang.Integer.toHexString(bytes[n] & 0XFF).toUpperCase());
                if (stmp.length() == 1)
                    ret.append("0").append(stmp);
                else
                    ret.append(stmp);
            }
            return ret.toString();
        } catch (Exception ex) {
            throw new java.lang.RuntimeException("toMd5 error arg s=[" + s + "]!", ex);
        }
    }

    /**
     * 不区分大小写的:String.indexOf
     * 
     * @param s
     * @param sub
     * @return
     */
    public static int indexOfText(String s, String sub) {
        int len = 0;
        int subLen = 0;
        if (s == null || sub == null || (len = s.length()) < (subLen = sub.length())) {
            return -1;
        }

        for (int i = 0, count = len - subLen + 1; i < count; i++) {
            boolean isEq = true;
            for (int k = 0, t = i; k < subLen; k++, t++) {
                if (lower(s.charAt(t)) != lower(sub.charAt(k))) {
                    isEq = false;
                    break;
                }
            }

            if (isEq) {
                return i;
            }
        }
        return -1;
    }

    /**
     * 不区分大小写、非正则表达式的分割字符串
     * 
     * @param s
     * @param sub
     * @return
     */
    public static List<String> splitText(String s, String sub) {
        List<String> ret = new ArrayList<String>();
        if (s == null || sub == null) {
            return ret;
        }
        if (sub.length() == 0) {
            ret.add(s);
            return ret;
        }

        while (s.length() > 0) {
            int i = indexOfText(s, sub);
            if (i == -1) {
                ret.add(s);
                break;
            } else {
                ret.add(s.substring(0, i));
                s = s.substring(i + sub.length());
            }
        }
        return ret;
    }

    /**
     * 非正则表达式的分割字符串
     * 
     * @param s
     * @param sub
     * @return
     */
    public static List<String> split(String s, String sub) {
        List<String> ret = new ArrayList<String>();
        if (s == null || sub == null) {
            return ret;
        }
        if (sub.length() == 0) {
            ret.add(s);
            return ret;
        }

        while (s.length() > 0) {
            int i = s.indexOf(sub);
            if (i == -1) {
                ret.add(s);
                break;
            } else {
                ret.add(s.substring(0, i));
                s = s.substring(i + sub.length());
            }
        }
        return ret;
    }

    /**
     * 非正则表达式替换
     * 
     * @param s
     * @param sub
     * @param rep
     * @return
     */
    public static String replaceAll(String s, String sub, String rep) {
        if (s == null || sub == null || rep == null) {
            return s;
        }

        StringBuilder sbRet = new StringBuilder();
        while (s.length() > 0) {
            int i = s.indexOf(sub);
            if (i > -1) {
                sbRet.append(s.substring(0, i));
                sbRet.append(rep);
                s = s.substring(i + sub.length());
            } else {
                sbRet.append(s);
                s = "";
            }

        }
        return sbRet.toString();
    }

    /**
     * 是否为[0-9]|[a-z]|[A-Z]
     * 
     * @param c
     * @return
     */
    public static boolean isAlpha(char c) {
        if (c < MAPPER_LEN) {
            return charMapper[c].isAlpha;
        } else {
            return false;
        }
    }

    private static class CharMapper {
        boolean isAlpha;
        char upper;
        char lower;
    }

    private final static int MAPPER_LEN = 128;
    private final static CharMapper[] charMapper = new CharMapper[128];
    static {
        for (char c = 0; c < MAPPER_LEN; c++) {
            charMapper[c] = new CharMapper();
            charMapper[c].isAlpha = c <= 'Z' && c >= 'A' || c <= 'z' && c >= 'a' || c <= '9' && c >= '0' || c == '_';
            charMapper[c].upper = Character.toUpperCase(c);
            charMapper[c].lower = Character.toLowerCase(c);
        }
    }

}