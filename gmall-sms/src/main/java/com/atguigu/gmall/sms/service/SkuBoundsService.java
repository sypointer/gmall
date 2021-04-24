package com.atguigu.gmall.sms.service;

import com.atguigu.core.bean.PageVo;
import com.atguigu.core.bean.QueryCondition;
import com.atguigu.gmall.sms.entity.SkuBoundsEntity;
import com.atguigu.gmall.sms.vo.SaleVO;
import com.baomidou.mybatisplus.extension.service.IService;


/**
 * 商品sku积分设置
 *
 * @author songyang
 * @email 68873846@qq.com
 * @date 2021-03-31 00:18:01
 */
public interface SkuBoundsService extends IService<SkuBoundsEntity> {

    PageVo queryPage(QueryCondition params);

    void saveSaleInfo(SaleVO saleVO);
}

