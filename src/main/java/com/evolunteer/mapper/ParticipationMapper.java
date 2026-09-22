package com.evolunteer.mapper;

import com.evolunteer.entity.Activity;
import com.evolunteer.entity.Participation;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
 * E志愿志愿者服务平台 V1.0
 * <p>
 * 活动报名表 participate 的数据访问接口。
 *
 * @Entity com.evolunteer.entity.Participation
 */
@Repository
public interface ParticipationMapper extends BaseMapper<Participation> {

    List<Participation> selectParticipateWithVolunteerByActivityNum(Integer activityNum);

    //调用数据库中的organization_check_volunteer存储过程
    void organization_check_volunteer(Map map);

}




