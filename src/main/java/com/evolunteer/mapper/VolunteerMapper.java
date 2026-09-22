package com.evolunteer.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.evolunteer.entity.Volunteer;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface VolunteerMapper extends BaseMapper<Volunteer> {

    List<Volunteer> selectByVolunteerName(@Param("volunteerName") String volunteerName);

    List<Volunteer> selectByVolunteerId(@Param("volunteerId") String volunteerId);

    int countByVolunteerTel(@Param("signUpTel") String signUpTel);

    void volunteer_insert(Map<Object, Object> map);

}
