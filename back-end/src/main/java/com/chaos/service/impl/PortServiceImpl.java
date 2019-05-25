package com.chaos.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.chaos.mapper.PortInfoMapper;
import com.chaos.po.PortInfo;
import com.chaos.service.IPortService;
import com.chaos.service.IUDPCommandService;
import com.chaos.vo.PortInfoVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Slf4j
@Service
public class PortServiceImpl implements IPortService {

    @Autowired
    private PortInfoMapper portInfoMapper;

    @Autowired
    private IUDPCommandService udpCommandService;

    @Override
    @Transactional
    public boolean add(PortInfoVo portInfoVo) {

        QueryWrapper<PortInfo> queryWrapper=new QueryWrapper<>();
        queryWrapper.eq("port",portInfoVo.getPort());
        PortInfo dbPortInfo = portInfoMapper.selectOne(queryWrapper);
        if(dbPortInfo!=null){
            throw new RuntimeException("already exist port:"+portInfoVo.getPort());
        }

        Integer port=portInfoVo.getPort();
        String psw=portInfoVo.getPassword();
        Integer useDay=portInfoVo.getDay();
        Long totalFlow=portInfoVo.getFlow();
        Integer type=portInfoVo.getUseType();
        //分布式事务

        //1.入库
        PortInfo portInfo = new PortInfo();
        portInfo.setPort(port);
        portInfo.setPassword(psw);
        portInfo.setUseType(type);

        //有限账号
        long nowMil = System.currentTimeMillis();
        Date now = new Date(nowMil);
        portInfo.setAddTime(now);

        long afterMil = nowMil + (useDay * 24 * 60 * 60 * 1000);
        Date after = new Date(afterMil);
        portInfo.setExpireTime(after);

        portInfo.setTotalFlow(totalFlow);


        portInfoMapper.insert(portInfo);

        //2.发送UDP
        boolean resp = udpCommandService.sendAdd(port, psw);
        if (!resp) {
            //触发回滚
            throw new RuntimeException("req ss fail");
        }

        return true;

    }


    /**
     * 查库并重新发送add命令
     *
     * @return
     */
    public List<PortInfo> listAndResendAll() {
        List<PortInfo> list = listAll(false);
        for (PortInfo portInfo : list) {
            int port = portInfo.getPort();
            String psw = portInfo.getPassword();
            boolean sendFlag = udpCommandService.sendAdd(port, psw);
            if (!sendFlag) {
                log.error("add port fail:{}", port);

                UpdateWrapper<PortInfo> wrapper = new UpdateWrapper<>();
                wrapper.set("use_type", 0);
                wrapper.eq("port", port);
                portInfoMapper.update(null, wrapper);

                portInfo.setUseType(0);
            }
        }

        return list;
    }

    public List<PortInfo> listAll(boolean allFlag) {
        QueryWrapper<PortInfo> wrapper = null;
        if (!allFlag) {
            wrapper = new QueryWrapper<>();
            wrapper.eq("use_type", 1).or().eq("use_type",2);
        }
        return portInfoMapper.selectList(wrapper);
    }

    public List<PortInfo> listByUser(String user){
        QueryWrapper<PortInfo> wrapper = null;
        wrapper = new QueryWrapper<>();
        wrapper.eq("fk_user_info_name",user);

        return portInfoMapper.selectList(wrapper);
    }

    public boolean ping() {
        String resp = udpCommandService.sendPing();
        if ("pong".equals(resp)) {
            return true;
        } else {
            return false;
        }
    }


    @Transactional
    @Override
    public boolean remove(int port) {
        UpdateWrapper<PortInfo> wrapper = new UpdateWrapper<>();
        wrapper.eq("port", port);
        wrapper.set("use_type", 0);
        int update = portInfoMapper.update(null, wrapper);

        if(update>0){
            boolean sendDelete = udpCommandService.sendDelete(port);
            if (!sendDelete) {
                log.error("send delete fail,port:{}", port);
                throw new RuntimeException("send delete fail");
            }
        }

        return true;
    }

    @Override
    public boolean update(PortInfoVo portInfoVo) {
        QueryWrapper<PortInfo> wrapper = new QueryWrapper<>();
        wrapper.eq("port", portInfoVo.getPort());
        PortInfo portInfo = portInfoMapper.selectOne(wrapper);
        if (portInfo == null) {
            return false;
        }

        //老的记录
        Long totalFlow = portInfo.getTotalFlow();
        Date expireTime = portInfo.getExpireTime();

        //MB
        Long modifyFlow = portInfoVo.getFlow();
        Integer modifyDay = portInfoVo.getDay();


        //新的记录
        Date newExpireTime = null;
        Long newTotalFlow = null;

        //待更新的对象
        PortInfo newPortInfo = new PortInfo();

        //计算新时间
        if (modifyDay!=null&&modifyDay > 0) {
            Long modifyMil = modifyDay * 24L * 60 * 60 * 1000;
            Long newMil = null;
            if (portInfoVo.getDayType() == 0) {
                newMil = expireTime.getTime() - modifyMil;
            } else {
                newMil = expireTime.getTime() + modifyMil;
            }
            newExpireTime = new Date(newMil);
            newPortInfo.setExpireTime(newExpireTime);
        }

        //计算新流量
        if (modifyFlow!=null&&modifyFlow > 0) {
            if (portInfoVo.getFlowType() == 0) {
                if (modifyFlow < totalFlow) {
                    //需要小于之前的
                    newTotalFlow = totalFlow - modifyFlow;
                }
            } else {
                newTotalFlow = totalFlow + modifyFlow;
            }
            newPortInfo.setTotalFlow(newTotalFlow);
        }

        //useType
        Integer useType=portInfoVo.getUseType();
        if(useType==1||useType==2){
            //todo
            //0置为不可用 应走删除逻辑 不应该在这里处理
            newPortInfo.setUseType(useType);
        }


        //更新DB
        if (newExpireTime != null || newTotalFlow != null|| newPortInfo.getUseType()!=null) {

            if (portInfo.getUseType()==0) {
                //触发一次增加端口请求
                boolean sendFlag = udpCommandService.sendAdd(portInfo.getPort(), portInfo.getPassword());
            }
            newPortInfo.setId(portInfo.getId());
            boolean dbFlag = portInfoMapper.updateById(newPortInfo) == 1;

            return dbFlag;
        } else {
            return false;
        }
    }

    @Override
    public boolean bindUser(int port, String user) {
        QueryWrapper<PortInfo> wrapper = new QueryWrapper<>();
        wrapper.eq("port", port);
        PortInfo portInfo = portInfoMapper.selectOne(wrapper);
        if (portInfo == null) {
            return false;
        }
        //待更新的对象
        PortInfo newPortInfo = new PortInfo();
        newPortInfo.setId(portInfo.getId());
        newPortInfo.setUserName(user);

        return portInfoMapper.updateById(newPortInfo)>0;

    }

    @Override
    public List<PortInfo> listUnbind() {
        QueryWrapper<PortInfo> wrapper = new QueryWrapper<>();
        wrapper.isNull("fk_user_info_name").or().eq("fk_user_info_name","");

        return portInfoMapper.selectList(wrapper);
    }
}
