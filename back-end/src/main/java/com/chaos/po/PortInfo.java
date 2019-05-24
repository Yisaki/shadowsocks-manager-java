package com.chaos.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("port_info")
public class PortInfo {

    @TableId(value = "id",type = IdType.AUTO)
    private Long id;
    @TableField(value = "port")
    private Integer port;
    private String password;
    private Date addTime;
    private Date expireTime;
    private Long totalFlow;
    private Long usedFlow;
    private Integer useType=1;
    //备注
    private String mark;
    //外键
    @TableField(value = "fk_user_info_name")
    private String userName;

    @TableField(exist = false)
    private UserInfo userInfo;

    //@TableField(select = false)
    private Date createTime;
    //@TableField(select = false)
    private Date updateTime;
}
