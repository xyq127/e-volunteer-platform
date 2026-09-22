package com.evolunteer.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.evolunteer.entity.Activity;
import com.evolunteer.mapper.ActivityMapper;
import com.evolunteer.service.ActivityService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * E志愿志愿者服务平台 V1.0
 * <p>
 * 志愿活动业务实现类：封装志愿活动的查询、平台审核结果回写以及活动逻辑删除等数据库操作。
 */
@Service
public class ActivityServiceImpl extends ServiceImpl<ActivityMapper, Activity> implements ActivityService {

    /**
     * 按志愿者组织编号与活动状态查询活动列表
     *
     * @param num   志愿者组织编号
     * @param state 活动状态（0 审核中、1 未开始、2 进行中、3 已结束、4 审核未通过）
     * @return 活动列表
     */
    @Override
    public List<Activity> getActivityByNumAndState(Integer num, String state) {
        return baseMapper.selectAllByOrganizationNumAndActivityStateAndActivityIsdeleted(num, state, 0);
    }

    /**
     * 按活动状态查询平台内全部活动，供平台管理员审核使用
     *
     * @param state 活动状态
     * @return 活动列表
     */
    @Override
    public List<Activity> getActivityByState(String state) {
        return baseMapper.selectAllByActivityStateAndActivityIsdeleted(state, 0);
    }

    /**
     * 查询指定活动的详情
     *
     * @param activityNum 活动编号
     * @return 活动详情列表
     */
    @Override
    public List<Activity> activityInfo(Integer activityNum) {
        return baseMapper.selectByActivityNum(activityNum);
    }

    /**
     * 校验活动名称是否可用，供志愿者组织申报活动时查重
     *
     * @param name 活动名称
     * @return 名称未被占用返回 true
     */
    @Override
    public Boolean searchActivityByName(String name) {
        return baseMapper.selectAllByActivityName(name).isEmpty();
    }

    /**
     * 调用存储过程写入平台管理员的活动审核结果
     *
     * @param map 审核参数，包含管理员账号、活动编号、审核结果与审核意见
     * @return 存储过程返回的处理结果信息
     */
    @Override
    public Object passActByAdminIdWithStatue(Map<Object, Object> map) {
        baseMapper.admin_check_activity(map);
        return map.get("msg");
    }

    /**
     * 按活动名称精确查询活动
     *
     * @param actName 活动名称
     * @return 匹配的活动，不存在时返回 null
     */
    @Override
    public Activity selectAct(String actName) {
        List<Activity> activities = baseMapper.selectByActivityName(actName);
        return activities.isEmpty() ? null : activities.get(0);
    }

    /**
     * 逻辑删除活动
     *
     * @param num 活动编号
     * @return 删除成功返回 true
     */
    @Override
    public Boolean deleteAct(Integer num) {
        return baseMapper.updateActivityIsdeletedByActivityNum(1, num) > 0;
    }
}
