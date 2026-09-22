package com.evolunteer.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.evolunteer.entity.Training;
import com.evolunteer.entity.TrainingSituationView;
import com.evolunteer.entity.TrainingView;

import java.util.Date;

/**
 * E志愿志愿者服务平台 V1.0
 * <p>
 * 志愿培训业务接口：面向志愿者组织提供本组织活动的培训安排维护与志愿者培训情况登记能力，
 * 面向志愿者提供本人已通过报名活动对应的培训安排查询能力。写操作均校验培训所属活动是否属于当前组织。
 */
public interface TrainingService {

    /**
     * 培训信息保存成功提示
     */
    String MESSAGE_SAVE_SUCCESS = "培训信息保存成功！";

    /**
     * 培训信息删除成功提示
     */
    String MESSAGE_DELETE_SUCCESS = "培训信息删除成功！";

    /**
     * 志愿者培训情况登记成功提示
     */
    String MESSAGE_SITUATION_SAVE_SUCCESS = "志愿者培训情况登记成功！";

    /**
     * 按本组织的志愿活动分页查询培训安排
     *
     * @param loginId     志愿者组织登录账号
     * @param activityNum 活动编号，为 null 时查询本组织的全部培训安排
     * @param page        页码
     * @param size        每页条数
     * @return 培训安排分页结果，已关联活动名称与已登记培训情况人数
     */
    IPage<TrainingView> pageByActivity(String loginId, Integer activityNum, Integer page, Integer size);

    /**
     * 保存培训安排：培训编号为空时新增并生成培训业务编号，否则修改已有培训安排
     *
     * @param loginId  志愿者组织登录账号
     * @param training 培训安排，包含所属活动编号、培训名称、培训内容、开始与结束时间、地点与签到方式
     * @return 返回给用户的中文提示
     */
    String saveTraining(String loginId, Training training);

    /**
     * 逻辑删除培训安排，只能删除本组织申报活动下的培训安排
     *
     * @param loginId     志愿者组织登录账号
     * @param trainingNum 培训编号
     * @return 返回给用户的中文提示
     */
    String deleteTraining(String loginId, Integer trainingNum);

    /**
     * 按培训分页查询志愿者参加培训的情况，只能查询本组织申报活动下的培训
     *
     * @param loginId     志愿者组织登录账号
     * @param trainingNum 培训编号
     * @param page        页码
     * @param size        每页条数
     * @return 志愿者培训情况分页结果，已关联志愿者编号、姓名与联系电话
     */
    IPage<TrainingSituationView> pageSituations(String loginId, Integer trainingNum, Integer page, Integer size);

    /**
     * 登记志愿者参加培训的情况，只能登记本组织申报活动下培训的安排，
     * 且该志愿者须报名过培训所属活动
     *
     * @param loginId      志愿者组织登录账号
     * @param trainingNum  培训编号
     * @param volunteerNum 志愿者编号
     * @param begin        培训开始时间
     * @param end          培训结束时间
     * @return 返回给用户的中文提示
     */
    String saveSituation(String loginId, Integer trainingNum, Integer volunteerNum, Date begin, Date end);

    /**
     * 志愿者端分页查询本人已通过报名活动对应的培训安排
     *
     * @param volunteerNum 志愿者编号
     * @param page         页码
     * @param size         每页条数
     * @return 培训安排分页结果，已关联活动名称与已登记培训情况人数
     */
    IPage<TrainingView> pageForVolunteer(Integer volunteerNum, Integer page, Integer size);
}
