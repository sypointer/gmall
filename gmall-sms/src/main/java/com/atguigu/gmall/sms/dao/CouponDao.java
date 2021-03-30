package com.atguigu.gmall.sms.dao;

import com.atguigu.gmall.sms.entity.CouponEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 优惠券信息
 * 
 * @author songyang
 * @email 68873846@qq.com
 * @date 2021-03-31 00:18:01
 */
@Mapper
public interface CouponDao extends BaseMapper<CouponEntity> {
	
}
