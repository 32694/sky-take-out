package com.sky.config;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "sky.shop")
@Data
public class ShopProperties {
    private String name;
    private String phone;
    private String address;
    private Double deliveryPrice;
}