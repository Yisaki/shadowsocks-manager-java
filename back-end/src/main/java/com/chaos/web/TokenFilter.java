package com.chaos.web;

import com.alibaba.fastjson.JSON;
import com.chaos.config.ConfigValue;
import com.chaos.vo.CommonResult;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 *
 */
/*@WebFilter(urlPatterns = "/api/*")*/
public class TokenFilter implements Filter{
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {/*
        HttpServletRequest request=(HttpServletRequest)servletRequest;
        String uri=request.getRequestURI();
        if(uri.contains("login")){
            filterChain.doFilter(servletRequest,servletResponse);
        }else{

        }
        String token=request.getHeader("token");
        if(token==null||ConfigValue.appToken.get(token)==null){
            CommonResult<Void> resp=new CommonResult();
            resp.setSuccess(false);
            resp.setDesc("token is wrong");
            servletResponse.getWriter().print(JSON.toJSON(resp));
            servletResponse.getWriter().flush();
        }else{
            filterChain.doFilter(servletRequest,servletResponse);
        }*/
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void destroy() {

    }
}
