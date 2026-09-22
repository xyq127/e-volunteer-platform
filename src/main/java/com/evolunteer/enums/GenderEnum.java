package com.evolunteer.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

@Getter
public enum GenderEnum {
    MALE(0,"男"),
    FEMALE(1,"女");

    @EnumValue // 将注解所标识的属性的值存储到数据库中
    private Integer gender;
    private String genderName;

    GenderEnum(Integer gender, String genderName) {
        this.gender = gender;
        this.genderName = genderName;
    }
}
