package com.evolunteer.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.evolunteer.entity.Volunteer;

import java.math.BigDecimal;
import java.util.List;

/**
 * E志愿志愿者服务平台 V1.0
 * <p>
 * 志愿者业务接口，基于 MyBatis-Plus 提供志愿者数据的通用增删改查能力，
 * 并提供志愿者注册、个人档案维护与技能标签查询等能力。
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
     * 注册志愿者，注册成功后同时开通平台登录账号
     *
     * @param signUpName     志愿者姓名
     * @param signUpTel      注册手机号
     * @param signUpPassword 登录密码
     * @return 新注册志愿者的志愿者编号，手机号已被注册时返回 null
     */
    String signUp(String signUpName, String signUpTel, String signUpPassword);

    /**
     * 按登录账号查询志愿者
     *
     * @param volunteerId 志愿者业务编号，同时作为登录账号
     * @return 志愿者信息，不存在时返回 null
     */
    Volunteer getByLoginId(String volunteerId);

    /**
     * 查询志愿者登记的服务技能标签
     *
     * @param volunteerNum 志愿者编号
     * @return 技能标签列表
     */
    List<String> listSkillNames(Integer volunteerNum);

    /**
     * 保存志愿者的技能标签与常住地点，用于供需匹配
     *
     * @param volunteerNum 志愿者编号
     * @param skillNames   服务技能标签列表
     * @param latitude     常住地点纬度，未设置时传 null
     * @param longitude    常住地点经度，未设置时传 null
     * @return 保存成功返回 true
     */
    boolean saveProfile(Integer volunteerNum, List<String> skillNames, BigDecimal latitude, BigDecimal longitude);
}
