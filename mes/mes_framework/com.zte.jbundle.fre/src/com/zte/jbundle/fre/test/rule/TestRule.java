package com.zte.jbundle.fre.test.rule;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import com.zte.jbundle.fre.flow.caze.FlowCase;
import com.zte.jbundle.fre.flow.caze.NodeCase;
import com.zte.jbundle.fre.flow.define.FlowDefine;
import com.zte.jbundle.fre.flow.define.Node;

public class TestRule {

    static String ruleJson = getRuleJson();
    static User user = null;// 登录人
    static FlowDefine ruleDefine = null;

    private static String getRuleJson() {
        Scanner scanner = new Scanner(TestRule.class.getResourceAsStream("/com/zte/jbundle/fre/test/rule/rule.json"),
                "utf-8");
        String s = "";
        while (scanner.hasNext()) {
            s += " " + scanner.next();
        }
        return s;
    }

    static void genTask(Pen pen) {
        FlowCase caze = ruleDefine.openCase(pen.getRuleStatus(), null);
        String status = "";
        if (caze.isFinished()) {
            for (NodeCase node : caze.listEndingNodes()) {
                status = node.getNode().getExt();
                break;
            }
        } else {
            for (NodeCase node : caze.listWorkingNodes()) {
                Node n = node.getNode();
                PenTask.removeTask(pen, n.getId());
                String[] roles = n.getRoles().split(",");
                for (String role : roles) {
                    for (User u : User.listUsers(role)) {
                        PenTask.addTask(pen, role, u, n.getId());
                    }
                }

                status = n.getExt();
            }
        }

        pen.setStatus(status);
        pen.setRuleStatus(caze.getStatus());
    }

    static void execTask(PenTask penTask, String key, String val) {
        FlowCase caze = ruleDefine.openCase(penTask.getPen().getRuleStatus(), null);

        Map<String, Object> attrMap = new HashMap<String, Object>();
        if (key != null && val != null) {
            attrMap.put(key, val);
        }
        for (NodeCase nodeCase : caze.listWorkingNodes()) {
            if (penTask.getRuleNodeId() == nodeCase.getId()) {
                nodeCase.close(penTask.getRole(), attrMap);
            }
        }
        penTask.getPen().setRuleStatus(caze.getStatus());
        penTask.setStatus("DONE");

        genTask(penTask.getPen());
    }

    static void execTask(PenTask penTask) {
        execTask(penTask, null, null);
    }

    public static void main(String[] args) throws Exception {
        ruleDefine = FlowDefine.parse(ruleJson);

        for (Pen pen : Pen.pens) {
            genTask(pen);
        }

        System.out.print(">");
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        while (true) {
            try {
                String[] cmds = br.readLine().split(" ");
                if (cmds.length == 0) {
                    System.out.println("错误的命令，如需帮助输入命令 h");
                } else if (cmds[0].equalsIgnoreCase("h") && cmds.length == 1) {
                    System.out.println("h   帮助");
                    System.out.println("u   显示用户");
                    System.out.println("l   登录，格式：l 用户编号，如： l corer");
                    System.out.println("p   显示所有的Pen，如： p");
                    System.out.println("ta  显示当前待处理任务，如： ta");
                    System.out.println("do  执行任务，格式：do 任务Id 参数代号 参数值，如： do 1 pass Y");
                } else if (cmds[0].equalsIgnoreCase("p") && cmds.length == 1) {
                    System.out.println("Pen列表：");
                    for (Pen p : Pen.pens) {
                        System.out.println(p);
                    }
                } else if (cmds[0].equalsIgnoreCase("u") && cmds.length == 1) {
                    System.out.println("当前用户：" + user + "，所有用户：" + User.users);
                } else if (cmds[0].equalsIgnoreCase("l") && cmds.length == 2) {
                    user = User.getUser(cmds[1]);
                    if (user == null) {
                        System.out.println("用户不存在，已有用户：" + User.users);
                    } else {
                        System.out.println("欢迎你：" + user);
                    }
                } else if (cmds[0].equalsIgnoreCase("ta") && cmds.length == 1) {
                    if (user == null) {
                        System.out.println("请先用  l 命令登录");
                    } else {
                        System.out.println("你当前待处理任务有：");
                        for (PenTask pt : PenTask.getUserTasks(user)) {
                            if ("INIT".equals(pt.getStatus()))
                                System.out.println(pt);
                        }
                    }
                } else if (cmds[0].equalsIgnoreCase("do") && cmds.length >= 2) {
                    if (user == null) {
                        System.out.println("请先用  l 命令登录");
                    } else {
                        PenTask task = null;
                        for (PenTask pt : PenTask.getUserTasks(user)) {
                            if ((pt.getId() + "").equals(cmds[1])) {
                                task = pt;
                                break;
                            }
                        }
                        if (task == null) {
                            System.out.println("任务(id=" + cmds[1] + ")不存在");
                        } else if (!"INIT".equals(task.getStatus())) {
                            System.out.println("任务(id=" + cmds[1] + ")不可执行，可能已经完成");
                        } else {
                            if (cmds.length >= 4) {
                                execTask(task, cmds[2], cmds[3]);
                            } else {
                                execTask(task);
                            }
                        }
                    }
                } else {
                    System.out.println("错误的命令，如需帮助输入命令 h");
                }
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println();
            }
            System.out.print(">");

        }
    }
}
