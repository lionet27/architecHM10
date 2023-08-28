package org.example.cacheaside;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import java.util.concurrent.TimeUnit;

import org.example.repository.Order;

public class OrderCacheCaffeine {

    private final Cache<Integer, Order> cache;

    public OrderCacheCaffeine() {
        cache = Caffeine.newBuilder()
                .expireAfterWrite(100, TimeUnit.MINUTES)
                .maximumSize(1000)
                .build();
    }

    public Order getOrder(int id) {
        Order order = cache.getIfPresent(id);
        if (order == null) {
            order = loadOrderFromDatabase(id);
            cache.put(id, order);
        }
        return order;
    }

    public void invalidateOrder(int id) {
        cache.invalidate(id);
    }

    public void updateOrder(int id, Order newOrder) {
        // обновление заказа в базе данных
        invalidateOrder(id);
    }

    private Order loadOrderFromDatabase(int id) {
        // загрузка заказа из базы данных
    }

}
