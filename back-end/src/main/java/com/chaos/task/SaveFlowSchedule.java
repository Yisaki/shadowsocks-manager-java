package com.chaos.task;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.chaos.mapper.PortInfoMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.LinkedBlockingDeque;

@Slf4j
@Component
public class SaveFlowSchedule {

    private static LinkedBlockingDeque<String> statQueue=new LinkedBlockingDeque<>();

    private static Map<Integer,Long> portAndFlowCache=new HashMap<>();

    @Autowired
    private PortInfoMapper portInfoMapper;

    @Scheduled(cron = "${task.saveFlow}")
    public void saveFlow(){
        String stat=null;
        while((stat=statQueue.poll())!=null){
            Map<Integer, Long> portAndFlowMap = JSON.parseObject(stat, new TypeReference<Map<Integer, Long>>() { });
            for(Map.Entry<Integer, Long> portAndFlow: portAndFlowMap.entrySet()){
                //端口
                Integer port=portAndFlow.getKey();
                //流量
                Long flow=portAndFlow.getValue();


                //汇总一个端口在这一周期的流量
                if(portAndFlowCache.containsKey(port)){
                    //此端口之前有流量
                    Long oldFlow=portAndFlowCache.get(port);
                    flow+=oldFlow;
                    portAndFlowCache.put(port,flow);
                }else{
                    portAndFlowCache.put(port,flow);
                }

            }
        }


        if(portAndFlowCache.isEmpty()){
            log.info("no flow to save");
            return;
        }

        for(Map.Entry<Integer,Long> entry:portAndFlowCache.entrySet()){
            Integer port=entry.getKey();
            Long byteFlow=entry.getValue();
            //四舍五入计算byte->MB
            double flowMBD=(double)(entry.getValue()/1024)/1024;
            long flowMB = Math.round(flowMBD);

            //入库
            portInfoMapper.updateFlow(port,flowMB);

            log.info("port:{},flow:{}",port,byteFlow);
        }
        log.info("save {} port flow",portAndFlowCache.size());
        portAndFlowCache.clear();


    }


    public void cacheStat(String stat){
        statQueue.add(stat);
    }



}
