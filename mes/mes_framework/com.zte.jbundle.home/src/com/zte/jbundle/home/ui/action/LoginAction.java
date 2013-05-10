package com.zte.jbundle.home.ui.action;

import javax.servlet.http.HttpServletRequest;


import com.zte.jbundle.cfg.HomeCfg;
import com.zte.jbundle.home.ui.JBundleConsole;
import com.zte.jbundle.home.utils.JBundleUitls;

public class LoginAction extends Action {

    public final static String URL = JBundleConsole.ROOT_URI + "/" + JBundleUitls.getNoPackageName(LoginAction.class);

    public String login(String username, String password) {
        username = username == null ? "" : username;
        password = password == null ? "" : password;
        boolean ret = username.equals(HomeCfg.getInstance().getUsername())
                && password.equals(HomeCfg.getInstance().getPassword());

        if (ret) {
            String sessionId = JBundleUitls.uuid();
            req.getSession().setAttribute("ibundle.login", sessionId);
            return sessionId;
        }
        return null;
    }

    public static boolean isLogined(HttpServletRequest req) {
        return req.getSession().getAttribute("ibundle.login") != null;
    }

}
