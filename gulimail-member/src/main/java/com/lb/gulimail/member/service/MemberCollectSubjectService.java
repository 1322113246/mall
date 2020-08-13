package com.lb.gulimail.member.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lb.common.utils.PageUtils;
import com.lb.gulimail.member.entity.MemberCollectSubjectEntity;

import java.util.Map;

/**
 * 会员收藏的专题活动
 *
 * @author liangbin
 * @email 1322113246@qq.com
 * @date 2020-06-07 17:17:46
 */
public interface MemberCollectSubjectService extends IService<MemberCollectSubjectEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

