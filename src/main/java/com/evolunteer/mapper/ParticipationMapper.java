package com.evolunteer.mapper;

import com.evolunteer.entity.Participation;
import com.evolunteer.entity.ServiceRecord;
import com.evolunteer.entity.VolunteerParticipationView;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface ParticipationMapper extends BaseMapper<Participation> {

    List<String> selectApprovedVolunteerAccounts(@Param("activityNum") Integer activityNum);

    IPage<Participation> selectPageParticipateWithVolunteerByActivityNum(
            Page<Participation> page,
            @Param("activityNum") Integer activityNum,
            @Param("keyword") String keyword);

    List<Participation> selectParticipateWithVolunteerByActivityNum(Integer activityNum);

    IPage<VolunteerParticipationView> selectPageVolunteerParticipations(
            Page<VolunteerParticipationView> page,
            @Param("volunteerNum") Integer volunteerNum);

    List<ServiceRecord> selectApprovedServiceRecords(@Param("volunteerNum") Integer volunteerNum);

    void volunteer_apply_activity(Map<Object, Object> map);

    void volunteer_cancel_participate(Map<Object, Object> map);

    void volunteer_confirm_participate(Map<Object, Object> map);

    void organization_check_volunteer(Map<Object, Object> map);

    void organization_remove_participate(Map<Object, Object> map);

    void participate_confirm_expire(Map<Object, Object> map);

    int countApprovedActivities(@Param("volunteerNum") Integer volunteerNum);
}
