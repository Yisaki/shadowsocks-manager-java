package com.chaos.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.chaos.netty.ShadowManagerClient;
import com.chaos.service.IUDPCommandService;
import com.chaos.vo.UDPCommandVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class UDPCommandServiceImpl implements IUDPCommandService {

    @Autowired
    private ShadowManagerClient client;

    @Override
    public boolean sendAdd(int port, String password)  {

        //2.发送UDP
        JSONObject reqJSON = new JSONObject();
        reqJSON.put("server_port", port);
        reqJSON.put("password", password);
        String reqStr = "add: " + reqJSON.toJSONString();

        UDPCommandVo udpCommandVo = new UDPCommandVo();
        udpCommandVo.setCommandContent(reqStr);
        udpCommandVo.setSeq(1L);
        udpCommandVo.setTimestamp(System.currentTimeMillis());

        client.send(udpCommandVo);

        return udpCommandVo.isRes();

    }

    @Override
    public boolean sendDelete(int port) {
        //2.发送UDP
        JSONObject reqJSON = new JSONObject();
        reqJSON.put("server_port", port);
        String reqStr = "remove: " + reqJSON.toJSONString();

        UDPCommandVo udpCommandVo = new UDPCommandVo();
        udpCommandVo.setCommandContent(reqStr);
        udpCommandVo.setSeq(1L);
        udpCommandVo.setTimestamp(System.currentTimeMillis());

        client.send(udpCommandVo);

        return udpCommandVo.isRes();
    }

    @Override
    public String sendPing() {

        String reqStr = "ping";

        UDPCommandVo udpCommandVo = new UDPCommandVo();
        udpCommandVo.setCommandContent(reqStr);
        udpCommandVo.setSeq(1L);
        udpCommandVo.setTimestamp(System.currentTimeMillis());

        client.send(udpCommandVo);

        return udpCommandVo.getResult();
    }
}
