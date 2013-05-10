package com.zte.jbundle.fre.test.expr;

import java.util.HashMap;
import java.util.Map;

import com.zte.jbundle.fre.expr.ExprEngine;
import com.zte.jbundle.fre.expr.ExprException;

public class TestExpr {

    static class BeanParam {

        private int field = 3;

        public BeanParam(int field) {
            this.field = field;
        }

        public int getField() {
            return field;
        }
    }

    static String testExpr2 = "max(1000, iif(i<10000, 500, 1000))+100.0*99-(600-3*15)/(((68-9)-3)*2-100)+10000%7*71";
    static String testExpr = "max(1,2,3)+iif(min(paraMap.A.field,paraMap.B.field) == paraMap.B.field, 1, 6)";
    static Object ret = null;
    static ExprEngine eng = null;

    public static void main(String[] args) throws ExprException {
        System.out.println(testExpr.length() + ": " + testExpr);
        long b = System.currentTimeMillis();
        for (int i = 0; i < 100000; i++) {
            eng = new ExprEngine(testExpr);
        }
        long p = System.currentTimeMillis();

        Map<String, BeanParam> paraMap = new HashMap<String, BeanParam>();
        paraMap.put("A", new BeanParam(1));
        paraMap.put("b", new BeanParam(2));
        paraMap.put("C", new BeanParam(3));
        eng.setParam("paraMap", paraMap);
        for (int i = 0; i < 100000; i++) {
            ret = eng.setParam("i", 10000).asNumber();
        }
        long e = System.currentTimeMillis();
        System.out.println("parse=" + (p - b) + "ms,calc:" + (e - p) + "ms,all:" + (e - b) + "ms,result:" + ret);
    }

}
