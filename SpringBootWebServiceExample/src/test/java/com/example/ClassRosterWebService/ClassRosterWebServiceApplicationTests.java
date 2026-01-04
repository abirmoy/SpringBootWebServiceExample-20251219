package com.example.ClassRosterWebService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class ClassRosterWebServiceApplicationTests {

    @Autowired
    private ApplicationContext context;

    @Test
    void contextLoads() {
        assertThat(context).isNotNull();
    }

    @Test
    void mainApplicationBeanExists() {
        assertThat(context.containsBean("classRosterWebServiceApplication")).isTrue();
    }

    @Test
    void securityConfigBeanExists() {
        assertThat(context.containsBean("securityConfig")).isTrue();
    }
}