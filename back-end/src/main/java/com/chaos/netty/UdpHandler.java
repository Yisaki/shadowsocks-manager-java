package com.chaos.netty;

import com.chaos.task.SaveFlowSchedule;
import com.chaos.vo.UDPCommandVo;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class UdpHandler extends SimpleChannelInboundHandler{

    @Autowired
    private SaveFlowSchedule saveFlowSchedule;

    private UDPCommandVo udpCommandVo;

    public void setUdpCommandVo(UDPCommandVo udpCommandVo) {
        if(this.udpCommandVo!=null){
            throw new RuntimeException("already set udpVo:"+this.udpCommandVo.toString());
        }

        this.udpCommandVo = udpCommandVo;
    }

    public void clearUdpCommandVo(){
        udpCommandVo=null;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, Object o) throws Exception {
        String msg=(String)o;
        if("pong".equals(msg)){
            log.info("recv pong");

            //收到回包后的回调
            udpCommandVo.callback(msg);
        }else if("ok".equals(msg)){
            log.info("recv ok");

            //收到回包后的回调
            udpCommandVo.callback(msg);
        }else if(msg.contains("stat")){

            //流量数据
            //eg:
            // stat: {
            //  "71": 298,
            //  "443": 51
            //}
            int flagIndex=msg.indexOf("{");
            if(flagIndex>0){
                String statJSON=msg.substring(flagIndex);
                saveFlowSchedule.cacheStat(statJSON);
            }

        }
    }


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("read error",cause);
        ctx.close();
    }





}
