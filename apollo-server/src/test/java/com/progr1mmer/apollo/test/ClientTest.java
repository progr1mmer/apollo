package com.progr1mmer.apollo.test;

import com.ctrip.framework.apollo.Config;
import com.ctrip.framework.apollo.ConfigChangeListener;
import com.ctrip.framework.apollo.ConfigService;
import com.ctrip.framework.apollo.model.ConfigChangeEvent;

/**
 * @author progr1mmer.
 * @date Created on 2019/11/16.
 */
public class ClientTest {

    public static void main(String[] args) throws Exception {
        System.setProperty("app.id", "SampleApp");
        System.setProperty("env", "DEV");
        System.setProperty("apollo.meta", "http://127.0.0.1:8080");
        Config config = ConfigService.getAppConfig(); //config instance is singleton for each namespace and is never null
        String someKey = "timeout";
        String someDefaultValue = "200";
        String value = config.getProperty(someKey, someDefaultValue);
        System.out.println(value);
        config.addChangeListener(changeEvent -> System.out.println(changeEvent.changedKeys()));
        Thread.sleep(888888);
    }

}
