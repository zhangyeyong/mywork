package com.zte.jbundle.fre.expr.func.inner;

import java.util.List;

import com.zte.jbundle.fre.expr.Consts;
import com.zte.jbundle.fre.expr.ExprUtil;
import com.zte.jbundle.fre.expr.FuncException;
import com.zte.jbundle.fre.expr.func.IFunction;

public class FuncAvg implements IFunction {

    public static final FuncAvg instance = new FuncAvg();

    private FuncAvg() {
    }

    @Override
    public String getName() {
        return "avg";
    }

    @Override
    public Object invoke(List<Object> args) throws FuncException {
        if (args.size() < 1) {
            throw new FuncException(Consts.FN_ARG_NO_ERR);
        }

        double sum = 0;
        for (Object arg : args) {
            Double d = null;
            if ((d = ExprUtil.toNum(arg)) == null) {
                throw new FuncException(Consts.FN_ARG_NUM_ERR);
            }
            sum += d;
        }
        return sum / args.size();
    }
}
