package com.campus.parking.pricing;

import com.campus.parking.domain.VehicleType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.Map;

@Configuration
public class PricingConfiguration {
    @Bean
    Map<VehicleType, PricingRule> pricingRules(
            @Value("${parking.ebike-flat-fee:15}") BigDecimal ebikeFlatFee) {
        Map<VehicleType, PricingRule> rules = new EnumMap<>(VehicleType.class);
        rules.put(VehicleType.TWO_WHEELER, new HourlyPricingRule(BigDecimal.TEN));
        rules.put(VehicleType.CAR, new HourlyPricingRule(BigDecimal.valueOf(30)));
        rules.put(VehicleType.ELECTRIC_CAR, new HourlyPricingRule(BigDecimal.valueOf(30)));
        rules.put(VehicleType.E_BIKE, (entry, exit) -> ebikeFlatFee);
        rules.put(VehicleType.DELIVERY_VAN, new DeliveryVanPricingRule(BigDecimal.valueOf(30)));
        return rules;
    }
}