package com.lb.gulimail.member.controller;

import java.util.Arrays;
import java.util.Map;

import com.lb.common.exception.BizCodeEnume;
import com.lb.gulimail.member.exception.PhoneExitException;
import com.lb.gulimail.member.exception.UserNameExitException;
import com.lb.gulimail.member.feign.CouponFeignService;
import com.lb.gulimail.member.vo.SocialUser;
import com.lb.gulimail.member.vo.UserLoginVo;
import com.lb.gulimail.member.vo.UserRegistVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.lb.gulimail.member.entity.MemberEntity;
import com.lb.gulimail.member.service.MemberService;
import com.lb.common.utils.PageUtils;
import com.lb.common.utils.R;


/**
 * 会员
 *
 * @author liangbin
 * @email 1322113246@qq.com
 * @date 2020-06-07 17:17:46
 */
@RestController
@RequestMapping("member/member")
public class MemberController {
    @Autowired
    private MemberService memberService;

    @Autowired
    private CouponFeignService couponFeignService;

    @RequestMapping("/coupons")
    public R test() {
        MemberEntity memberEntity = new MemberEntity();
        memberEntity.setNickname("张三");

        R memberCoupons = couponFeignService.memberCoupons();

        return R.ok().put("member", memberEntity).put("coupons", memberCoupons.get("coupons"));
    }

    /**
     * 列表
     */
    @RequestMapping("/list")
    //@RequiresPermissions("member:member:list")
    public R list(@RequestParam Map<String, Object> params) {
        PageUtils page = memberService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    //@RequiresPermissions("member:member:info")
    public R info(@PathVariable("id") Long id) {
        MemberEntity member = memberService.getById(id);

        return R.ok().put("member", member);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    //@RequiresPermissions("member:member:save")
    public R save(@RequestBody MemberEntity member) {
        memberService.save(member);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    //@RequiresPermissions("member:member:update")
    public R update(@RequestBody MemberEntity member) {
        memberService.updateById(member);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    //@RequiresPermissions("member:member:delete")
    public R delete(@RequestBody Long[] ids) {
        memberService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

    /**
     * 注册
     * @param userRegistVo
     * @return
     */
    @PostMapping("/regist")
    public R regist(@RequestBody UserRegistVo userRegistVo){
        try {
            memberService.regist(userRegistVo);
        }catch (PhoneExitException p){
            return R.error(BizCodeEnume.PHONE_EXIST_EXCEPTION);
        }catch (UserNameExitException u){
            return R.error(BizCodeEnume.USER_EXIST_EXCEPTION);
        }
        return R.ok();
    }
    /**
     * 登录
     */
    @PostMapping("/login")
    public R login(@RequestBody UserLoginVo userLoginVo){
        MemberEntity memberEntity=memberService.login(userLoginVo);
        if (memberEntity!=null){
            return R.ok().setData(memberEntity);
        }else {
            return R.error(BizCodeEnume.USER_PASSWORD_INVALID);
        }
    }
    /**
     * 处理社交登录
     */
    @PostMapping("/oauth2/login")
    public R oauthLogin(@RequestBody SocialUser socialUser){
        MemberEntity memberEntity=memberService.login(socialUser);
        return R.ok().setData(memberEntity);
    }

}
