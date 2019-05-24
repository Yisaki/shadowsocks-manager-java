package com.chaos.service;

import com.chaos.po.UserInfo;
import com.chaos.vo.User;

public interface IUserService {

    /**
     * 用户鉴权
     * @param name
     * @param password
     * @return
     */
    User authUser(String name, String password);

    UserInfo getUser(String name);
}
