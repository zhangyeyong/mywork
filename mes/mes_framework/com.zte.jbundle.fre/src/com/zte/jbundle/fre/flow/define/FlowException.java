package com.zte.jbundle.fre.flow.define;

@SuppressWarnings("serial")
public class FlowException extends RuntimeException {

    public FlowException() {
    }

    public FlowException(String msg) {
        super(msg);
    }

    public FlowException(Exception e) {
        super(e);
    }

}
