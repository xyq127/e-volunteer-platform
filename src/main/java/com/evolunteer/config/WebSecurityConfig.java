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

/**
 * E志愿志愿者服务平台 V1.0
 * <p>
 * 安全配置类：配置登录认证入口、登录成功跳转、注销入口，以及公众页面、志愿者页面、
 * 志愿者组织页面与平台管理员页面的访问权限，并在历史账号登录成功后自动升级密码密文。
 */
@Configuration
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    UserDetailsServiceImpl userDetailsService;

    @Autowired
    PasswordUpgradeServiceImpl passwordUpgradeService;

    /**
     * 配置登录认证使用的用户信息来源、密码校验规则与密码升级服务
     *
     * @param auth 认证管理器
     */
    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsService)
                .passwordEncoder(passwordEncoder())
                .userDetailsPasswordManager(passwordUpgradeService);
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
                // 公众页面：门户首页与志愿服务证明校验入口
                .antMatchers("/portal/**", "/portal/index.html", "/index.html", "/", "/index").permitAll()
                // 志愿者注册与登录页面
                .antMatchers("/admin/sign-up.html", "/admin/login.html").permitAll()
                // 志愿者注册接口
                .antMatchers("/loginRegisterController/**").permitAll()
                // 志愿服务证明校验接口，供第三方核验证明真伪
                .antMatchers("/certificate/**").permitAll()
                // 通知公告附件与志愿秀图片下载
                .antMatchers("/file/download/**").permitAll()
                // 门户公开数据接口：通知公告、活动风采、志愿者组织与志愿秀
                .antMatchers("/portal/site/**").permitAll()
                // 志愿者页面与志愿者本人的签到签退接口
                .antMatchers("/volunteer/**").hasRole("VOLUNTEER")
                .antMatchers("/checkin/in", "/checkin/out").hasRole("VOLUNTEER")
                // 志愿者组织的签到复核、签到码与活动结算接口
                .antMatchers("/checkin/**").hasRole("ORGANIZATION")
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
     * @return 使用加盐摘要规则的密码编码器，可校验历史 MD5 摘要密文并触发升级
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new SecurePasswordEncoder();
    }
}
