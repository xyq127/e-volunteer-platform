package com.evolunteer.entity;

import java.io.Serializable;
import java.util.List;
import lombok.Data;

/**
 * E志愿志愿者服务平台 V1.0
 * <p>
 * 通知公告展示视图：在通知公告内容的基础上补充附件数量与附件列表，
 * 供公众门户公告栏与平台管理员公告管理共用，避免列表页逐条查询附件。
 */
@Data
public class PortalAnnouncementView implements Serializable {

    /** 公告编号 */
    private Integer policyannouncementNum;

    /** 公告业务编号 */
    private String policyannouncementId;

    /** 公告标题 */
    private String policyannouncementName;

    /** 公告内容 */
    private String policyannouncementDetail;

    /** 公告附件 */
    private String policyannouncementFile;

    /** 发布公告的管理员编号 */
    private Integer adminNum;

    /** 附件数量 */
    private Integer fileCount;

    /** 附件列表，查询公告列表与公告详情时装配 */
    private List<PolicyFile> files;

    private static final long serialVersionUID = 1L;
}
