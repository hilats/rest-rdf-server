package com.hilats.server.spring;

import com.hilats.server.spring.jwt.HilatsUserDetailsService;
import com.hilats.server.spring.jwt.HilatsUserService;
import com.hilats.server.spring.jwt.StatelessAuthenticationFilter;
import com.hilats.server.spring.jwt.TokenAuthenticationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.security.config.annotation.SecurityConfigurer;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@PropertySource("classpath:application.properties")
@Order(5)
public class SpringSecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    Environment env;

    @Autowired
    private HilatsUserDetailsService userService;

    @Autowired
    private TokenAuthenticationService tokenAuthenticationService;

    @Autowired
    private SecurityConfigurer securityConfigurer;

    public SpringSecurityConfig() {
        super(true);
        //this.userService = new UserService();
        //tokenAuthenticationService = new TokenAuthenticationService("tooManySecrets", userService);
    }



    @Override
    @Autowired
    public void setApplicationContext(ApplicationContext context) {
        super.setApplicationContext(context);

        this.userService = context.getBean(HilatsUserDetailsService.class);
        //TODO why this crap? why isn't the class lookup functioning ?
        this.tokenAuthenticationService = (TokenAuthenticationService)context.getBean("myTokenAuthenticationService");
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {

        boolean allowAnonymous = env.getProperty("auth.allowAnonymous", Boolean.class, false);

        http
                .exceptionHandling().and()
                .anonymous().and()
                .servletApi().and()
                .headers().cacheControl().and()
                .httpBasic().and() //enable basic authentication, but users defined elsewhere
                .authorizeRequests()

                    // Allow anonymous resource requests
                    .antMatchers("/").permitAll()
                    .antMatchers("/favicon.ico").permitAll()
                    .antMatchers("/**/*.html").permitAll()
                    .antMatchers("/**/*.css").permitAll()
                    .antMatchers("/**/*.js").permitAll()

                    // Allow anonymous logins
                    .antMatchers("/api/auth/**").permitAll()

                // Custom Token based authentication based on the header previously given to the client
                .and().addFilterBefore(new StatelessAuthenticationFilter(tokenAuthenticationService), UsernamePasswordAuthenticationFilter.class);

        // if anonymous, all authorization is left up to REST resources
        if (!allowAnonymous)
            // All other request need to be authenticated
            http.authorizeRequests().anyRequest().authenticated();
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        if (securityConfigurer != null)
            securityConfigurer.configure(auth);

        auth.userDetailsService(userDetailsService()).passwordEncoder(new BCryptPasswordEncoder());
    }


    @Bean
    @Override
    public UserDetailsService userDetailsService() {
        return userService;
    }

    @Bean
    public TokenAuthenticationService tokenAuthenticationService() {
        return tokenAuthenticationService;
    }
}
