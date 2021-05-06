package com.atguigu.gmall.pms.vo;

import com.atguigu.gmall.pms.entity.CategoryEntity;
import lombok.Data;

import java.util.List;

@Data
public class CategoryVO extends CategoryEntity {
    // 坑:属性名字不要自己起，不然前端对应不上去，保持和视频一致
    List<CategoryEntity> subs;
}
