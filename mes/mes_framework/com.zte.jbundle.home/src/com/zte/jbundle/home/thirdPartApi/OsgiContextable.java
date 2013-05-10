package com.zte.jbundle.home.thirdPartApi;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

import com.zte.jbundle.api.OsgiContext.IOsgiContextable;
import com.zte.jbundle.home.internal.Activator;

public class OsgiContextable implements IOsgiContextable {

    @SuppressWarnings("unchecked")
    @Override
    public <T> List<T> getAllServices(Class<T> clazz) {
        try {
            List<T> ret = new ArrayList<T>();
            ServiceReference<?>[] references = Activator.getContext().getAllServiceReferences(clazz.getName(), null);
            if (references == null || references.length == 0) {
                return ret;
            }

            for (ServiceReference<?> ref : references) {
                Object o = Activator.getContext().getService(ref);
                if (clazz.isInstance(o)) {
                    ret.add((T) o);
                }
            }
            return ret;
        } catch (InvalidSyntaxException e) {
            return Collections.emptyList();
        }
    }
}
