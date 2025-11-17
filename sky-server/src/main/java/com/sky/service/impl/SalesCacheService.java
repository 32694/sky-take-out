package com.sky.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class SalesCacheService {

    @Autowired
    private RedisTemplate redisTemplate;

    // 定义所有需要清理的销量相关缓存键
    private static final String SALES_DATA_KEY = "dish_sales"; // 销量数据缓存
    private static final String POPULAR_RECOMMEND_KEY = "recommend:popular:by_sales"; // 大众推荐缓存

    /**
     * 刷新所有销量相关缓存
     * 在订单创建、完成等操作后调用
     */
    public void refreshSalesCache() {
        try {
            // 清理销量数据缓存
            redisTemplate.delete(SALES_DATA_KEY);

            // 清理大众推荐缓存
            redisTemplate.delete(POPULAR_RECOMMEND_KEY);

            log.info("销量相关缓存已清除: 销量数据 + 大众推荐");
        } catch (Exception e) {
            log.error("清除销量缓存失败", e);
        }
    }

    /**
     * 仅清理销量数据缓存（如果需要单独清理）
     */
    public void refreshSalesDataOnly() {
        try {
            redisTemplate.delete(SALES_DATA_KEY);
            log.info("销量数据缓存已清除");
        } catch (Exception e) {
            log.error("清除销量数据缓存失败", e);
        }
    }

    /**
     * 仅清理大众推荐缓存（如果需要单独清理）
     */
    public void refreshPopularRecommendOnly() {
        try {
            redisTemplate.delete(POPULAR_RECOMMEND_KEY);
            log.info("大众推荐缓存已清除");
        } catch (Exception e) {
            log.error("清除大众推荐缓存失败", e);
        }
    }
}