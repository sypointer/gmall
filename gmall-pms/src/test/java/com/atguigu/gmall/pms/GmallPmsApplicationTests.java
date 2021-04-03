package com.atguigu.gmall.pms;

import com.atguigu.gmall.pms.dao.BrandDao;
import com.atguigu.gmall.pms.entity.BrandEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class GmallPmsApplicationTests {

    @Autowired
    private BrandDao brandDao;

    @Test
    void contextLoads() {
    }

    @Test
    public void test() {
        BrandEntity brandEntity = new BrandEntity();
        brandEntity.setDescript("测试");
        brandEntity.setFirstLetter("");
        brandEntity.setLogo("www.jd.com/log.jpg");
        brandEntity.setShowStatus(1);
        brandEntity.setName("京东测试");
        brandEntity.setSort(1);

        brandDao.insert(brandEntity);

    }

}
