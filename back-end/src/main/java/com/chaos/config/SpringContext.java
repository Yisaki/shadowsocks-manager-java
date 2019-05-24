package com.chaos.config;

import com.chaos.service.IPortService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class SpringContext implements ApplicationListener <ContextRefreshedEvent>,ApplicationContextAware{

    public static ApplicationContext applicationContext;


    @Autowired
    private IPortService portService;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        SpringContext.applicationContext=applicationContext;
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
        //初始化
        log.info("spring started ....");

        boolean pingFlag = portService.ping();
        if(!pingFlag){
            //关闭容器
            log.error("shadow server fail,shut down!!!!!");
            System.exit(0);
            return;
        }

        portService.listAndResendAll();
        log.info("resend all port finish ...");
    }




}
