package com.zte.jbundle.fre.test.rule;

import java.util.ArrayList;
import java.util.List;

public class User {

    private String num;
    private String name;
    private String role;

    public User(String name, String role) {
        this.num = role;
        this.role = role;
        this.name = name;
    }

    @Override
    public String toString() {
        return num + ":" + name;
    }

    public String getNum() {
        return num;
    }

    public void setNum(String num) {
        this.num = num;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public static final List<User> users = initUsers();

    public static User getUser(String userNum) {
        for (User u : users) {
            if (u.getNum().equalsIgnoreCase(userNum)) {
                return u;
            }
        }
        return null;
    }

    public static List<User> listUsers(String role) {
        List<User> ret = new ArrayList<User>();
        for (User u : users) {
            if (u.role.equalsIgnoreCase(role)) {
                ret.add(u);
            }
        }
        return ret;
    }

    private static List<User> initUsers() {
        List<User> ret = new ArrayList<User>();
        ret.add(new User("笔芯安装工", "corer"));
        ret.add(new User("笔帽安装工", "capper"));
        ret.add(new User("测试员", "tester"));
        ret.add(new User("包装员", "wrapper"));
        return ret;
    }

}
