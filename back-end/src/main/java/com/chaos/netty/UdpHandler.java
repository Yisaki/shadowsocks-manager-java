package com.chaos.netty;

import com.chaos.task.SaveFlowSchedule;
import com.chaos.vo.UDPCommandHolder;
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

    //当前的返回值
    public static UDPCommandHolder processingCommandHolder = new UDPCommandHolder();


    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, Object o) throws Exception {
        String msg=(String)o;
        if("pong".equals(msg)){
            log.info("recv pong");

            fillResp(msg);
        }else if("ok".equals(msg)){
            log.info("recv ok");

            fillResp(msg);
        }else if(msg.contains("stat")){

            //eg:
            // stat: {
            //  "71": 298,
            //  "443": 51
            //}
            int flagIndex=msg.indexOf("{");
            String statJSON=msg.substring(flagIndex);
            saveFlowSchedule.cacheStat(statJSON);
        }
    }


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("read error",cause);
        ctx.close();
    }

    /**
     * 通过共享变量设置返回值
     * @param msg
     */
    private void fillResp(String msg){
        UDPCommandVo  processingCommand = processingCommandHolder.getUdpCommandVo();
        if(processingCommand!=null){
            processingCommand.setRes(true);
            processingCommand.setResult(msg);
        }else {
            log.error("holder is not empty! resp:{}",msg);
        }
    }



}
