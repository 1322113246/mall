package com.lb.gulimail.order.feign;

import com.lb.gulimail.order.vo.MemberAddressVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient("gulimail-member")
public interface MemberFeignService {
    @GetMapping("member/memberreceiveaddress/address")
    List<MemberAddressVo> getAddr(@RequestParam("memberId") Long memberId);
}
