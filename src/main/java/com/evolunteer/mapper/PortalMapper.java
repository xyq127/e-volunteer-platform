package com.evolunteer.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.evolunteer.entity.Activity;
import com.evolunteer.entity.Organization;
import com.evolunteer.entity.PolicyFile;
import com.evolunteer.entity.PortalAnnouncementView;
import com.evolunteer.entity.PortalShowView;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * E志愿志愿者服务平台 V1.0
 * <p>
 * 公众门户数据访问接口：为无需登录的门户页面提供通知公告、志愿活动、志愿者组织
 * 与志愿秀广场的只读分页查询，仅查询未删除且可对外公开的数据。
 */
@Repository
public interface PortalMapper {

    /**
     * 分页查询门户通知公告，并统计每条公告的附件数量
     *
     * @param page    分页对象
     * @param keyword 关键字，按公告标题与公告内容模糊匹配，为空时查询全部
     * @return 通知公告分页结果
     */
    IPage<PortalAnnouncementView> selectAnnouncementPage(Page<PortalAnnouncementView> page,
                                                         @Param("keyword") String keyword);

    /**
     * 按公告编号查询门户公告详情
     *
     * @param policyannouncementNum 公告编号
     * @return 公告详情，公告不存在或已删除时返回 null
     */
    PortalAnnouncementView selectAnnouncementByNum(@Param("policyannouncementNum") Integer policyannouncementNum);

    /**
     * 按公告编号批量查询附件，供门户公告列表一次性装配各公告的附件
     *
     * @param policyannouncementNums 公告编号列表，不能为空
     * @return 附件列表
     */
    List<PolicyFile> selectFilesByAnnouncementNums(@Param("policyannouncementNums") List<Integer> policyannouncementNums);

    /**
     * 分页查询门户公开志愿活动，仅返回未开始与进行中且未删除的活动
     *
     * @param page    分页对象
     * @param keyword 关键字，按活动名称与活动地点模糊匹配，为空时查询全部
     * @return 志愿活动分页结果
     */
    IPage<Activity> selectActivityPage(Page<Activity> page, @Param("keyword") String keyword);

    /**
     * 分页查询门户志愿者组织
     *
     * @param page    分页对象
     * @param keyword 关键字，按组织名称与组织简介模糊匹配，为空时查询全部
     * @return 志愿者组织分页结果
     */
    IPage<Organization> selectOrganizationPage(Page<Organization> page, @Param("keyword") String keyword);

    /**
     * 分页查询门户志愿秀，并关联分享志愿者姓名、关联活动名称与首图访问路径
     *
     * @param page    分页对象
     * @param keyword 关键字，按分享内容与分享志愿者姓名模糊匹配，为空时查询全部
     * @return 志愿秀分页结果
     */
    IPage<PortalShowView> selectShowPage(Page<PortalShowView> page, @Param("keyword") String keyword);

    /**
     * 按志愿秀编号查询志愿秀详情，并关联分享志愿者姓名、关联活动名称与首图访问路径
     *
     * @param showNum 志愿秀编号
     * @return 志愿秀详情，志愿秀不存在或已删除时返回 null
     */
    PortalShowView selectShowByNum(@Param("showNum") Integer showNum);

    /**
     * 按志愿秀编号查询全部图片访问路径，按图片编号升序排列
     *
     * @param showNum 志愿秀编号
     * @return 图片访问路径列表
     */
    List<String> selectPictureRoutes(@Param("showNum") Integer showNum);

    /**
     * 查询志愿者在指定志愿秀范围内已点赞的志愿秀编号，用于标记门户列表与详情的是否已点赞
     *
     * @param volunteerNum 志愿者编号
     * @param showNums     志愿秀编号列表，不能为空
     * @return 已点赞的志愿秀编号列表
     */
    List<Integer> selectLikedShowNums(@Param("volunteerNum") Integer volunteerNum,
                                      @Param("showNums") List<Integer> showNums);
}
