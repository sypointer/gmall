package com.atguigu.gmall.pms.vo;

import com.atguigu.gmall.pms.entity.AttrAttrgroupRelationEntity;
import com.atguigu.gmall.pms.entity.AttrEntity;
import com.atguigu.gmall.pms.entity.AttrGroupEntity;
import lombok.Data;

import java.util.List;

@Data
public class AttrGroupVO extends AttrGroupEntity {
    // 商品属性
    private List<AttrEntity> attrEntities;
    // 属性&属性分组关联
    private List<AttrAttrgroupRelationEntity> relations;
}
