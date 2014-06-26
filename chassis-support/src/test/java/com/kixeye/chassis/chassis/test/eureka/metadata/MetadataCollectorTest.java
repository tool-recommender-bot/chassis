package com.kixeye.chassis.chassis.test.eureka.metadata;

import com.google.common.collect.Iterators;
import com.kixeye.chassis.chassis.eureka.MetadataCollector;
import com.netflix.config.ConfigurationManager;
import com.netflix.config.DynamicPropertyFactory;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.StandardEnvironment;

import java.util.HashMap;
import java.util.Map;

public class MetadataCollectorTest {

    private AnnotationConfigApplicationContext context;

    @Before
    public void setup() {
        ConfigurationManager.getConfigInstance().setProperty("chassis.eureka.disable","true");
        ConfigurationManager.getConfigInstance().setProperty("eureka.metadata.prop1", "propValue");
        ConfigurationManager.getConfigInstance().setProperty("eureka.datacenter", "default");

        StandardEnvironment environment = new StandardEnvironment();
        environment.getPropertySources().addFirst(new ArchaiusSpringPropertySource());
        context = new AnnotationConfigApplicationContext();
        context.setEnvironment(environment);
        context.register(MetadataCollectorConfiguration.class);
        context.refresh();
        context.start();
    }

    @After
    public void tearDown() {
        if (context != null) {
            context.close();
            context = null;
        }
    }

    @Test
    public void collectTest() {
        MetadataCollector collector = context.getBean(MetadataCollector.class);
        Map<String,String> map = collector.getMetadataMap();

        Assert.assertEquals("propValue",map.get("prop1"));
        Assert.assertEquals("value1",map.get("test1"));
        Assert.assertEquals("value2",map.get("test2"));
    }

    class ArchaiusSpringPropertySource extends EnumerablePropertySource<Map<String, Object>> {
        public ArchaiusSpringPropertySource() {
            super("archius", new HashMap<String, Object>());
        }

        @Override
        public String[] getPropertyNames() {
            return Iterators.toArray(ConfigurationManager.getConfigInstance().getKeys(), String.class);
        }

        @Override
        public Object getProperty(String name) {
            return DynamicPropertyFactory.getInstance().getStringProperty(name, null).get();
        }
    }
}
