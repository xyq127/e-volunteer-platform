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

@Service
public class VolunteerServiceImpl extends ServiceImpl<VolunteerMapper, Volunteer> implements VolunteerService {

    @Autowired
    private VolunteerSkillMapper volunteerSkillMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public int checkSignupTel(String signUpTel) {
        return baseMapper.countByVolunteerTel(signUpTel);
    }

    @Override
    public String signUp(String signUpName, String signUpTel, String signUpPassword) {
        Map<Object, Object> map = new HashMap<>();
        map.put("volunteerName", signUpName);
        map.put("volunteerTel", signUpTel);
        map.put("volunteerPassword", passwordEncoder.encode(signUpPassword));
        baseMapper.volunteer_insert(map);
        return (String) map.get("volunteerId");
    }

    @Override
    public Volunteer getByLoginId(String volunteerId) {
        List<Volunteer> volunteers = baseMapper.selectByVolunteerId(volunteerId);
        return volunteers.isEmpty() ? null : volunteers.get(0);
    }

    @Override
    public List<String> listSkillNames(Integer volunteerNum) {
        return volunteerSkillMapper.selectSkillNamesByVolunteerNum(volunteerNum);
    }

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
