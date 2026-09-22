package com.evolunteer.entity;

import java.io.Serializable;
import java.util.List;
import lombok.Data;

@Data
public class PortalAnnouncementView implements Serializable {

    private Integer policyannouncementNum;

    private String policyannouncementId;

    private String policyannouncementName;

    private String policyannouncementDetail;

    private String policyannouncementFile;

    private Integer adminNum;

    private Integer fileCount;

    private List<PolicyFile> files;

    private static final long serialVersionUID = 1L;
}
