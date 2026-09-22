package com.evolunteer.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.evolunteer.entity.PolicyAnnouncement;
import com.evolunteer.entity.PolicyFile;
import com.evolunteer.entity.PortalAnnouncementView;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * E志愿志愿者服务平台 V1.0
 * <p>
 * 通知公告业务接口，基于 MyBatis-Plus 提供通知公告数据的通用增删改查能力，
 * 并提供平台管理员发布与删除通知公告、上传与删除公告附件以及按公告查询附件的能力。
 */
public interface AnnouncementService extends IService<PolicyAnnouncement> {

    /**
     * 分页查询通知公告，每条公告附带附件数量
     *
     * @param pageNum  页码
     * @param pageSize 每页条数
     * @param keyword  关键字，按公告标题与公告内容模糊匹配，可为空
     * @return 通知公告分页结果
     */
    IPage<PortalAnnouncementView> page(Integer pageNum, Integer pageSize, String keyword);

    /**
     * 保存通知公告：编号为空时新增并生成公告业务编号，否则更新公告标题与内容
     *
     * @param policyannouncementNum 公告编号，新增时传 null
     * @param name                  公告标题
     * @param detail                公告内容
     * @param adminId               发布公告的管理员登录账号
     * @return 处理结果，包含提示信息 msg、处理标记 ok 与公告编号 policyannouncementNum
     */
    Map<Object, Object> save(Integer policyannouncementNum, String name, String detail, String adminId);

    /**
     * 逻辑删除通知公告，并同步清理公告附件记录与物理文件
     *
     * @param policyannouncementNum 公告编号
     * @return 处理结果，包含提示信息 msg 与处理标记 ok
     */
    Map<Object, Object> delete(Integer policyannouncementNum);

    /**
     * 上传公告附件：文件保存到服务器文件目录并写入公告附件表
     *
     * @param policyannouncementNum 公告编号
     * @param file                  上传文件
     * @return 已保存的公告附件信息
     */
    PolicyFile uploadFile(Integer policyannouncementNum, MultipartFile file);

    /**
     * 删除公告附件：同时逻辑删除附件记录并删除服务器上的物理文件
     *
     * @param policyfileNum 附件编号
     * @return 附件不存在时返回 false，删除成功返回 true
     */
    boolean deleteFile(Integer policyfileNum);

    /**
     * 按公告查询附件列表
     *
     * @param policyannouncementNum 公告编号
     * @return 附件列表
     */
    List<PolicyFile> listFiles(Integer policyannouncementNum);
}
