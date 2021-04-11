package com.atguigu.gmall.pms.vo;

import com.atguigu.gmall.pms.entity.ProductAttrValueEntity;
import org.apache.commons.lang.StringUtils;

import java.util.List;

public class ProductAttrValueVO extends ProductAttrValueEntity {
    // 接手前端参数本质上是用set方法来接收，所以重写set方法即可
    public void setValueSelected(List<String> valueSelected) {
        this.setAttrValue(StringUtils.join(valueSelected, ","));
    }
}
