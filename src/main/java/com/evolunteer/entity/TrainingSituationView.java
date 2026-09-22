package com.evolunteer.entity;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * E志愿志愿者服务平台 V1.0
 * <p>
 * 志愿者培训情况视图对象：志愿者组织查询某次培训的参加记录时，
 * 关联展示志愿者的业务编号、姓名与联系电话，不属于任何数据表。
 */
@Data
public class TrainingSituationView implements Serializable {
    /** 培训情况编号 */
    private Integer vtsituationNum;

    /** 培训编号 */
    private Integer trainingNum;

    /** 志愿者编号 */
    private Integer volunteerNum;

    /** 记录业务编号 */
    private String vtsituationId;

    /** 培训开始时间 */
    private Date vtsituationBegintime;

    /** 培训结束时间 */
    private Date vtsituationEndtime;

    /** 志愿者业务编号 */
    private String volunteerId;

    /** 志愿者姓名 */
    private String volunteerName;

    /** 志愿者联系电话 */
    private String volunteerTel;

    private static final long serialVersionUID = 1L;
}
