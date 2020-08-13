package com.lb.gulimail.member;

import com.lb.gulimail.member.entity.MemberEntity;
import com.lb.gulimail.member.service.MemberService;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class GulimailMemberApplicationTests {
    @Autowired
    MemberService service;

    @Test
    void contextLoads() {
        MemberEntity memberEntity = new MemberEntity();
        memberEntity.setUsername("1322113246@qq.com");
        memberEntity.setPassword("bin79302");
        memberEntity.setNickname("梁斌");
        service.save(memberEntity);
    }

}
