package com.evolunteer.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.evolunteer.entity.VolunteerSkill;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VolunteerSkillMapper extends BaseMapper<VolunteerSkill> {

    List<String> selectSkillNamesByVolunteerNum(@Param("volunteerNum") Integer volunteerNum);

    int deleteSkillNamesByVolunteerNum(@Param("volunteerNum") Integer volunteerNum);
}
