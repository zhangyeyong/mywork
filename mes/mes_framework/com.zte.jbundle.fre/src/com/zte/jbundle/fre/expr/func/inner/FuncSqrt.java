package com.zte.jbundle.fre.expr.func.inner;

import java.util.List;

import com.zte.jbundle.fre.expr.Consts;
import com.zte.jbundle.fre.expr.ExprUtil;
import com.zte.jbundle.fre.expr.FuncException;
import com.zte.jbundle.fre.expr.func.IFunction;

public class FuncSqrt implements IFunction {

    public static final FuncSqrt instance = new FuncSqrt();

    private FuncSqrt() {
    }

    @Override
    public String getName() {
        return "sqrt";
    }

    @Override
    public Object invoke(List<Object> args) throws FuncException {
        Double x = null;
        if (args.size() != 1 || (x = ExprUtil.toNum(args.get(0))) == null) {
            throw new FuncException(Consts.FN_ARG_1NUM_ERR);
        }

        return Math.sqrt(x);
    }
}
