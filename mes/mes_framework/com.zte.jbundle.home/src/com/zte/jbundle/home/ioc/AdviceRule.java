package com.zte.jbundle.home.ioc;

import com.zte.jbundle.api.Advice;
import com.zte.jbundle.api.AdviceBuilder;

public class AdviceRule {

    private final AdviceBuilder builder;
    private final Bean builderBean;

    public AdviceRule(Bean builderBean) {
        this.builder = (AdviceBuilder) builderBean.getProxy();
        this.builderBean = builderBean;
    }

    public Advice[] advices() {
        return builder.advices();
    }

    public boolean isBean(Bean bean) {
        return builderBean.uid == bean.uid;
    }

    public boolean matchesRule(String className) {
        return builder.ifAdvised(className);
    }

}
