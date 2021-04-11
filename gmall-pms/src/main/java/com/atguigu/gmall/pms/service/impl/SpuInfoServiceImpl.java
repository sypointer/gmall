package com.atguigu.gmall.pms.service.impl;

import com.atguigu.core.bean.PageVo;
import com.atguigu.core.bean.Query;
import com.atguigu.core.bean.QueryCondition;
import com.atguigu.gmall.pms.dao.*;
import com.atguigu.gmall.pms.entity.*;
import com.atguigu.gmall.pms.feign.GmallSmsClient;
import com.atguigu.gmall.pms.service.ProductAttrValueService;
import com.atguigu.gmall.pms.service.SpuInfoDescService;
import com.atguigu.gmall.pms.service.SpuInfoService;
import com.atguigu.gmall.pms.vo.ProductAttrValueVO;
import com.atguigu.gmall.pms.vo.SaleVO;
import com.atguigu.gmall.pms.vo.SkuInfoVO;
import com.atguigu.gmall.pms.vo.SpuInfoVO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.Date;
import java.util.List;
import java.util.UUID;


@Service("spuInfoService")
public class SpuInfoServiceImpl extends ServiceImpl<SpuInfoDao, SpuInfoEntity> implements SpuInfoService {

    @Autowired
    private SpuInfoDescDao spuInfoDescDao;
    @Autowired
    private ProductAttrValueDao productAttrValueDao;
    @Autowired
    private SkuInfoDao skuInfoDao;
    @Autowired
    private SkuImagesDao skuImagesDao;
    @Autowired
    private SkuSaleAttrValueDao skuSaleAttrValueDao;
    @Autowired
    private GmallSmsClient gmallSmsClient;

    @Override
    public PageVo queryPage(QueryCondition params) {
        IPage<SpuInfoEntity> page = this.page(
                new Query<SpuInfoEntity>().getPage(params),
                new QueryWrapper<>()
        );

        return new PageVo(page);
    }

    @Override
    public PageVo querySpuInfoByCatId(QueryCondition queryCondition, Long catId) {
        QueryWrapper<SpuInfoEntity> wrapper = new QueryWrapper<>();

        String key = queryCondition.getKey();

        // 判断key不为空
        if (StringUtils.isNotBlank(key)) {
            wrapper.and(t -> t.eq("id", key).or().like("spu_name", key));
        }

        // 判断catId不为0
        if (catId != 0) {
            wrapper.eq("catalog_id", catId);
        }

        IPage<SpuInfoEntity> page = this.page(
                new Query<SpuInfoEntity>().getPage(queryCondition),
                wrapper
        );

        return new PageVo(page);

    }

    /**
     * 九张表：
     * 1.spu相关：3
     * 2.sku相关：3
     * 3.营销相关：3
     *
     * @param spuInfoVO
     */
    @Transactional
    @Override
    public void bigSaveProduct(SpuInfoVO spuInfoVO) {
        // 新增需要有顺序
        // 1.新增spu相关
        // 1.1 新增pms_spu_info
        spuInfoVO.setCreateTime(new Date());
        spuInfoVO.setUodateTime(spuInfoVO.getCreateTime());
        this.save(spuInfoVO);
        Long spuId = spuInfoVO.getId();

        // 1.2 新增pms_spu_info_desc 因为有了spu_id，所以可以新增描述
        SpuInfoDescEntity spuInfoDescEntity = new SpuInfoDescEntity();
        String desc = StringUtils.join(spuInfoVO.getSpuImages(), ",");
        spuInfoDescEntity.setSpuId(spuId);
        spuInfoDescEntity.setDecript(desc);
        this.spuInfoDescDao.insert(spuInfoDescEntity);

        // 1.3 新增基本属性pms_product_attr_value
        List<ProductAttrValueVO> baseAttrs = spuInfoVO.getBaseAttrs();
        baseAttrs.forEach(baseAttr -> {
            baseAttr.setSpuId(spuId);
            baseAttr.setAttrSort(0);
            baseAttr.setQuickShow(1);
            this.productAttrValueDao.insert(baseAttr);
        });

        // 2.新增sku相关 先需要spu_id
        // 2.1 新增pms_sku_info
        List<SkuInfoVO> skuInfoVOs = spuInfoVO.getSkus();
        if (!CollectionUtils.isEmpty(skuInfoVOs)) {
            skuInfoVOs.forEach(skuInfoVO -> {
                SkuInfoEntity skuInfoEntity = new SkuInfoEntity();
                BeanUtils.copyProperties(skuInfoVO, skuInfoEntity);
                skuInfoEntity.setSpuId(spuId);
                skuInfoEntity.setBrandId(spuInfoVO.getBrandId());
                skuInfoEntity.setCatalogId(spuInfoVO.getCatalogId());
                skuInfoEntity.setSkuCode(UUID.randomUUID().toString());
                // 设置默认图片
                List<String> images = skuInfoVO.getImages();
                if (!CollectionUtils.isEmpty(images)) {
                    skuInfoEntity.setSkuDefaultImg(StringUtils.isNotBlank(skuInfoEntity.getSkuDefaultImg()) ?
                            skuInfoEntity.getSkuDefaultImg() : images.get(0));
                }

                this.skuInfoDao.insert(skuInfoEntity);
                Long skuId = skuInfoEntity.getSkuId();

                // 2.2 新增pms_sku_images
                if (!CollectionUtils.isEmpty(images)) {
                    images.forEach(image -> {
                        SkuImagesEntity skuImagesEntity = new SkuImagesEntity();
                        skuImagesEntity.setImgUrl(image);
                        skuImagesEntity.setImgSort(0);
                        skuImagesEntity.setDefaultImg(StringUtils.equals(skuInfoEntity.getSkuDefaultImg(), image) ? 1 : 0);
                        skuImagesEntity.setSkuId(skuId);
                        skuImagesDao.insert(skuImagesEntity);
                    });
                }
                // 2.3 新增销售属性pms_sku_sale_attr_value
                List<SkuSaleAttrValueEntity> saleAttrs = skuInfoVO.getSaleAttrs();
                if (!CollectionUtils.isEmpty(saleAttrs)) {
                    saleAttrs.forEach(skuSaleAttrValueEntity -> {
                                skuSaleAttrValueEntity.setSkuId(skuId);
                                skuSaleAttrValueEntity.setAttrSort(0);
                                this.skuSaleAttrValueDao.insert(skuSaleAttrValueEntity);
                            }
                    );
                }
                // 通过feign调用sms(营销微服务)保存
                // 3.新增营销相关 先需要sku_id
                // 3.1 新增积分信息sms_sku_bounds
                // 3.2 新增满减信息sms_sku_full_reduction
                // 3.2 新增满多少件优惠信息sms_sku_ladder
                SaleVO saleVO = new SaleVO();
                BeanUtils.copyProperties(skuInfoVO, saleVO);
                saleVO.setSkuId(skuId);
                this.gmallSmsClient.saveSaleInfo(saleVO);
            });
        }


    }

}