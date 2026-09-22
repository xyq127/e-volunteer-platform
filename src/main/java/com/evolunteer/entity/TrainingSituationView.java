package com.evolunteer.entity;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

@Data
public class TrainingSituationView implements Serializable {

    private Integer vtsituationNum;

    private Integer trainingNum;

    private Integer volunteerNum;

    private String vtsituationId;

    private Date vtsituationBegintime;

    private Date vtsituationEndtime;

    private String volunteerId;

    private String volunteerName;

    private String volunteerTel;

    private static final long serialVersionUID = 1L;
}
