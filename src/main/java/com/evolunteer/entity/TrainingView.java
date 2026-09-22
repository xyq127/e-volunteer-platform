package com.evolunteer.entity;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

@Data
public class TrainingView implements Serializable {

    private Integer trainingNum;

    private String trainingId;

    private Integer activityNum;

    private String trainingName;

    private String trainingDetail;

    private Date trainingBegintime;

    private Date trainingEndtime;

    private String trainingLocation;

    private String trainingCheckin;

    private String activityName;

    private Integer situationCount;

    private static final long serialVersionUID = 1L;
}
