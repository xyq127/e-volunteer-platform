package com.evolunteer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * E志愿志愿者服务平台 V1.0
 * <p>
 * 平台启动类：面向志愿者、志愿者组织与平台管理员，提供志愿活动申报、活动审核、志愿者报名、
 * 组织资料维护与志愿服务数据管理等服务。
 */
@SpringBootApplication
public class EVolunteerApplication {

    public static void main(String[] args) {
        SpringApplication.run(EVolunteerApplication.class, args);
    }
}
