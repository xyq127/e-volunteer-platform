package com.evolunteer.entity;

import java.io.Serializable;
import java.util.List;
import lombok.Data;

/**
 * E志愿志愿者服务平台 V1.0
 * <p>
 * 志愿服务证明视图对象：汇总志愿者已核定的累计服务时长、服务记录与星级，
 * 并附带由累计服务数据生成的可校验编码，供第三方核验证明真伪，不属于任何数据表。
 */
@Data
public class ServiceCertificate implements Serializable {

    /** 志愿者业务编号 */
    private String volunteerId;

    /** 志愿者姓名，对外展示时做脱敏处理 */
    private String volunteerName;

    /** 已核定累计服务时长（小时） */
    private Double totalDuration;

    /** 已核定服务记录数 */
    private Integer activityCount;

    /** 当前星级 */
    private String starLevel;

    /** 下一星级名称，已达最高星级时为空 */
    private String nextLevelName;

    /** 距离下一星级还需要的服务时长（小时） */
    private Double hoursToNextLevel;

    /** 签发日期 */
    private String issueDate;

    /** 证明校验码 */
    private String verifyCode;

    /** 证明是否校验通过 */
    private boolean valid;

    /** 已核定服务记录明细 */
    private List<ServiceRecord> records;

    private static final long serialVersionUID = 1L;
}
