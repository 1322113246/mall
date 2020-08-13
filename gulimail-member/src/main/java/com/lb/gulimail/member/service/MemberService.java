package com.lb.gulimail.member.service;

import com.lb.gulimail.member.vo.SocialUser;
import com.lb.gulimail.member.vo.UserLoginVo;
import com.lb.gulimail.member.vo.UserRegistVo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lb.common.utils.PageUtils;
import com.lb.gulimail.member.entity.MemberEntity;

import java.util.Map;

/**
 * 会员
 *
 * @author liangbin
 * @email 1322113246@qq.com
 * @date 2020-06-07 17:17:46
 */
public interface MemberService extends IService<MemberEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void regist(UserRegistVo userRegistVo);

    MemberEntity login(UserLoginVo userLoginVo);

    MemberEntity login(SocialUser socialUser);
}

