package com.zte.jbundle.home.ui.action;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class Action {

    protected Logger log = LoggerFactory.getLogger(getClass());
    protected HttpServletRequest req;
    protected HttpServletResponse resp;

    public void initContext(HttpServletRequest req, HttpServletResponse resp) {
        this.req = req;
        this.resp = resp;
    }

    protected static void fmt(StringBuilder sb, String format, Object... args) {
        sb.append(String.format(format, args));
    }
}
