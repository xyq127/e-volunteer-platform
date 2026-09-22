package com.evolunteer.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.evolunteer.entity.Volunteer;
import com.evolunteer.mapper.VolunteerMapper;
import com.evolunteer.service.VolunteerService;
import com.evolunteer.utils.MD5Util;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * E志愿志愿者服务平台 V1.0
 * <p>
 * 志愿者业务实现类：负责志愿者手机号查重以及志愿者注册，注册时对密码进行摘要后再入库。
 */
@Service
public class VolunteerServiceImpl extends ServiceImpl<VolunteerMapper, Volunteer> implements VolunteerService {

    /**
     * 统计使用同一手机号注册的志愿者数量
     *
     * @param signUpTel 注册手机号
     * @return 已注册的志愿者数量
     */
    @Override
    public int checkSignupTel(String signUpTel) {
        return baseMapper.countByVolunteerTel(signUpTel);
    }

    /**
     * 调用存储过程完成志愿者注册，由数据库生成志愿者编号
     *
     * @param signUpName     志愿者姓名
     * @param signUpTel      注册手机号
     * @param signUpPassword 登录密码
     * @return 新注册志愿者的志愿者编号
     */
    @Override
    public String signUp(String signUpName, String signUpTel, String signUpPassword) {
        Map<Object, Object> map = new HashMap<>();
        map.put("volunteerName", signUpName);
        map.put("volunteerTel", signUpTel);
        map.put("volunteerPassword", MD5Util.encode(signUpPassword));
        baseMapper.volunteer_insert(map);
        return (String) map.get("volunteerId");
    }
}
