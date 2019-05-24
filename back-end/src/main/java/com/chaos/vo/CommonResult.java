package com.chaos.vo;

import lombok.Data;

@Data
public class CommonResult<T> {

    private boolean success=true;
    private Integer code=0;
    private String desc;
    private T data;
}
