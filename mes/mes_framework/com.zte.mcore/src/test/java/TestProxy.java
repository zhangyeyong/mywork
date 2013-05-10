import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import javassist.util.proxy.MethodHandler;
import javassist.util.proxy.ProxyFactory;

public class TestProxy {

    public static interface IType {
        void foo();
    }

    public static class CType implements IType {

        @Override
        public void foo() {
        }

    }

    static IType javaProxy() throws Exception {
        final IType itype = new CType();
        IType itype2 = (IType) Proxy.newProxyInstance(TestProxy.class.getClassLoader(), new Class<?>[] { IType.class },
                new InvocationHandler() {

                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                        return method.invoke(itype, args);
                    }
                });
        itype2.foo();
        return itype2;
    }

    public static IType javassist() throws Exception {
        final IType itype = new CType();

        ProxyFactory factory = new ProxyFactory();
        factory.setSuperclass(CType.class);
        IType itype2 = (IType) factory.create(null, null, new MethodHandler() {

            @Override
            public Object invoke(Object arg0, Method arg1, Method arg2, Object[] arg3) throws Throwable {
                return arg1.invoke(itype, arg3);
            }
        });
        return itype2;
    }

    public static void main(String[] args) throws Exception {
        long start = System.currentTimeMillis();
        // IType t = javaProxy();
        //IType t = javaProxy();
        IType t = new CType();
        for (int i = 0; i < 10000000; i++) {
            t.foo();
        }
        System.out.println("Times:" + (System.currentTimeMillis() - start));
    }

}
