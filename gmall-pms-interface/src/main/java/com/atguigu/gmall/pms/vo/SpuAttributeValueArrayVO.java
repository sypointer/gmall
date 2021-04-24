package com.atguigu.gmall.pms.vo;

import lombok.Data;

/**
 * 该类中的values应为多值列，故定义为数组，视频中讲解错误
 */
@Data
public class SpuAttributeValueArrayVO {
    private Long productAttributeId; //当前sku对应的属性的attr_id
    private String name;//属性名  电池
    private String[] value;//3G   3000mah 4000 5000
}
