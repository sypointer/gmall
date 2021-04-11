package com.atguigu.gmall.pms.vo;

import com.atguigu.gmall.pms.entity.SpuInfoEntity;
import lombok.Data;

import java.util.List;

@Data
public class SpuInfoVO extends SpuInfoEntity {
    // spu图片
    private List<String> spuImages;

    //spu的基本属性
    private List<ProductAttrValueVO> baseAttrs;

    //sku信息
    private List<SkuInfoVO> skus;
}
