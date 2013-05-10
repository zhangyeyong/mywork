package com.zte.jbundle.home.thirdPartApi;

import java.io.InputStream;

import com.caucho.hessian.client.HessianProxyFactory;
import com.caucho.hessian.io.AbstractSerializerFactory;
import com.caucho.hessian.io.Deserializer;
import com.caucho.hessian.io.HessianProtocolException;
import com.caucho.hessian.io.InputStreamDeserializer;
import com.caucho.hessian.io.InputStreamSerializer;
import com.caucho.hessian.io.Serializer;
import com.zte.jbundle.api.HessianProxier.IHessianProxiable;

public class HessianProxiable implements IHessianProxiable {

    @SuppressWarnings("rawtypes")
    public static class InternalSerializerFactory extends AbstractSerializerFactory {

        public Deserializer getDeserializer(Class clazz) throws HessianProtocolException {
            if (InputStream.class.isAssignableFrom(clazz)) {
                return new InputStreamDeserializer();
            }

            return null;
        }

        public Serializer getSerializer(Class clazz) throws HessianProtocolException {
            if (InputStream.class.isAssignableFrom(clazz)) {
                return new InputStreamSerializer();
            }
            return null;
        }

    }

    private static HessianProxyFactory factory = initHessianProxyFactory();

    @SuppressWarnings("unchecked")
    @Override
    public <T> T proxyIt(Class<T> clazz, String rsUrl) {
        try {
            return (T) factory.create(clazz, rsUrl, HessianProxyFactory.class.getClassLoader());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static HessianProxyFactory initHessianProxyFactory() {
        HessianProxyFactory factory = new HessianProxyFactory();
        factory.setChunkedPost(false);
        factory.setReadTimeout(5000);
        factory.setOverloadEnabled(false);
        factory.getSerializerFactory().addFactory(new InternalSerializerFactory());
        return factory;
    }

}
