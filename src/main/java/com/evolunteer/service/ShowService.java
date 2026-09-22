package com.evolunteer.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.evolunteer.entity.PortalShowView;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * E志愿志愿者服务平台 V1.0
 * <p>
 * 志愿秀业务接口，基于 MyBatis-Plus 提供志愿秀数据的通用增删改查能力，
 * 并提供志愿者发布志愿秀与上传图片、志愿者本人志愿秀查询、门户志愿秀浏览与点赞，
 * 以及平台管理员志愿秀查询与删除的能力。
 */
public interface ShowService {

    /**
     * 志愿者发布志愿秀，关联活动时只能是本人已通过报名的活动
     *
     * @param volunteerNum 志愿者编号
     * @param activityNum  关联活动编号，不关联活动时传 null
     * @param detail       分享内容
     * @return 处理结果，包含提示信息 msg、处理标记 ok 与志愿秀编号 showNum
     */
    Map<Object, Object> publish(Integer volunteerNum, Integer activityNum, String detail);

    /**
     * 为本人发布的志愿秀上传图片
     *
     * @param volunteerNum 志愿者编号
     * @param showNum      志愿秀编号
     * @param file         上传图片
     * @return 处理结果，包含提示信息 msg 与处理标记 ok
     */
    Map<Object, Object> uploadPicture(Integer volunteerNum, Integer showNum, MultipartFile file);

    /**
     * 分页查询志愿者本人发布的志愿秀
     *
     * @param volunteerNum 志愿者编号
     * @param pageNum      页码
     * @param pageSize     每页条数
     * @return 志愿秀分页结果
     */
    IPage<PortalShowView> pageByVolunteer(Integer volunteerNum, Integer pageNum, Integer pageSize);

    /**
     * 分页查询门户志愿秀，附带分享志愿者姓名、关联活动名称与首图路径
     *
     * @param pageNum      页码
     * @param pageSize     每页条数
     * @param keyword      关键字，按分享内容与分享志愿者姓名模糊匹配，可为空
     * @param volunteerNum 当前访问志愿者编号，未登录时传 null，仅用于标记是否已点赞
     * @return 志愿秀分页结果
     */
    IPage<PortalShowView> pagePortal(Integer pageNum, Integer pageSize, String keyword, Integer volunteerNum);

    /**
     * 查询志愿秀详情并累加浏览次数
     *
     * @param showNum      志愿秀编号
     * @param volunteerNum 当前访问志愿者编号，未登录时传 null，仅用于标记是否已点赞
     * @return 志愿秀详情，志愿秀不存在或已删除时返回 null
     */
    PortalShowView detail(Integer showNum, Integer volunteerNum);

    /**
     * 志愿者点赞或取消点赞：已点赞则取消，未点赞则点赞，同一志愿者对同一志愿秀只计一次
     *
     * @param volunteerNum 志愿者编号
     * @param showNum      志愿秀编号
     * @return 处理结果，包含提示信息 msg、处理标记 ok、是否已点赞 liked 与最新点赞次数 likeCount
     */
    Map<Object, Object> like(Integer volunteerNum, Integer showNum);

    /**
     * 平台管理员分页查询志愿秀
     *
     * @param pageNum  页码
     * @param pageSize 每页条数
     * @param keyword  关键字，按分享内容与分享志愿者姓名模糊匹配，可为空
     * @return 志愿秀分页结果
     */
    IPage<PortalShowView> pageAdmin(Integer pageNum, Integer pageSize, String keyword);

    /**
     * 平台管理员逻辑删除志愿秀并记录操作审计
     *
     * @param showNum  志愿秀编号
     * @param operator 操作管理员登录账号
     * @param ip       操作来源 IP
     * @return 处理结果，包含提示信息 msg 与处理标记 ok
     */
    Map<Object, Object> deleteShow(Integer showNum, String operator, String ip);
}
