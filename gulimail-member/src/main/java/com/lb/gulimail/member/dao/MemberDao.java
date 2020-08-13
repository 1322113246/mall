package com.lb.gulimail.member.dao;

import com.lb.gulimail.member.entity.MemberEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 会员
 *
 * @author liangbin
 * @email 1322113246@qq.com
 * @date 2020-06-07 17:17:46
 */
@Mapper
public interface MemberDao extends BaseMapper<MemberEntity> {

}
