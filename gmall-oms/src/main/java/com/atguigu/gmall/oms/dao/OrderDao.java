package com.atguigu.gmall.oms.dao;

import com.atguigu.gmall.oms.entity.OrderEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 订单
 * 
 * @author songyang
 * @email 68873846@qq.com
 * @date 2021-03-31 00:16:10
 */
@Mapper
public interface OrderDao extends BaseMapper<OrderEntity> {
	
}
