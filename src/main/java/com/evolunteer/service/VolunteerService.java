package com.evolunteer.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.evolunteer.entity.Volunteer;

import java.math.BigDecimal;
import java.util.List;

public interface VolunteerService extends IService<Volunteer> {

    int checkSignupTel(String signUpTel);

    String signUp(String signUpName, String signUpTel, String signUpPassword);

    Volunteer getByLoginId(String volunteerId);

    List<String> listSkillNames(Integer volunteerNum);

    boolean saveProfile(Integer volunteerNum, List<String> skillNames, BigDecimal latitude, BigDecimal longitude);
}
