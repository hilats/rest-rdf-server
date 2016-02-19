package com.hilats.server.spring;

import com.hilats.server.spring.jwt.HilatsUserDetailsService;
import org.springframework.security.config.annotation.SecurityBuilder;
import org.springframework.security.config.annotation.SecurityConfigurer;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;

/**
 * Created by pduchesne on 7/02/16.
 */
public class TestSecurityConfigurer implements SecurityConfigurer
{
    private HilatsUserDetailsService userService;

    public TestSecurityConfigurer(HilatsUserDetailsService userService) {
        this.userService = userService;
    }

    @Override
    public void init(SecurityBuilder builder) throws Exception {

    }

    @Override
    public void configure(SecurityBuilder builder) throws Exception {
        ((AuthenticationManagerBuilder)builder)
                .userDetailsService(userService).and()
                .inMemoryAuthentication()
                .withUser("test").password("test").roles("USER");
    }
}
