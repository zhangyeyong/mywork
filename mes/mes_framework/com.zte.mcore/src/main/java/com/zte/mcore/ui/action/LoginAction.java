package com.zte.mcore.ui.action;

import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.zte.mcore.cfg.McoreCfg;
import com.zte.mcore.utils.NullU;
import com.zte.mcore.utils.StringU;

public class LoginAction {

    public boolean ifCheckLogin() {
        return false;
    }

    public boolean login(HttpServletRequest req, HttpServletResponse resp) {
        String username = NullU.nvl(req.getParameter("u"));
        String password = NullU.nvl(req.getParameter("p"));
        boolean ret = username.equals(McoreCfg.getInstance().getUsername())
                && password.equals(McoreCfg.getInstance().getPassword());
        if (ret) {
            String sessionId = StringU.uuid();
            req.getSession().setAttribute("mcore.login", sessionId);
            return true;
        }
        return false;
    }

    public static boolean isLogined(HttpServletRequest req) {
        return req.getSession().getAttribute("mcore.login") != null;
    }

    public static void main(String[] args) throws IOException {
        Enumeration<URL> urls = LoginAction.class.getClassLoader().getResources("com/zte/mcore/ui/action");
        while (urls.hasMoreElements()) {
            URL url = urls.nextElement();
            System.out.println(url);
        }
    }
}
