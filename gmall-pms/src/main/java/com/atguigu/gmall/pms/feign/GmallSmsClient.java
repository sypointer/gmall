package com.atguigu.gmall.pms.feign;

import com.atguigu.core.bean.Resp;
import com.atguigu.gmall.pms.vo.SaleVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 调用营销微服务的添加折扣信息方法
 */
@FeignClient("sms-service")
public interface GmallSmsClient {
    @PostMapping("sms/skubounds/sale")
    Resp<Object> saveSaleInfo(@RequestBody SaleVO saleVO);
}
