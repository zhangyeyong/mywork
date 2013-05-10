package com.zte.jbundle.fre.expr.func.inner;

import java.util.List;

import com.zte.jbundle.fre.expr.Consts;
import com.zte.jbundle.fre.expr.ExprUtil;
import com.zte.jbundle.fre.expr.FuncException;
import com.zte.jbundle.fre.expr.func.IFunction;

public class FuncSum implements IFunction {

    public static final FuncSum instance = new FuncSum();

    private FuncSum() {
    }

    @Override
    public String getName() {
        return "sum";
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
        return sum;
    }
}
