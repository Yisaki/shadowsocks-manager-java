package com.chaos.aop;

import com.chaos.vo.CommonResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

@Slf4j
@ControllerAdvice
public class ControllerAspect {

    @ResponseBody
    @ExceptionHandler(value = Exception.class)
    public CommonResult<Void> errorHandler(Exception ex) {
        log.error("api exception:",ex);
        CommonResult commonResult=new CommonResult();
        commonResult.setDesc(ex.getMessage());
        commonResult.setSuccess(false);
        return commonResult;
    }


}
