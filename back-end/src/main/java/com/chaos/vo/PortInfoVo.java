package com.chaos.vo;

import lombok.Data;

@Data
public class PortInfoVo {

    private Integer port;
    private String password;

    private Integer useType;

    //展示用###start###
    private String addTime;
    private String expireTime;
    private String updateTime;
    //MB
    private Long totalFlow=0L;
    //MB
    private Long usedFlow;
    //备注
    private String mark;
    //展示用###end###

    //更新用###start###
    private Integer day;
    //0:减少 1:增加
    private Integer dayType;

    //MB
    private Long flow;
    //0:减少 1:增加
    private Integer flowType;
    //更新用###end###

}
