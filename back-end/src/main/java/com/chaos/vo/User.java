package com.chaos.vo;

import lombok.Data;

import java.util.Date;

@Data
public class User {
    private String user;
    private String password;
    private Integer role;
    private String token;
    private String loginTime;
    private Long loginTimeLong;
}
