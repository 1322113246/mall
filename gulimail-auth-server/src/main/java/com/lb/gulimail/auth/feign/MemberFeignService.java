package com.lb.gulimail.auth.feign;

import com.lb.common.utils.R;
import com.lb.gulimail.auth.vo.SocialUser;
import com.lb.gulimail.auth.vo.UserLoginVo;
import com.lb.gulimail.auth.vo.UserRegistVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient("gulimail-member")
public interface MemberFeignService {
    @PostMapping("/member/member/regist")
    R regist(@RequestBody UserRegistVo userRegistVo);

    @PostMapping("/member/member/login")
    R login(@RequestBody UserLoginVo userLoginVo);

    @PostMapping("/member/member/oauth2/login")
    public R oauthLogin(@RequestBody SocialUser socialUser);
}
