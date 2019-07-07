package com.chaos.vo;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Data
public class UDPCommandVo {
    //序列号
    private Long seq;
    //命令内容
    private String commandContent;
    //命令响应结果
    private String result;
    //是否响应
    private boolean isRes=false;
    //失败描述
    private String failDesp;
    //时间戳
    private Long timestamp;

    public void callback(String msg){
        this.setRes(true);
        this.setResult(msg);
        log.info("callback:{}",this);
    }
}
