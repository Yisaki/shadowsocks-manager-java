package com.chaos.controller.api;

import com.chaos.annotation.Role;
import com.chaos.po.PortInfo;
import com.chaos.service.IPortService;
import com.chaos.util.CommonUtil;
import com.chaos.vo.CommonResult;
import com.chaos.vo.PortInfoVo;
import com.chaos.vo.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/api/port")
public class PortController {
    @Autowired
    private IPortService portService;

    @Role(1)
    @GetMapping("/list")
    public CommonResult<List<PortInfoVo>> list(){
        CommonResult<List<PortInfoVo>> result = new CommonResult<>();
        //当前登录用户
        User user=(User)RequestContextHolder.getRequestAttributes().getAttribute("currentUser", RequestAttributes.SCOPE_REQUEST);

        List<PortInfo> portInfos =null;
        if(user.getRole()==0){
            //管理员
            portInfos=portService.listAll(true);
        }else{
            //普通用户 只查自己的port list
            portInfos=portService.listByUser(user.getUser());
        }

        List<PortInfoVo> data=new ArrayList<>();

        PortInfoVo portInfoVo=null;
        for(PortInfo portInfo:portInfos){
            portInfoVo=new PortInfoVo();
            portInfoVo.setPort(portInfo.getPort());
            portInfoVo.setPassword(portInfo.getPassword());

            Long usedFlowMB=portInfo.getUsedFlow();
            Long totalFlowMB=portInfo.getTotalFlow();
            portInfoVo.setUsedFlow(usedFlowMB);
            portInfoVo.setTotalFlow(totalFlowMB);

            Date addTime=portInfo.getAddTime();
            portInfoVo.setAddTime(CommonUtil.formateDate(addTime));
            Date expireTime=portInfo.getExpireTime();
            portInfoVo.setExpireTime(CommonUtil.formateDate(expireTime));
            Date updateDate=portInfo.getUpdateTime();
            portInfoVo.setUpdateTime(CommonUtil.formateDate(updateDate));

            portInfoVo.setUseType(portInfo.getUseType());

            portInfoVo.setMark(portInfo.getMark());

            portInfoVo.setOwner(portInfo.getUserName());
            data.add(portInfoVo);
        }


        result.setData(data);
        return  result;
    }

    @Role(0)
    @GetMapping("/setPortUseType")
    public CommonResult<Void> setPortUseType(@RequestParam int port,@RequestParam int useType){
        CommonResult commonResult=new CommonResult();

        if(useType==0){
           boolean result=portService.remove(port);
            commonResult.setSuccess(result);
        }else{
            PortInfoVo portInfoVo=new PortInfoVo();
            portInfoVo.setPort(port);
            portInfoVo.setUseType(useType);

            boolean result= portService.update(portInfoVo);
            commonResult.setSuccess(result);
        }
        return commonResult;
    }

    @Role(0)
    @PostMapping("/update")
    public CommonResult<Void> update(@RequestBody PortInfoVo portInfoVo){
        boolean flag=portService.update(portInfoVo);
        CommonResult<Void> result=new CommonResult<>();
        result.setSuccess(flag);

        return result;
    }

    @Role(0)
    @PostMapping("/add")
    public CommonResult<Void> add(@RequestBody PortInfoVo portInfoVo) {

        CommonResult<Void> result = new CommonResult<>();

        Integer useDay=portInfoVo.getDay();
        if(useDay==null){
            //没传
            portInfoVo.setDay(0);
        }
        Long totalFlow=portInfoVo.getFlow();
        if(totalFlow==null){
            //没传
            portInfoVo.setFlow(0L);
        }

        boolean flag = portService.add(portInfoVo);
        result.setSuccess(flag);


        return result;
    }

    @Role(0)
    @GetMapping("/remove")
    public CommonResult<Void> remove(int port) {
        CommonResult<Void> result = new CommonResult<>();

        boolean flag = portService.remove(port);
        result.setSuccess(flag);


        return result;
    }

    @Role(0)
    @PostMapping("/bindUser")
    public CommonResult<Void> bindUser(@RequestBody PortInfoVo portInfoVo){
        CommonResult<Void> result = new CommonResult<>();
        boolean res=portService.bindUser(portInfoVo.getPort(),portInfoVo.getOwner());
        result.setSuccess(res);
        return result;
    }

    @Role(0)
    @GetMapping("/listUnbind")
    public CommonResult<List<PortInfo>> listUnbind(){
        CommonResult<List<PortInfo>> result = new CommonResult<>();
        List<PortInfo> portInfos = portService.listUnbind();
        result.setData(portInfos);
        return result;
    }

}
