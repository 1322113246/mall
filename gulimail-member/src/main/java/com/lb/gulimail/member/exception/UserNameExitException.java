package com.lb.gulimail.member.exception;

public class UserNameExitException extends RuntimeException{
    public UserNameExitException(){
        super("用户名存在");
    }
}
