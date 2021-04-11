package com.atguigu.gmall.pms.service.impl;

import com.atguigu.gmall.pms.dao.AttrAttrgroupRelationDao;
import com.atguigu.gmall.pms.entity.AttrAttrgroupRelationEntity;
import com.atguigu.gmall.pms.vo.AttrVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.core.bean.PageVo;
import com.atguigu.core.bean.Query;
import com.atguigu.core.bean.QueryCondition;

import com.atguigu.gmall.pms.dao.AttrDao;
import com.atguigu.gmall.pms.entity.AttrEntity;
import com.atguigu.gmall.pms.service.AttrService;
import org.springframework.transaction.annotation.Transactional;


@Service("attrService")
public class AttrServiceImpl extends ServiceImpl<AttrDao, AttrEntity> implements AttrService {

    @Autowired
    private AttrAttrgroupRelationDao relationDao;

    @Override
    public PageVo queryPage(QueryCondition params) {
        IPage<AttrEntity> page = this.page(
                new Query<AttrEntity>().getPage(params),
                new QueryWrapper<AttrEntity>()
        );

        return new PageVo(page);
    }

    @Override
    public PageVo queryAttrByCid(Integer type, Long cid, QueryCondition queryCondition) {

        QueryWrapper<AttrEntity> wrapper = new QueryWrapper<>();

        if (type != null) {
            wrapper.eq("attr_type", type);
        }
        if (cid != null) {
            wrapper.eq("catelog_id", cid);
        }

        IPage<AttrEntity> page = this.page(
                new Query<AttrEntity>().getPage(queryCondition),
                wrapper
        );

        return new PageVo(page);
    }

    @Transactional
    @Override
    public void saveGroupIdAndAttrId(AttrVO attrVO) {
        // 1.保存属性
        this.save(attrVO);
        // 2.保存至关联表
        AttrAttrgroupRelationEntity relationEntity = new AttrAttrgroupRelationEntity();
        relationEntity.setAttrSort(0);
        relationEntity.setAttrId(attrVO.getAttrId());
        relationEntity.setAttrGroupId(attrVO.getAttrGroupId());
        this.relationDao.insert(relationEntity);
    }

}