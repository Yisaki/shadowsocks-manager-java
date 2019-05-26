package com.chaos.task;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.chaos.config.ConfigValue;
import com.chaos.mapper.PortInfoMapper;
import com.chaos.po.PortInfo;
import com.chaos.service.IPortService;
import com.chaos.service.IUDPCommandService;
import com.chaos.vo.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class CheckPortExpireSchedule {

    @Autowired
    private IPortService portService;

    @Autowired
    private PortInfoMapper portInfoMapper;

    @Autowired
    private IUDPCommandService udpCommandService;

    @Transactional
    @Scheduled(cron = "${task.checkPort}")
    public void execute(){
        List<PortInfo> portInfos = portService.listAll(false);

        int deleteCount=0;
        for (PortInfo portInfo : portInfos) {
            if(portInfo.getUseType()==2){
                //无限使用不检查
                continue;
            }

            int port = portInfo.getPort();

            //1.流量
            boolean unavailableFlag = portInfo.getUsedFlow() >= portInfo.getTotalFlow();
            if (unavailableFlag) {
                log.info("port is out of flow:{}", port);


            } else {
                //2.过期
                Date expireTime = portInfo.getExpireTime();
                Date now = new Date();
                if (expireTime.before(now)) {
                    unavailableFlag = true;
                    log.info("port is out of date:{}", port);
                }
            }

            if (unavailableFlag) {

                //1.db设为不可用
                UpdateWrapper<PortInfo> wrapper = new UpdateWrapper<>();
                wrapper.set("use_type", 0);
                wrapper.eq("port", port);
                portInfoMapper.update(null, wrapper);

                //2.发送删除命令
                boolean sendFlag = udpCommandService.sendDelete(port);
                if (!sendFlag) {
                    throw new RuntimeException("send error");
                }

                deleteCount++;
            }


        }

        log.info("delete port size:{}",deleteCount);


        //清除超时的token
        clearExpireToken();
    }


    /**
     * 清除过期的token 这个业务动作放到这个task里做吧先
     */
    private void clearExpireToken(){
        Set<Map.Entry<String, User>> entries = ConfigValue.tokenMap.entrySet();
        for(Map.Entry<String,User> entry:entries){
            User value = entry.getValue();
            long loginTime=value.getLoginTimeLong();
            if(System.currentTimeMillis()>loginTime+ TimeUnit.HOURS.toMillis(3)){
                //超时时间3个小时
                ConfigValue.tokenMap.remove(entry.getKey());
            }
        }
    }
}
