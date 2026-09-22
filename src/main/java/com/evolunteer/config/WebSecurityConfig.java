package com.evolunteer.config;

import com.evolunteer.service.impl.PasswordUpgradeServiceImpl;
import com.evolunteer.service.impl.UserDetailsServiceImpl;
import com.evolunteer.utils.LoginSuccessHandler;
import com.evolunteer.utils.SecurePasswordEncoder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    UserDetailsServiceImpl userDetailsService;

    @Autowired
    PasswordUpgradeServiceImpl passwordUpgradeService;

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsService)
                .passwordEncoder(passwordEncoder())
                .userDetailsPasswordManager(passwordUpgradeService);
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {

        http
                .formLogin()
                .loginPage("/admin/login.html")
                .loginProcessingUrl("/login")
                .usernameParameter("username")
                .passwordParameter("password")
                .successHandler(new LoginSuccessHandler());

        http
                .logout()
                .logoutUrl("/logout")
                .logoutSuccessUrl("/admin/login.html")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID");

        http
                .authorizeRequests()

                .antMatchers("/portal/js/**", "/portal/css/**", "/portal/images/**", "/portal/fonts/**").permitAll()
                .antMatchers("/console/assets/**").permitAll()

                .antMatchers("/portal/**", "/portal/index.html", "/index.html", "/", "/index").permitAll()

                .antMatchers("/admin/sign-up.html", "/admin/login.html").permitAll()

                .antMatchers("/loginRegisterController/**").permitAll()

                .antMatchers("/certificate/**").permitAll()

                .antMatchers("/file/download/**").permitAll()

                .antMatchers("/portal/site/**").permitAll()

                .antMatchers("/volunteer/**").hasRole("VOLUNTEER")
                .antMatchers("/checkin/in", "/checkin/out").hasRole("VOLUNTEER")

                .antMatchers("/checkin/**").hasRole("ORGANIZATION")

                .antMatchers("/activity/**").hasAnyRole("ADMIN", "ORGANIZATION")

                .antMatchers("/admin/**").hasRole("ADMIN")

                .antMatchers("/org/**", "/participate/**").hasRole("ORGANIZATION")
                .anyRequest().authenticated();

        http.csrf().disable();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new SecurePasswordEncoder();
    }
}
