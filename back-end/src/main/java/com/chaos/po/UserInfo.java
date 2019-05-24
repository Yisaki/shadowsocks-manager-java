package com.chaos.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("user_info")
public class UserInfo {
    @TableId(value = "id",type = IdType.AUTO)
    private Long id;
    private String name;
    private String password;
    private Integer role;
    private Date lastLoginTime;
    private Date createTime;
    private Date updateTime;
}
