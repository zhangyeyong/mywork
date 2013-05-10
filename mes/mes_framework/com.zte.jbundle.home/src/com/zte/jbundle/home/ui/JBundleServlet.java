package com.zte.jbundle.home.ui;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URLDecoder;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zte.jbundle.home.ui.action.Action;
import com.zte.jbundle.home.ui.action.ConfigAction;
import com.zte.jbundle.home.ui.action.Download;
import com.zte.jbundle.home.ui.action.LoggerAction;
import com.zte.jbundle.home.ui.action.LoginAction;
import com.zte.jbundle.home.ui.action.PluginAction;
import com.zte.jbundle.home.utils.JBundleUitls;

public class JBundleServlet extends HttpServlet {

    private static final long serialVersionUID = -1770609916447510718L;
    static Logger log = LoggerFactory.getLogger(JBundleServlet.class);

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doPost(req, resp);
    }

    private Action createAction(String action, HttpServletRequest req, HttpServletResponse resp) throws Exception {
        String clazzName = JBundleUitls.getPackage(getClass()) + ".action." + action;
        try {
            @SuppressWarnings("unchecked")
            Class<Action> renderClass = (Class<Action>) Class.forName(clazzName);
            Action ret = renderClass.newInstance();
            ret.initContext(req, resp);
            return ret;
        } catch (Exception e) {
            throw new Exception("No Action[" + clazzName + "]");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String uri = req.getRequestURI();
        if (!uri.startsWith(LoginAction.URL) && !uri.startsWith(LoggerAction.URL) && !uri.startsWith(ConfigAction.URL)
                && !uri.startsWith(PluginAction.URL)) {
            if (LoginAction.isLogined(req)) {
                resp.sendRedirect("/jbundle/page/plugin.html");
            } else {
                resp.sendRedirect("/jbundle/page/login.html");
            }
            return;
        }
        if (!uri.startsWith(LoginAction.URL) && !uri.startsWith(LoggerAction.URL) && !LoginAction.isLogined(req)) {
            resp.getWriter().println("{\"login\": \"location.href='/jbundle/page/login.html';\"}");
            return;
        }

        String param = uri.substring(JBundleConsole.ROOT_URI.length());
        if (param.startsWith("/")) {
            param = param.substring(1);
        }
        String[] params = param.split("/");

        boolean isDownload = false;
        Map<String, Object> respMap = new HashMap<String, Object>();
        respMap.put("ret", "Y");
        respMap.put("login", false);
        try {
            Action action = createAction(params[0], req, resp);
            String methodName = params[1];
            Method method = null;
            Set<Method> methods = new HashSet<Method>(Arrays.asList(action.getClass().getMethods()));
            methods.addAll(Arrays.asList(action.getClass().getDeclaredMethods()));
            for (Method m : methods) {
                Class<?>[] prmTypes = m.getParameterTypes();
                if (prmTypes.length >= params.length - 2 && m.getName().equalsIgnoreCase(methodName)) {
                    method = m;
                    break;
                }
            }

            Object[] reqArgs = new Object[method.getParameterTypes().length];
            for (int i = 2; i < params.length; i++) {
                reqArgs[i - 2] = URLDecoder.decode(params[i], "utf-8");
            }
            isDownload = Download.class.isAssignableFrom(method.getReturnType());
            method.setAccessible(true);
            if (isDownload) {
                responseFile(req, resp, (Download) method.invoke(action, reqArgs));
            } else if (method.getReturnType() == void.class) {
                method.invoke(action, reqArgs);
                responseJson(req, resp, respMap);
            } else {
                respMap.put("data", method.invoke(action, reqArgs));
                responseJson(req, resp, respMap);
            }
        } catch (Throwable e) {
            if (e instanceof InvocationTargetException) {
                e = ((InvocationTargetException) e).getTargetException();
            }
            if (!isDownload) {
                respMap.put("ret", "N");
                respMap.put("msg", e.getMessage());
                responseJson(req, resp, respMap);
            }
            if (e instanceof BizException) {
                log.error("[!_!] parse request(" + param + ") failed!" + e);
            } else {
                log.error("[!_!] parse request(" + param + ") failed!", e);
            }
        }
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

    private void responseFile(HttpServletRequest req, HttpServletResponse resp, Download down) throws IOException {
        resp.reset();
        String fileName = new String(down.getTitle().getBytes("UTF-8"), "ISO8859_1");
        resp.setHeader("Content-disposition", "attachment; filename=" + fileName);
        resp.setContentType(req.getSession().getServletContext().getMimeType(fileName));
        try {
            FileInputStream in = new FileInputStream(down.getFile());
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
        } finally {
            if (down.isDoneDelete()) {
                down.getFile().delete();
            }
        }
    }

}
