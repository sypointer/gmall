package com.atguigu.gmall.index.service;

import com.atguigu.core.bean.Resp;
import com.atguigu.gmall.pms.entity.CategoryEntity;
import com.atguigu.gmall.pms.vo.CategoryVO;

import java.util.List;

public interface IndexService {
    Resp<List<CategoryEntity>> queryOneCategories();

    List<CategoryVO> queryCategoriesByPid(Integer pid);

    String testLock();

    String testLock1();
}
