package com.atguigu.gmall.pms.feign;

import com.atguigu.gmall.sms.api.GmallSmsApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * 调用营销微服务的添加折扣信息方法
 */
@FeignClient("sms-service")
public interface GmallSmsClient extends GmallSmsApi {
}
