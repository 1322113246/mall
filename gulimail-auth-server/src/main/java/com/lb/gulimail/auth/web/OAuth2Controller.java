package com.lb.gulimail.auth.web;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.lb.common.utils.HttpUtils;
import com.lb.common.utils.R;
import com.lb.gulimail.auth.feign.MemberFeignService;
import com.lb.common.vo.MemberRespVo;
import com.lb.gulimail.auth.vo.SocialUser;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpSession;
import java.util.HashMap;

/**
 * 社交登录
 */
@Controller
public class  OAuth2Controller {
    @Autowired
    MemberFeignService memberFeignService;
    @GetMapping("/oauth2.0/weibo/success")
    public String weibo(@RequestParam("code") String code, HttpSession session) throws Exception {
        //1.根据code换accessToken
        HashMap<String, String> map = new HashMap<>();
        map.put("client_id","2898809631");
        map.put("client_secret","d5f5c980c791692b280551713db4bf04");
        map.put("grant_type","authorization_code");
        map.put("redirect_uri","http://auth.gulimall.com/oauth2.0/weibo/success");
        map.put("code",code);
        HttpResponse response = HttpUtils.doPost("https://api.weibo.com", "/oauth2/access_token", "post", null,null, map);
        //接受access_token并处理
        if (response.getStatusLine().getStatusCode()==200){
            //成功获取
            String json = EntityUtils.toString(response.getEntity());
            SocialUser socialUser = JSON.parseObject(json, SocialUser.class);
            //当该社交用户第一次登录则自动注册
            R r = memberFeignService.oauthLogin(socialUser);
            if (r.getCode()==0){
                MemberRespVo memberRespVo = r.getData(new TypeReference<MemberRespVo>(){});
                System.out.println("----登录成功:"+memberRespVo);
                session.setAttribute("loginUser",memberRespVo);
                return "redirect:http://gulimall.com";
            }else {
                return "redirect:http://auth.gulimall.com/login.html";
            }
        }else {
            return "redirect:http://auth.gulimall.com/login.html";
        }

    }
}
