package com.chaos.netty;

import com.chaos.vo.UDPCommandVo;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class UDPRequestCallback {

    private UDPCommandVo udpCommandVo;
    public UDPRequestCallback(UDPCommandVo udpCommandVo){
        this.udpCommandVo=udpCommandVo;
    }

    public void callback(String msg){
        log.debug("callback,req:{}, resp:{}",udpCommandVo.toString(),msg);
        udpCommandVo.setRes(true);
        udpCommandVo.setResult(msg);
    }

    public boolean isDone(){
        return udpCommandVo.isRes();
    }

    public UDPCommandVo getCallbackedVo(){
        return udpCommandVo;
    }

}
