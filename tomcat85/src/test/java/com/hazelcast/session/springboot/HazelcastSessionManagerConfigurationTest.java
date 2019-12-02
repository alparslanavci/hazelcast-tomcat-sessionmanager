package com.hazelcast.session.springboot;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static junit.framework.TestCase.assertNotNull;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(
  classes = Application.class)
public class HazelcastSessionManagerConfigurationTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Test()
    public void testHazelcastInstanceBean() {
        assertNotNull(applicationContext.getBean("hazelcastTomcatSessionManagerCustomizer"));
    }
}