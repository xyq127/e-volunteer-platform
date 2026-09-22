package com.evolunteer.config;

import com.evolunteer.service.impl.UserDetailsServiceImpl;
import com.evolunteer.utils.LoginSuccessHandler;
import com.evolunteer.utils.MD5PasswordEncoder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * E志愿志愿者服务平台 V1.0
 * <p>
 * 安全配置类：配置登录认证入口、登录成功跳转、注销入口以及公众页面、志愿者组织页面与平台管理员页面的访问权限。
 */
@Configuration
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    UserDetailsServiceImpl userDetailsService;

    /**
     * 配置登录认证使用的用户信息来源与密码校验规则
     *
     * @param auth 认证管理器
     */
    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsService).passwordEncoder(passwordEncoder());
    }

    /**
     * 配置登录、注销与页面访问权限
     *
     * @param http HTTP 安全配置
     */
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
                // 静态资源
                .antMatchers("/portal/js/**", "/portal/css/**", "/portal/images/**", "/portal/fonts/**").permitAll()
                .antMatchers("/console/assets/**").permitAll()
                // 公众页面
                .antMatchers("/portal/**", "/portal/index.html", "/index.html", "/", "/index").permitAll()
                // 志愿者注册与登录页面
                .antMatchers("/admin/sign-up.html", "/admin/login.html").permitAll()
                // 志愿者注册接口
                .antMatchers("/loginRegisterController/**").permitAll()
                // 志愿活动接口，志愿者组织与平台管理员共用
                .antMatchers("/activity/**").hasAnyRole("ADMIN", "ORGANIZATION")
                // 平台管理员页面与接口
                .antMatchers("/admin/**").hasRole("ADMIN")
                // 志愿者组织页面与接口
                .antMatchers("/org/**", "/participate/**").hasRole("ORGANIZATION")
                .anyRequest().authenticated();

        http.csrf().disable();
    }

    /**
     * 平台密码编码器
     *
     * @return 使用平台 MD5 摘要规则的密码编码器
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new MD5PasswordEncoder();
    }
}
