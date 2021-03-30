package com.atguigu.gmall.sms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.gmall.sms.entity.SeckillSkuNoticeEntity;
import com.atguigu.core.bean.PageVo;
import com.atguigu.core.bean.QueryCondition;


/**
 * 秒杀商品通知订阅
 *
 * @author songyang
 * @email 68873846@qq.com
 * @date 2021-03-31 00:18:01
 */
public interface SeckillSkuNoticeService extends IService<SeckillSkuNoticeEntity> {

    PageVo queryPage(QueryCondition params);
}

