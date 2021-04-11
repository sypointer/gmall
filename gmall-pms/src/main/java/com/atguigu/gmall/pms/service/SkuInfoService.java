package com.atguigu.gmall.pms.service;

import com.atguigu.core.bean.Resp;
import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.gmall.pms.entity.SkuInfoEntity;
import com.atguigu.core.bean.PageVo;
import com.atguigu.core.bean.QueryCondition;

import java.util.List;


/**
 * sku信息
 *
 * @author songyang
 * @email 68873846@qq.com
 * @date 2021-03-31 00:03:41
 */
public interface SkuInfoService extends IService<SkuInfoEntity> {

    PageVo queryPage(QueryCondition params);

}

