package com.atguigu.gmall.pms.api;

import com.atguigu.core.bean.QueryCondition;
import com.atguigu.core.bean.Resp;
import com.atguigu.gmall.pms.entity.BrandEntity;
import com.atguigu.gmall.pms.entity.CategoryEntity;
import com.atguigu.gmall.pms.entity.SkuInfoEntity;
import com.atguigu.gmall.pms.entity.SpuInfoEntity;
import com.atguigu.gmall.pms.vo.SpuAttributeValueVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

public interface GmallPmsApi {
    @PostMapping("pms/spuinfo/list")
    Resp<List<SpuInfoEntity>> querySpuPage(QueryCondition queryCondition);

    @GetMapping("pms/skuinfo/{spuId}")
    Resp<List<SkuInfoEntity>> getSkuInfoBySpuId(@PathVariable("spuId") Long spuId);

    @GetMapping("pms/brand/info/{brandId}")
    Resp<BrandEntity> queryBrandById(@PathVariable("brandId") Long brandId);

    @GetMapping("pms/category/info/{catId}")
    Resp<CategoryEntity> queryCategoryById(@PathVariable("catId") Long catId);

    @GetMapping("pms/productattrvalue/{spuId}")
    Resp<List<SpuAttributeValueVO>> querySearchAttrValue(@PathVariable("spuId") Long spuId);

    @PostMapping("pms/spuinfo/{status}")
    Resp<List<SpuInfoEntity>> querySpuInfoByStatus(@RequestBody QueryCondition condition,
                                                   @PathVariable("status") Integer status);
}
