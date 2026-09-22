package com.evolunteer.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import lombok.Data;

@TableName(value ="e_user")
@Data
public class UserAccount implements Serializable {

    private String id;

    private String password;

    private String role;

    private Integer enabled;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
