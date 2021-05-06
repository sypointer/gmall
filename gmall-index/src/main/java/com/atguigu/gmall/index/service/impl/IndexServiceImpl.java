package com.atguigu.gmall.index.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.atguigu.core.bean.Resp;
import com.atguigu.gmall.index.feign.GmallPmsClient;
import com.atguigu.gmall.index.service.IndexService;
import com.atguigu.gmall.pms.entity.CategoryEntity;
import com.atguigu.gmall.pms.vo.CategoryVO;
import org.apache.commons.lang.StringUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class IndexServiceImpl implements IndexService {

    @Autowired
    private GmallPmsClient gmallPmsClient;

    // redis用这个类，防止乱码，方便调试
    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private JedisPool jedisPool;

    @Autowired
    private RedissonClient redissonClient;

    private static final String KEY_PREFIX = "index:category";

    @Override
    public Resp<List<CategoryEntity>> queryOneCategories() {
        return this.gmallPmsClient.queryCategories(1, null);
    }

    @Override
    public List<CategoryVO> queryCategoriesByPid(Integer pid) {
        // 1.判断缓存中是否存在，如果存在，查询缓存
        String cache = this.redisTemplate.opsForValue().get(KEY_PREFIX + pid);
        if (StringUtils.isNotBlank(cache)) return JSON.parseArray(cache, CategoryVO.class);
        // 2.如果缓存中不存在，则查询数据库
        Resp<List<CategoryVO>> listResp = this.gmallPmsClient.queryCategoriesByPid(pid);
        List<CategoryVO> categoryVOS = listResp.getData();
        // 3.查询完成后，放入缓存
        //if (!CollectionUtils.isEmpty(categoryVOS)) 不要加这句，为null也缓存至redis中，防止缓存穿透
        this.redisTemplate.opsForValue().set(KEY_PREFIX + pid, JSONObject.toJSONString(categoryVOS));

        return categoryVOS;
    }

    public String testLock() {
        RLock lock = this.redissonClient.getLock("lock");
        lock.lock();
        // 获取到锁执行业务逻辑
        String numString = this.redisTemplate.opsForValue().get("num");
        if (StringUtils.isBlank(numString)) {
            return null;
        }
        int num = Integer.parseInt(numString);
        this.redisTemplate.opsForValue().set("num", String.valueOf(++num));

        lock.unlock();

        return "已经增加成功";
    }

    // 测试 redis实现分布式锁
    @Override
    public String testLock1() {// 所有请求，竞争锁
        String uuid = UUID.randomUUID().toString();
        Boolean lock = this.redisTemplate.opsForValue().setIfAbsent("lock", uuid, 10, TimeUnit.SECONDS);
        // 获取到锁执行业务逻辑
        if (lock) {
            String numString = this.redisTemplate.opsForValue().get("num");
            if (StringUtils.isBlank(numString)) {
                return null;
            }
            int num = Integer.parseInt(numString);
            this.redisTemplate.opsForValue().set("num", String.valueOf(++num));

            // 释放锁
            try (Jedis jedis = this.jedisPool.getResource()) {
                String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
                jedis.eval(script, Collections.singletonList("lock"), Collections.singletonList(uuid));
            }
            //用这个可能会报错，报错就用上面那种
//            this.redisTemplate.execute(new DefaultRedisScript<>(script), Arrays.asList("lock"), uuid);
//            if (StringUtils.equals(uuid, this.redisTemplate.opsForValue().get("lock"))){
//                this.redisTemplate.delete("lock");
//            }
        } else {
            // 没有获取到锁的请求进行重试
            try {
                TimeUnit.SECONDS.sleep(1);
                testLock();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return "已经增加成功";

    }
}
