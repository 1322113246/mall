package com.lb.gulimail.order.interceptor;

import com.lb.common.constant.AuthServerConstant;
import com.lb.common.vo.MemberRespVo;

import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 在执行controller方法前，判断用户是临时用户还是系统用户
 */
@Component
public class OrderInterceptor implements HandlerInterceptor{
    public static ThreadLocal<MemberRespVo> threadLocal=new ThreadLocal<>();
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        boolean match = new AntPathMatcher().match("/order/order/status/**", request.getRequestURI());
        boolean match1 = new AntPathMatcher().match("/payed/notify", request.getRequestURI());
        if (match||match1){
            return true;
        }
        MemberRespVo attribute = (MemberRespVo) request.getSession().getAttribute(AuthServerConstant.LOGIN_USER);
        if (attribute!=null){
            //登录成功
            threadLocal.set(attribute);
            return true;
        }else {
            //没登陆就跳转到登录页面
            response.sendRedirect("http://auth.gulimall.com/login.html");
            return false;
        }
    }

}
