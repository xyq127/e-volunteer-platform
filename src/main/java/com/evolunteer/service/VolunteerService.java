package com.evolunteer.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.evolunteer.entity.Volunteer;

/**
 * E志愿志愿者服务平台 V1.0
 * <p>
 * 志愿者业务接口：提供志愿者手机号查重与志愿者注册能力。
 */
/**
 * E志愿志愿者服务平台 V1.0
 * <p>
 * 志愿者业务接口，基于 MyBatis-Plus 提供志愿者数据的通用增删改查能力。
 */
public interface VolunteerService extends IService<Volunteer> {

    /**
     * 统计使用同一手机号注册的志愿者数量
     *
     * @param signUpTel 注册手机号
     * @return 已注册的志愿者数量
     */
    int checkSignupTel(String signUpTel);

    /**
     * 注册志愿者
     *
     * @param signUpName     志愿者姓名
     * @param signUpTel      注册手机号
     * @param signUpPassword 登录密码
     * @return 新注册志愿者的志愿者编号
     */
    String signUp(String signUpName, String signUpTel, String signUpPassword);
}
