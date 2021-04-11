package com.atguigu.gmall.pms.service;

import com.atguigu.gmall.pms.vo.AttrVO;
import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.gmall.pms.entity.AttrEntity;
import com.atguigu.core.bean.PageVo;
import com.atguigu.core.bean.QueryCondition;


/**
 * 商品属性
 *
 * @author songyang
 * @email 68873846@qq.com
 * @date 2021-03-31 00:03:41
 */
public interface AttrService extends IService<AttrEntity> {

    PageVo queryPage(QueryCondition params);

    PageVo queryAttrByCid(Integer type, Long cid, QueryCondition queryCondition);

    void saveGroupIdAndAttrId(AttrVO attrVO);
}

