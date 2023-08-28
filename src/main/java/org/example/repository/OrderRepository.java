package org.example.repository;

import java.util.List;

public interface OrderRepository {
    Order getById(int id);

    List<OrderItem> getOrderItemsByOrdersId(int orderId);

    void save(Order order);

    void createTable();
}