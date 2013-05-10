package com.zte.mcore.ioc.impl;

import java.lang.reflect.Field;

class IocEntry {

    private final Field field;
    private final Object target;
    private Object value = null;
    private final String resName;

    public IocEntry(Object target, Field field, String resName) {
        this.target = target;
        this.field = field;
        this.resName = resName;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        field.setAccessible(true);
        try {
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        this.value = value;
    }

    public String getResName() {
        return resName;
    }

    public Field getField() {
        return field;
    }

    @Override
    public String toString() {
        return target.getClass().getName() + "->" + field.getName() + "[" + field.getType().getName() + "]";
    }

    public Object getTarget() {
        return target;
    }

}
