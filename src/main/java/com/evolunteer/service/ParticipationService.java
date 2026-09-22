package com.evolunteer.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.evolunteer.entity.Participation;
import com.evolunteer.entity.VolunteerParticipationView;

import java.util.List;
import java.util.Map;

public interface ParticipationService extends IService<Participation> {

    List<Participation> getVolunteerList(Integer activityNum);

    IPage<Participation> pageVolunteerList(Integer activityNum, String keyword, Integer pageNum, Integer pageSize);

    List<String> approvedVolunteerAccounts(Integer activityNum);

    IPage<VolunteerParticipationView> pageVolunteerParticipations(Integer volunteerNum,
                                                                 Integer pageNum, Integer pageSize);

    Map<Object, Object> applyActivity(Integer volunteerNum, Integer activityNum);

    Map<Object, Object> cancelParticipate(Integer participateNum, Integer volunteerNum);

    Map<Object, Object> confirmParticipate(Integer participateNum, Integer volunteerNum, Integer confirmState);

    Map<Object, Object> removeParticipate(String loginId, Integer participateNum);

    Map<Object, Object> releaseUnconfirmedParticipations();

    Object volCheckRecruit(Map<Object, Object> map);
}
