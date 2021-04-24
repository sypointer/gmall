package com.atguigu.gmall.pms.controller;

import java.util.Arrays;
import java.util.List;
import java.util.Map;


import com.atguigu.core.bean.PageVo;
import com.atguigu.core.bean.Query;
import com.atguigu.core.bean.QueryCondition;
import com.atguigu.core.bean.Resp;
import com.atguigu.gmall.pms.vo.SpuInfoVO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.atguigu.gmall.pms.entity.SpuInfoEntity;
import com.atguigu.gmall.pms.service.SpuInfoService;


/**
 * spu信息
 *
 * @author songyang
 * @email 68873846@qq.com
 * @date 2021-03-31 00:03:41
 */
@Api(tags = "spu信息 管理")
@RestController
@RequestMapping("pms/spuinfo")
public class SpuInfoController {
    @Autowired
    private SpuInfoService spuInfoService;

    /**
     * cha'xu 查询spu信息表
     *
     * @param queryCondition 接收前端传来的分页参数
     * @param catId          分类id
     * @return
     */
    @GetMapping
    public Resp<PageVo> querySpuInfoByCatId(QueryCondition queryCondition,
                                            @RequestParam(value = "catId", defaultValue = "0") Long catId) {

        PageVo pageVo = this.spuInfoService.querySpuInfoByCatId(queryCondition, catId);
        return Resp.ok(pageVo);
    }

    @ApiOperation("分页查询已发布spu商品信息")
    @PostMapping("{status}")
    public Resp<List<SpuInfoEntity>> querySpuInfoByStatus(@RequestBody QueryCondition condition,
                                                          @PathVariable("status") Integer status) {

        IPage<SpuInfoEntity> spuInfoEntityIPage = this.spuInfoService.page(
                new Query<SpuInfoEntity>().getPage(condition),
                new QueryWrapper<SpuInfoEntity>().eq("publish_status", status));
        return Resp.ok(spuInfoEntityIPage.getRecords());
    }

    /**
     * 列表
     */
    @ApiOperation("分页查询(排序)")
    @PostMapping("list")
    Resp<List<SpuInfoEntity>> querySpuPage(QueryCondition queryCondition) {
        PageVo page = spuInfoService.queryPage(queryCondition);
        return Resp.ok((List<SpuInfoEntity>) page.getList());
    }

    /**
     * 列表
     */
    @ApiOperation("分页查询(排序)")
    @GetMapping("/list")
    @PreAuthorize("hasAuthority('pms:spuinfo:list')")
    public Resp<PageVo> list(QueryCondition queryCondition) {
        PageVo page = spuInfoService.queryPage(queryCondition);
        return Resp.ok(page);
    }


    /**
     * 信息
     */
    @ApiOperation("详情查询")
    @GetMapping("/info/{id}")
    @PreAuthorize("hasAuthority('pms:spuinfo:info')")
    public Resp<SpuInfoEntity> info(@PathVariable("id") Long id) {
        SpuInfoEntity spuInfo = spuInfoService.getById(id);
        return Resp.ok(spuInfo);
    }

    /**
     * 保存
     */
    @ApiOperation("保存")
    @PostMapping("save")
    @PreAuthorize("hasAuthority('pms:spuinfo:save')")
    public Resp<Object> save(@RequestBody SpuInfoVO spuInfoVO) {
        this.spuInfoService.bigSaveProduct(spuInfoVO);
        return Resp.ok(null);
    }

    /**
     * 修改
     */
    @ApiOperation("修改")
    @PostMapping("/update")
    @PreAuthorize("hasAuthority('pms:spuinfo:update')")
    public Resp<Object> update(@RequestBody SpuInfoEntity spuInfo) {
        spuInfoService.updateById(spuInfo);

        return Resp.ok(null);
    }

    /**
     * 删除
     */
    @ApiOperation("删除")
    @PostMapping("/delete")
    @PreAuthorize("hasAuthority('pms:spuinfo:delete')")
    public Resp<Object> delete(@RequestBody Long[] ids) {
        spuInfoService.removeByIds(Arrays.asList(ids));

        return Resp.ok(null);
    }

}
