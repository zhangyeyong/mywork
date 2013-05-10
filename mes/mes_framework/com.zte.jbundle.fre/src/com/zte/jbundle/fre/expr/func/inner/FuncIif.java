package com.zte.jbundle.fre.expr.func.inner;

import java.util.List;

import com.zte.jbundle.fre.expr.Consts;
import com.zte.jbundle.fre.expr.FuncException;
import com.zte.jbundle.fre.expr.func.IFunction;

public class FuncIif implements IFunction {

    public static final FuncIif instance = new FuncIif();

    private FuncIif() {
    }

    @Override
    public String getName() {
        return "iif";
    }

    @Override
    public Object invoke(List<Object> args) throws FuncException {
        if (args.size() != 3) {
            throw new FuncException(Consts.FN_IIF_ERR);
        }

        Object cond = args.get(0);
        if (cond instanceof Boolean) {
            if ((Boolean) cond) {
                return args.get(1);
            } else {
                return args.get(2);
            }
        } else {
            throw new FuncException(Consts.FN_IIF_ERR);
        }
    }
}
