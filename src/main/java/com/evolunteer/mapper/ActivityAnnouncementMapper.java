package com.evolunteer.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.evolunteer.entity.ActivityAnnouncement;
import com.evolunteer.entity.ActivityAnnouncementView;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

/**
 * E志愿志愿者服务平台 V1.0
 * <p>
 * 志愿活动公告表 act_announcement 的数据访问接口，提供志愿者组织按活动分页查询本组织发布的活动公告、
 * 志愿者查询本人已报名活动的公告，以及公告业务编号的生成能力。
 *
 * @Entity com.evolunteer.entity.ActivityAnnouncement
 */
@Repository
public interface ActivityAnnouncementMapper extends BaseMapper<ActivityAnnouncement> {

    /**
     * 按志愿者组织分页查询本组织发布的活动公告，可选按活动筛选
     *
     * @param page            分页对象
     * @param organizationNum 志愿者组织编号
     * @param activityNum     活动编号，为 null 时查询本组织的全部活动公告
     * @return 活动公告分页结果，已关联活动名称
     */
    IPage<ActivityAnnouncementView> selectPageByOrganization(Page<ActivityAnnouncementView> page,
                                                             @Param("organizationNum") Integer organizationNum,
                                                             @Param("activityNum") Integer activityNum);

    /**
     * 按志愿者分页查询本人已报名（待审核、已通过、候补）活动的公告
     *
     * @param page         分页对象
     * @param volunteerNum 志愿者编号
     * @return 活动公告分页结果，已关联活动名称
     */
    IPage<ActivityAnnouncementView> selectPageForVolunteer(Page<ActivityAnnouncementView> page,
                                                           @Param("volunteerNum") Integer volunteerNum);

    /**
     * 生成下一个公告业务编号的自增序号，取已有公告业务编号的数值部分最大值加一
     *
     * @return 自增序号，尚无活动公告时返回 1
     */
    Integer selectNextAnnouncementSeq();
}
