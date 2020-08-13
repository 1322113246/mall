package com.lb.gulimail.auth.vo;

import lombok.Data;
import lombok.ToString;
import org.hibernate.validator.constraints.Length;
import org.springframework.web.bind.annotation.PathVariable;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;

@Data
@ToString
public class UserRegistVo {
    @NotEmpty(message = "用户名必须提交")
    @Length(min=6,max = 18,message = "用户名必须在6-18个字符之间")
    private String userName;
    @NotEmpty(message = "密码必须填写")
    @Length(min = 6,max = 18,message = "用户名必须在6-18个字符之间")
    private String password;
    @Pattern(regexp = "^1[3456789]\\d{9}$",message = "手机号格式不正确")
    private String phone;
    @NotEmpty(message = "验证码必须填写")
    private String code;

}
