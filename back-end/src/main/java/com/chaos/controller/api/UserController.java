package com.chaos.controller.api;

import com.chaos.annotation.Role;
import com.chaos.config.ConfigValue;
import com.chaos.service.IUserService;
import com.chaos.vo.CommonResult;
import com.chaos.vo.User;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private IUserService userService;

    @PostMapping("/login")
    public CommonResult<User> login(@RequestBody User user){
        CommonResult<User> resp=new CommonResult<>();
        String name=user.getUser();
        String password=user.getPassword();
        if(StringUtils.isBlank(name)||StringUtils.isBlank(password)){
            throw new RuntimeException("用户名或密码为空");
        }

        User data = userService.authUser(name, password);
        if(data==null){
            resp.setSuccess(false);
            resp.setDesc("login fail");
        }else{
            resp.setData(data);
        }


        return resp;
    }

    @Role(1)
    @GetMapping("/logout")
    public CommonResult<Void> logout(@RequestHeader String token){
        CommonResult<Void> resp=new CommonResult<>();

        ConfigValue.tokenMap.remove(token);

        return resp;
    }
}
