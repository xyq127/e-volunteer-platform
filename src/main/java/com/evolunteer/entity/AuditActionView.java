package com.evolunteer.entity;

import lombok.Data;

@Data
public class AuditActionView {

    private String code;

    private String name;

    public AuditActionView(String code, String name) {
        this.code = code;
        this.name = name;
    }
}
