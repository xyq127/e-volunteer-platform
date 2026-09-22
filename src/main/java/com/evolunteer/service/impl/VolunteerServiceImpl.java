package com.evolunteer.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.evolunteer.entity.Volunteer;
import com.evolunteer.entity.VolunteerSkill;
import com.evolunteer.mapper.VolunteerMapper;
import com.evolunteer.mapper.VolunteerSkillMapper;
import com.evolunteer.service.VolunteerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * E志愿志愿者服务平台 V1.0
 * <p>
 * 志愿者业务实现类：负责志愿者手机号查重、志愿者注册与个人档案维护。
 * 注册时密码以加盐摘要方式存储并同步开通平台登录账号，档案保存时重建技能标签与常住地点。
 */
@Service
public class VolunteerServiceImpl extends ServiceImpl<VolunteerMapper, Volunteer> implements VolunteerService {

    @Autowired
    private VolunteerSkillMapper volunteerSkillMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

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
     * 调用存储过程完成志愿者注册，注册后即可凭志愿者编号登录个人工作台
     *
     * @param signUpName     志愿者姓名
     * @param signUpTel      注册手机号
     * @param signUpPassword 登录密码
     * @return 新注册志愿者的志愿者编号，手机号已被注册时返回 null
     */
    @Override
    public String signUp(String signUpName, String signUpTel, String signUpPassword) {
        Map<Object, Object> map = new HashMap<>();
        map.put("volunteerName", signUpName);
        map.put("volunteerTel", signUpTel);
        map.put("volunteerPassword", passwordEncoder.encode(signUpPassword));
        baseMapper.volunteer_insert(map);
        return (String) map.get("volunteerId");
    }

    /**
     * 按登录账号查询志愿者
     *
     * @param volunteerId 志愿者业务编号，同时作为登录账号
     * @return 志愿者信息，不存在时返回 null
     */
    @Override
    public Volunteer getByLoginId(String volunteerId) {
        List<Volunteer> volunteers = baseMapper.selectByVolunteerId(volunteerId);
        return volunteers.isEmpty() ? null : volunteers.get(0);
    }

    /**
     * 查询志愿者登记的服务技能标签
     *
     * @param volunteerNum 志愿者编号
     * @return 技能标签列表
     */
    @Override
    public List<String> listSkillNames(Integer volunteerNum) {
        return volunteerSkillMapper.selectSkillNamesByVolunteerNum(volunteerNum);
    }

    /**
     * 保存志愿者的技能标签与常住地点：技能标签先清空再写入，避免遗留已取消的标签
     *
     * @param volunteerNum 志愿者编号
     * @param skillNames   服务技能标签列表
     * @param latitude     常住地点纬度，未设置时传 null
     * @param longitude    常住地点经度，未设置时传 null
     * @return 保存成功返回 true
     */
    @Override
    @Transactional
    public boolean saveProfile(Integer volunteerNum, List<String> skillNames, BigDecimal latitude, BigDecimal longitude) {

        volunteerSkillMapper.deleteSkillNamesByVolunteerNum(volunteerNum);

        Set<String> distinctSkills = new LinkedHashSet<>(skillNames);
        for (String skillName : distinctSkills) {
            if (skillName == null || skillName.trim().isEmpty()) {
                continue;
            }
            VolunteerSkill volunteerSkill = new VolunteerSkill();
            volunteerSkill.setVolunteerNum(volunteerNum);
            volunteerSkill.setSkillName(skillName.trim());
            volunteerSkillMapper.insert(volunteerSkill);
        }

        Volunteer volunteer = new Volunteer();
        volunteer.setVolunteerNum(volunteerNum);
        volunteer.setVolunteerLatitude(latitude);
        volunteer.setVolunteerLongitude(longitude);
        return baseMapper.updateById(volunteer) > 0;
    }
}
