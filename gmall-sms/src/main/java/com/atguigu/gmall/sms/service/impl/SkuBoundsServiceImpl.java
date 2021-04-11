package com.atguigu.gmall.sms.service.impl;

import com.atguigu.gmall.sms.dao.SkuFullReductionDao;
import com.atguigu.gmall.sms.dao.SkuLadderDao;
import com.atguigu.gmall.sms.entity.SkuFullReductionEntity;
import com.atguigu.gmall.sms.entity.SkuLadderEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.core.bean.PageVo;
import com.atguigu.core.bean.Query;
import com.atguigu.core.bean.QueryCondition;

import com.atguigu.gmall.sms.dao.SkuBoundsDao;
import com.atguigu.gmall.sms.entity.SkuBoundsEntity;
import com.atguigu.gmall.sms.service.SkuBoundsService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import vo.SaleVO;


@Service("skuBoundsService")
public class SkuBoundsServiceImpl extends ServiceImpl<SkuBoundsDao, SkuBoundsEntity> implements SkuBoundsService {

    @Autowired
    private SkuFullReductionDao skuFullReductionDao;
    @Autowired
    private SkuLadderDao skuLadderDao;

    @Override
    public PageVo queryPage(QueryCondition params) {
        IPage<SkuBoundsEntity> page = this.page(
                new Query<SkuBoundsEntity>().getPage(params),
                new QueryWrapper<SkuBoundsEntity>()
        );

        return new PageVo(page);
    }

    @Transactional
    @Override
    public void saveSaleInfo(SaleVO saleVO) {
        // 3.新增营销相关 先需要sku_id
        // 3.1 新增积分信息sms_sku_bounds
        SkuBoundsEntity skuBoundsEntity = new SkuBoundsEntity();
        skuBoundsEntity.setBuyBounds(saleVO.getBuyBounds());
        skuBoundsEntity.setGrowBounds(saleVO.getGrowBounds());
        skuBoundsEntity.setSkuId(saleVO.getSkuId());
        List<Integer> works = saleVO.getWork();
        if (!CollectionUtils.isEmpty(works) && works.size() == 4) {
            int sum = 0;
            for (int i = works.size() - 1; i >= 0; --i) {
                sum += (works.get(i) * Math.pow(2, 3 - i));
            }
            skuBoundsEntity.setWork(sum);
        }
        this.save(skuBoundsEntity);
        // 3.2 新增满减信息sms_sku_full_reduction
        SkuFullReductionEntity skuFullReductionEntity = new SkuFullReductionEntity();
        skuFullReductionEntity.setSkuId(saleVO.getSkuId());
        skuFullReductionEntity.setFullPrice(saleVO.getFullPrice());
        skuFullReductionEntity.setReducePrice(saleVO.getReducePrice());
        skuFullReductionEntity.setAddOther(saleVO.getFullAddOther());
        this.skuFullReductionDao.insert(skuFullReductionEntity);
        // 3.2 新增满多少件优惠信息sms_sku_ladder
        SkuLadderEntity skuLadderEntity = new SkuLadderEntity();
        skuLadderEntity.setSkuId(saleVO.getSkuId());
        skuLadderEntity.setDiscount(saleVO.getDiscount());
        skuLadderEntity.setAddOther(saleVO.getLadderAddOther());
        skuLadderEntity.setFullCount(saleVO.getFullCount());
        this.skuLadderDao.insert(skuLadderEntity);
    }

}