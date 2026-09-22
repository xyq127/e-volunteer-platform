package com.evolunteer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * E志愿志愿者服务平台 V1.0
 * <p>
 * 平台启动类：面向志愿者、志愿者组织与平台管理员，提供志愿活动申报、活动审核、志愿者报名、
 * 可信签到与志愿服务时长核算、供需匹配、志愿服务证明等服务的志愿活动管理平台。
 * 启用定时任务用于按活动时间自动流转活动状态并释放未按期确认参加的报名名额。
 */
@EnableScheduling
@SpringBootApplication
public class EVolunteerApplication {

    public static void main(String[] args) {
        SpringApplication.run(EVolunteerApplication.class, args);
    }
}
