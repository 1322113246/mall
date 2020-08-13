package com.lb.gulimail.member.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.lb.common.utils.HttpUtils;
import com.lb.gulimail.member.dao.MemberLevelDao;
import com.lb.gulimail.member.entity.MemberLevelEntity;
import com.lb.gulimail.member.exception.PhoneExitException;
import com.lb.gulimail.member.exception.UserNameExitException;
import com.lb.gulimail.member.vo.SocialUser;
import com.lb.gulimail.member.vo.UserLoginVo;
import com.lb.gulimail.member.vo.UserRegistVo;
import com.lb.gulimail.member.entity.MemberEntity;
import com.lb.gulimail.member.service.MemberService;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lb.common.utils.PageUtils;
import com.lb.common.utils.Query;

import com.lb.gulimail.member.dao.MemberDao;


@Service("memberService")
public class MemberServiceImpl extends ServiceImpl<MemberDao, MemberEntity> implements MemberService {
    @Autowired
    MemberLevelDao memberLevelDao;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<MemberEntity> page = this.page(
                new Query<MemberEntity>().getPage(params),
                new QueryWrapper<MemberEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void regist(UserRegistVo userRegistVo) {
        MemberEntity memberEntity = new MemberEntity();

        //设置手机号
        checkPhone(userRegistVo.getPhone());
        memberEntity.setMobile(userRegistVo.getPhone());
        //设置用户名
        checkUserName(userRegistVo.getUserName());
        memberEntity.setUsername(userRegistVo.getUserName());
        //设置默认昵称
        memberEntity.setNickname(userRegistVo.getUserName());
        //设置密码并加密
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        String encode = passwordEncoder.encode(userRegistVo.getPassword());
        memberEntity.setPassword(encode);
        //设置默认等级
        MemberLevelEntity levelEntity=memberLevelDao.getDefaultLevel();
        memberEntity.setLevelId(levelEntity.getId());
        baseMapper.insert(memberEntity);
    }

    /**
     * 登录业务逻辑
     * @param userLoginVo
     * @return
     */
    @Override
    public MemberEntity login(UserLoginVo userLoginVo) {
        String loginacct = userLoginVo.getLoginacct();
        String password = userLoginVo.getPassword();
        MemberDao memberDao = this.baseMapper;
        MemberEntity memberEntity = memberDao.selectOne(new QueryWrapper<MemberEntity>().eq("username", loginacct));
        if (memberEntity==null){
            return null;
        }
        //密码匹配
        String encodePassword = memberEntity.getPassword();
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        boolean matches = passwordEncoder.matches(password, encodePassword);
        if (matches){
            //匹配成功
            return memberEntity;
        }else {
            //匹配失败
            return null;
        }
    }

    /**
     * https://api.weibo.com/2/users/show.json?access_token=2.00eJwCmDDoGLKD11b5126cc5tPFAMB&uid=3458362830
     * 社交登录逻辑
     * @param socialUser
     * @return
     */
    @Override
    public MemberEntity login(SocialUser socialUser) {
        String uid = socialUser.getUid();
        //1.判断社交用户是否已经注册过
        MemberDao memberDao= this.baseMapper;
        MemberEntity memberEntity = memberDao.selectOne(new QueryWrapper<MemberEntity>().eq("social_uid", uid));
        if (memberEntity!=null){
            //表示社交用户已经注册，更新令牌
            memberEntity.setAccessToken(socialUser.getAccess_token());
            memberEntity.setExpiresIn(socialUser.getExpires_in());
            memberDao.updateById(memberEntity);
            return memberEntity;
        }else {
            //表示社交用户并未注册，并进行注册
            MemberEntity regist = new MemberEntity();
            //查询社交用户基本信息
            try {
                HashMap<String , String > query = new HashMap<>();
                query.put("access_token",socialUser.getAccess_token());
                query.put("uid", socialUser.getUid());
                HttpResponse response = HttpUtils.doGet("https://api.weibo.com", "/2/users/show.json", "get", new HashMap<>(), query);
                if (response.getStatusLine().getStatusCode()==200){
                    //查询成功
                    String string = EntityUtils.toString(response.getEntity());
                    JSONObject jsonObject = JSON.parseObject(string);
                    String nickName = jsonObject.getString("name");
                    String gender = jsonObject.getString("gender");
                    regist.setNickname(nickName);
                    regist.setGender("m".equalsIgnoreCase(gender)?1:0);
                }
            }catch (Exception e){

            }
            regist.setSocialUid(socialUser.getUid());
            regist.setAccessToken(socialUser.getAccess_token());
            regist.setExpiresIn(socialUser.getExpires_in());
            memberDao.insert(regist);
            return regist;
        }
    }

    public void checkPhone(String phone) throws PhoneExitException {
        MemberDao memberDao = this.baseMapper;
        Integer count = memberDao.selectCount(new QueryWrapper<MemberEntity>().eq("mobile", phone));
        if (count>0){
            throw new PhoneExitException();
        }
    }
    public void checkUserName(String userName) throws UserNameExitException {
        MemberDao memberDao = this.baseMapper;
        Integer count = memberDao.selectCount(new QueryWrapper<MemberEntity>().eq("username", userName));
        if (count>0){
            throw new PhoneExitException();
        }
    }
}