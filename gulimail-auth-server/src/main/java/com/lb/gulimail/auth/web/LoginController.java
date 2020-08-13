package com.lb.gulimail.auth.web;

import com.alibaba.fastjson.TypeReference;
import com.lb.common.exception.BizCodeEnume;
import com.lb.common.utils.R;
import com.lb.common.constant.AuthServerConstant;
import com.lb.common.vo.MemberRespVo;
import com.lb.gulimail.auth.feign.MemberFeignService;
import com.lb.gulimail.auth.feign.ThirdPartyFeignService;
import com.lb.gulimail.auth.vo.UserLoginVo;
import com.lb.gulimail.auth.vo.UserRegistVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Controller
public class LoginController {
    @Autowired
    ThirdPartyFeignService thirdPartyFeignService;

    @Autowired
    StringRedisTemplate stringRedisTemplate;
    @Autowired
    MemberFeignService memberFeignService;
    @ResponseBody
    @GetMapping("/sms/sendcode")
    public R sendCode(@RequestParam("phone") String phone){
        //20S 不能多次发送
        ValueOperations<String, String> ops = stringRedisTemplate.opsForValue();
        String key= AuthServerConstant.SMS_CODE_CACHE+phone;
        String code_time = ops.get(key);
        if (!StringUtils.isEmpty(code_time)){
            long time = Long.parseLong(code_time.split("_")[1]);
            long currentTime = System.currentTimeMillis();
            long exitTime=currentTime-time;
            if (exitTime<60000){
                //不能继续发
                Long seconds = ops.getOperations().getExpire(key);
                return R.error(BizCodeEnume.SMS_CODE_EXCEPTION).put("exitTime",seconds);
            }
        }
        //TODO 接口防刷 验证码再次校验
        String code = UUID.randomUUID().toString().substring(0, 5);
        String value=code+"_"+System.currentTimeMillis();
        thirdPartyFeignService.sendCode(phone,code);
        ops.set(key,value,60, TimeUnit.SECONDS);
        return R.ok();
    }

    @PostMapping("/regist")
    public String regist(@Valid UserRegistVo userRegistVo, BindingResult result, RedirectAttributes redirectAttributes){
        if (result.hasErrors()){
            Map<String, String> map = result.getFieldErrors().stream().collect(Collectors.toMap(FieldError::getField,FieldError::getDefaultMessage));
            redirectAttributes.addFlashAttribute("errors",map);
            return "redirect:http://auth.gulimall.com/reg.html";
        }
        //调用远程服务
        //验证码校验
         String code = userRegistVo.getCode();
        String phone = userRegistVo.getPhone();
        ValueOperations<String, String> ops = stringRedisTemplate.opsForValue();
        String value = ops.get(AuthServerConstant.SMS_CODE_CACHE + phone);
        if (!StringUtils.isEmpty(value)&&code.equalsIgnoreCase(value.split("_")[0])){
            //验证通过
            stringRedisTemplate.delete(AuthServerConstant.SMS_CODE_CACHE + phone);
            //注册用户
            R r = memberFeignService.regist(userRegistVo);
            if (r.getCode()==0){
                //注册成功回到登录页

                return "redirect:http://auth.gulimall.com/login.html";
            }else {
                Map<String, String> map = new HashMap<>();
                map.put("msg",r.getMsg());
                redirectAttributes.addFlashAttribute("errors",map);
                return "redirect:http://auth.gulimall.com/reg.html";
            }
        }else {
            Map<String, String> map = new HashMap<>();
            map.put("errors","验证码失效或错误");
            redirectAttributes.addFlashAttribute("errors",map);
            return "redirect:http://auth.gulimall.com/reg.html";
        }

    }

    /**
     * 登录
     * @param userLoginVo
     * @param redirectAttributes
     * @param session
     * @return
     */
    @PostMapping("/login")
    public String login(UserLoginVo userLoginVo, RedirectAttributes redirectAttributes,
                        HttpSession session){
        //验证登录信息
        R r = memberFeignService.login(userLoginVo);
        if (r.getCode()==0){
            session.setAttribute(AuthServerConstant.LOGIN_USER,r.getData(new TypeReference<MemberRespVo>(){}));
            return "redirect:http://gulimall.com";
        }else {
            Map<String,String> errors=new HashMap<>();
            errors.put("msg",r.getMsg());
            redirectAttributes.addFlashAttribute("errors",errors);
            return "redirect:http://auth.gulimall.com/login.html";
        }
    }
    /**
     * 系统首页页面跳转
     */
    @GetMapping("/login.html")
    public String loginPage(HttpSession httpSession){
        //登录过了
        Object loginUser = httpSession.getAttribute("loginUser");
        if (loginUser==null){
            return "login";
        }else {
            return "redirect:http://gulimall.com";
        }
    }

}
