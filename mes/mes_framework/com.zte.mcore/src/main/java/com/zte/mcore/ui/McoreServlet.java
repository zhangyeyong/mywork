package com.zte.mcore.ui;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.codehaus.jackson.map.ObjectMapper;

import com.zte.mcore.remote.impl.RemotableByHessian;
import com.zte.mcore.ui.action.LoginAction;
import com.zte.mcore.utils.Logger;

public class McoreServlet extends HttpServlet {

    private static final long serialVersionUID = -1770609916447510718L;
    static Logger log = Logger.getLogger(McoreServlet.class);
    static String ACTION_PKG = LoginAction.class.getPackage().getName() + ".";
    ServletConfig cfg;

    static class McoreUri {

        public String head;
        public boolean isAsset;
        public String fname;
        public String method;

        public static McoreUri parse(HttpServletRequest req, HttpServletResponse resp) {
            McoreUri ret = new McoreUri();
            String uri = req.getRequestURI();
            int i = uri.indexOf("/m-core/");
            if (i < 0) {
                return null;
            }

            ret.head = uri.substring(0, i + 1);
            String end = uri.substring(i + "/m-core/".length());
            String s = end.toLowerCase();
            ret.isAsset = false;
            if (s.endsWith(".css") || s.endsWith(".js") || s.endsWith(".html") || s.endsWith(".htm")) {
                ret.isAsset = true;
                ret.fname = end;
            } else {
                i = end.indexOf("/");
                if (i < 0) {
                    return null;
                }

                ret.fname = end.substring(0, i);
                ret.method = end.substring(i + 1);
            }
            return ret;
        }

        public static String toAssetUrl(HttpServletRequest req, String fName) {
            String uri = req.getRequestURI();
            int i = uri.indexOf("/m-core/");
            if (i > -1) {
                uri = uri.substring(0, i);
            }
            while (uri.endsWith("/")) {
                uri = uri.substring(0, uri.length() - 1);
            }
            while (fName.startsWith("/")) {
                fName = fName.substring(1);
            }
            return uri + "/m-core/" + fName;
        }
    }

    @Override
    public void init(ServletConfig cfg) throws ServletException {
        super.init();
        this.cfg = cfg;
    }

    private Object createAction(String action, HttpServletRequest req, HttpServletResponse resp) {
        String clazzName = ACTION_PKG + action;
        try {
            return Class.forName(clazzName).newInstance();
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (RemotableByHessian.handledAsHessian(req, resp, cfg)) {
            return;
        }

        McoreUri mcu = McoreUri.parse(req, resp);
        if (handleAsset(req, resp, mcu)) {
            return;
        }

        String actionName = mcu == null ? "" : mcu.fname;
        String method = mcu == null ? "" : mcu.method;
        Object action = createAction(actionName, req, resp);
        if (action == null) {
            if (LoginAction.isLogined(req)) {
                resp.sendRedirect(McoreUri.toAssetUrl(req, "config.html"));
            } else {
                resp.sendRedirect(McoreUri.toAssetUrl(req, "login.html"));
            }
            return;
        }

        Map<String, Object> respMap = new HashMap<String, Object>();
        respMap.put("err", false);
        try {
            Method am = null;
            Method ifCheckLogin = null;
            Class<?>[] prmTypes = null;
            Set<Method> methods = new HashSet<Method>(Arrays.asList(action.getClass().getMethods()));
            methods.addAll(Arrays.asList(action.getClass().getDeclaredMethods()));

            for (Method m : methods) {
                if (m.getName().equalsIgnoreCase("ifCheckLogin") && m.getParameterTypes().length == 0) {
                    ifCheckLogin = m;
                    break;
                }
            }

            for (Method m : methods) {
                prmTypes = m.getParameterTypes();
                if (!m.getName().equals(method)) {
                    continue;
                }

                boolean matched = prmTypes.length < 3;
                for (int i = 0; matched && i < prmTypes.length; i++) {
                    Class<?> p = prmTypes[i];
                    if (!p.isAssignableFrom(HttpServletRequest.class) && !p.isAssignableFrom(HttpServletResponse.class)) {
                        matched = false;
                    }
                }

                if (matched) {
                    am = m;
                    break;
                }
            }

            if (am == null) {
                throw new RuntimeException("[!_!] Can't locate method " + ACTION_PKG + actionName + "." + method);
            }

            boolean checkLogin = true;
            if (ifCheckLogin != null) {
                try {
                    ifCheckLogin.setAccessible(true);
                    Object o = ifCheckLogin.invoke(action);
                    if (o instanceof Boolean) {
                        checkLogin = (Boolean) o;
                    } else {
                        checkLogin = false;
                    }
                } catch (Exception NULL) {
                }
            }

            if (checkLogin && !LoginAction.isLogined(req)) {
                respMap.put("relogin", true);
                responseJson(req, resp, respMap);
                return;
            }

            Object[] reqArgs = new Object[prmTypes.length];
            for (int i = 0; i < prmTypes.length; i++) {
                if (prmTypes[i].isAssignableFrom(HttpServletRequest.class)) {
                    reqArgs[i] = req;
                } else {
                    reqArgs[i] = resp;
                }
            }
            am.setAccessible(true);
            Object ret = am.invoke(action, reqArgs);
            if (ret != null) {
                respMap.put("ret", ret);
                responseJson(req, resp, respMap);
            }
        } catch (Throwable e) {
            if (e instanceof InvocationTargetException) {
                e = ((InvocationTargetException) e).getTargetException();
            }

            respMap.put("err", true);
            respMap.put("msg", e.getMessage());
            responseJson(req, resp, respMap);
            log.error("[!_!] parse request uri(" + req.getRequestURI() + ") failed!" + e);
        }
    }

    boolean handleAsset(HttpServletRequest req, HttpServletResponse resp, McoreUri mcu) throws IOException {
        if (mcu == null || !mcu.isAsset) {
            return false;
        }

        resp.reset();
        resp.setHeader("Content-disposition", "inline");
        resp.setContentType(req.getSession().getServletContext().getMimeType(mcu.fname));
        String resUrl = "com/zte/mcore/ui/page/" + mcu.fname;
        InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream(resUrl);
        OutputStream out = resp.getOutputStream();
        try {
            byte[] buffer = new byte[1024 * 1024];
            for (int len; (len = in.read(buffer)) > 0;) {
                out.write(buffer, 0, len);
            }
        } finally {
            out.close();
            in.close();
        }
        return true;
    }

    private void responseJson(HttpServletRequest req, HttpServletResponse resp, Map<String, Object> respMap)
            throws IOException {
        String json = new ObjectMapper().writeValueAsString(respMap);
        resp.setHeader("Cache-Control", "no-cache");
        resp.setDateHeader("Expires", 0);
        resp.setHeader("Pragma", "no-cache");
        resp.setContentType("text/plain; charset=utf-8");
        resp.getWriter().println(json);
    }

}
