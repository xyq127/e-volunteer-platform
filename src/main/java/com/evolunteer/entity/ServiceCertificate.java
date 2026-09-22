package com.evolunteer.entity;

import java.io.Serializable;
import java.util.List;
import lombok.Data;

@Data
public class ServiceCertificate implements Serializable {

    private String volunteerId;

    private String volunteerName;

    private Double totalDuration;

    private Integer activityCount;

    private String starLevel;

    private String nextLevelName;

    private Double hoursToNextLevel;

    private String issueDate;

    private String verifyCode;

    private boolean valid;

    private List<ServiceRecord> records;

    private static final long serialVersionUID = 1L;
}
