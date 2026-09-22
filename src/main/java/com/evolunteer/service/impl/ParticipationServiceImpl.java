package com.evolunteer.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.evolunteer.entity.Participation;
import com.evolunteer.mapper.ParticipationMapper;
import com.evolunteer.service.ParticipationService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * E志愿志愿者服务平台 V1.0
 * <p>
 * 活动报名业务实现类：负责志愿者报名信息的查询以及志愿者组织对报名申请的审核。
 */
@Service
public class ParticipationServiceImpl extends ServiceImpl<ParticipationMapper, Participation> implements ParticipationService {

    /**
     * 查询指定活动下已报名的志愿者列表
     *
     * @param activityNum 活动编号
     * @return 报名记录列表，已关联志愿者基本信息
     */
    @Override
    public List<Participation> getVolunteerList(Integer activityNum) {
        return baseMapper.selectParticipateWithVolunteerByActivityNum(activityNum);
    }

    /**
     * 调用存储过程写入志愿者组织对报名申请的审核结果
     *
     * @param map 审核参数，包含报名编号与审核结果
     * @return 存储过程返回的处理结果信息
     */
    @Override
    public Object volCheckRecruit(Map<Object, Object> map) {
        baseMapper.organization_check_volunteer(map);
        return map.get("msg");
    }
}
