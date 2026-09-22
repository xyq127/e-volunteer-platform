package com.evolunteer.entity;

import java.io.Serializable;
import lombok.Data;

@Data
public class ServiceRecord implements Serializable {

    private String activityName;

    private String activityId;

    private String serviceDate;

    private Double duration;

    private static final long serialVersionUID = 1L;
}
