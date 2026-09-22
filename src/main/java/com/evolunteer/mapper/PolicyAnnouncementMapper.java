package com.evolunteer.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.evolunteer.entity.PolicyAnnouncement;
import com.evolunteer.entity.PortalAnnouncementView;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

/**
 * E志愿志愿者服务平台 V1.0
 * <p>
 * 通知公告表 policy_announcement 的数据访问接口，并提供按关键字分页查询公告并统计附件数量、
 * 按管理员登录账号查询管理员编号的能力。
 *
 * @Entity com.evolunteer.entity.PolicyAnnouncement
 */
@Repository
public interface PolicyAnnouncementMapper extends BaseMapper<PolicyAnnouncement> {

    /**
     * 按关键字分页查询通知公告，并统计每条公告的附件数量供管理端列表展示
     *
     * @param page    分页对象
     * @param keyword 关键字，按公告标题与公告内容模糊匹配，为空时查询全部
     * @return 通知公告分页结果
     */
    IPage<PortalAnnouncementView> selectPageWithFileCount(Page<PortalAnnouncementView> page,
                                                          @Param("keyword") String keyword);

    /**
     * 按管理员登录账号查询管理员编号，用于记录公告的发布人
     *
     * @param adminId 管理员登录账号
     * @return 管理员编号，账号不存在时返回 null
     */
    Integer selectAdminNumByAdminId(@Param("adminId") String adminId);
}
