package com.lb.gulimail.member.exception;

public class PhoneExitException extends RuntimeException{
    public PhoneExitException(){
        super("手机号存在");
    }
}
