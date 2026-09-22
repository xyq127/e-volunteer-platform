package com.evolunteer.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.evolunteer.entity.ActivityAnnouncement;
import com.evolunteer.entity.ActivityAnnouncementView;

/**
 * E志愿志愿者服务平台 V1.0
 * <p>
 * 志愿活动公告业务接口：面向志愿者组织提供本组织活动的公告发布、修改与删除能力，
 * 公告发布成功后向该活动已通过报名的志愿者发送站内通知；面向志愿者提供本人已报名活动的公告查询能力。
 */
public interface ActivityAnnouncementService {

    /**
     * 活动公告保存成功提示
     */
    String MESSAGE_SAVE_SUCCESS = "活动公告保存成功！";

    /**
     * 活动公告删除成功提示
     */
    String MESSAGE_DELETE_SUCCESS = "活动公告删除成功！";

    /**
     * 按本组织的志愿活动分页查询活动公告
     *
     * @param loginId     志愿者组织登录账号
     * @param activityNum 活动编号，为 null 时查询本组织的全部活动公告
     * @param page        页码
     * @param size        每页条数
     * @return 活动公告分页结果，已关联活动名称
     */
    IPage<ActivityAnnouncementView> pageByOrganization(String loginId, Integer activityNum, Integer page, Integer size);

    /**
     * 志愿者端分页查询本人已报名（待审核、已通过、候补）活动的公告
     *
     * @param volunteerNum 志愿者编号
     * @param page         页码
     * @param size         每页条数
     * @return 活动公告分页结果，已关联活动名称
     */
    IPage<ActivityAnnouncementView> pageForVolunteer(Integer volunteerNum, Integer page, Integer size);

    /**
     * 保存活动公告：公告编号为空时新增并生成公告业务编号，同时向该活动已通过报名的志愿者发送站内通知，
     * 否则修改已有活动公告
     *
     * @param loginId      志愿者组织登录账号
     * @param announcement 活动公告，包含所属活动编号、公告标题与公告内容
     * @return 返回给用户的中文提示
     */
    String saveAnnouncement(String loginId, ActivityAnnouncement announcement);

    /**
     * 逻辑删除活动公告，只能删除本组织发布的活动公告
     *
     * @param loginId         志愿者组织登录账号
     * @param announcementNum 公告编号
     * @return 返回给用户的中文提示
     */
    String deleteAnnouncement(String loginId, Integer announcementNum);
}
