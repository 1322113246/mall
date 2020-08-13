package com.lb.gulimail.member.vo;

import lombok.Data;
import lombok.ToString;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;

@Data
@ToString
public class UserRegistVo {

    private String userName;

    private String password;

    private String phone;
    private String code;

}
