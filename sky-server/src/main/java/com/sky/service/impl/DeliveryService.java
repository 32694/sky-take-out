package com.sky.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
@Slf4j
public class DeliveryService {

    @Value("${sky.shop.address}")
    private String shopAddress;

    @Value("${sky.baidu-map.ak}")
    private String baiduAk;

    private static final int MAX_DELIVERY_DISTANCE = 5000;

    // 创建支持text/javascript的RestTemplate
    private RestTemplate createRestTemplate() {
        RestTemplate restTemplate = new RestTemplate();

        // 获取原有的消息转换器
        List<HttpMessageConverter<?>> messageConverters = new ArrayList<>();

        // 添加支持text/javascript的转换器
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        converter.setSupportedMediaTypes(Arrays.asList(
                MediaType.APPLICATION_JSON,
                MediaType.TEXT_PLAIN,
                MediaType.parseMediaType("text/javascript;charset=utf-8")
        ));
        messageConverters.add(converter);

        // 添加其他必要的转换器
        messageConverters.add(new StringHttpMessageConverter());

        restTemplate.setMessageConverters(messageConverters);
        return restTemplate;
    }

    public boolean checkDeliveryRange(String userAddress) {
        log.info("开始配送范围校验");
        log.info("用户地址: {}", userAddress);
        log.info("商家地址: {}", shopAddress);

        try {
            // 1. 获取商家坐标
            Map<String, Double> shopLocation = getLocation(shopAddress);
            if (shopLocation == null) {
                log.error("无法获取商家坐标");
                return false;
            }

            // 2. 获取用户坐标
            Map<String, Double> userLocation = getLocation(userAddress);
            if (userLocation == null) {
                log.error("无法获取用户坐标: {}", userAddress);
                return false;
            }

            // 3. 计算距离
            double distance = calculateDistance(
                    shopLocation.get("lat"), shopLocation.get("lng"),
                    userLocation.get("lat"), userLocation.get("lng")
            );

            boolean withinRange = distance <= MAX_DELIVERY_DISTANCE;
            log.info("配送距离计算结果: {}米, 是否在范围内: {}", (int)distance, withinRange);

            return withinRange;

        } catch (Exception e) {
            log.error("配送距离校验异常: {}", e.getMessage(), e);
            return false;
        }
    }

    private Map<String, Double> getLocation(String address) {
        try {
            String url = "https://api.map.baidu.com/geocoding/v3/?address={address}&output=json&ak={ak}";
            log.info("调用百度地图API，地址: {}", address);

            RestTemplate restTemplate = createRestTemplate();
            Map<String, Object> response = restTemplate.getForObject(url, Map.class, address, baiduAk);

            log.info("百度地图API响应: {}", response);

            if (response != null) {
                Integer status = (Integer) response.get("status");
                if (status == 0) {
                    Map<String, Object> result = (Map<String, Object>) response.get("result");
                    Map<String, Object> location = (Map<String, Object>) result.get("location");

                    Map<String, Double> coord = new HashMap<>();
                    coord.put("lng", ((Number) location.get("lng")).doubleValue());
                    coord.put("lat", ((Number) location.get("lat")).doubleValue());

                    log.info("地址 '{}' 的坐标: 经度={}, 纬度={}", address, coord.get("lng"), coord.get("lat"));
                    return coord;
                } else {
                    log.error("百度地图API返回错误状态: {}, 消息: {}", status, response.get("message"));
                }
            }
        } catch (Exception e) {
            log.error("获取地址坐标失败: {}", address, e);
        }
        return null;
    }

    private double calculateDistance(double lat1, double lng1, double lat2, double lng2) {
        double earthRadius = 6371000;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLng / 2) * Math.sin(dLng / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return earthRadius * c;
    }
}