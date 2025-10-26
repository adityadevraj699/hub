package com.fashion.hub.Dto;

import com.fashion.hub.Model.Order;

import java.util.EnumMap;
import java.util.Map;

public class UserOrderStats {
    private Long userId;
    private Map<Order.Status, Long> statusCount = new EnumMap<>(Order.Status.class);

    public UserOrderStats(Long userId) {
        this.userId = userId;
        for (Order.Status s : Order.Status.values()) {
            statusCount.put(s, 0L); // initialize 0
        }
    }

    public void setStatusCount(Order.Status status, Long count) {
        statusCount.put(status, count);
    }

    public Long getUserId() {
        return userId;
    }

    public Map<Order.Status, Long> getStatusCount() {
        return statusCount;
    }
}
