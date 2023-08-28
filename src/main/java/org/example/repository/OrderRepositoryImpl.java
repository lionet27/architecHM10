package org.example.repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class OrderRepositoryImpl implements OrderRepository {

    // Соединение с базой данных
    private Connection connection;

    /**
     * Конструктор, инициализирующий базу данных.
     *
     * @param databaseUrl URL базы данных для подключения
     */
    public void OrderRepository(String databaseUrl) throws SQLException {
        connection = DriverManager.getConnection(databaseUrl);
        createTable();
    }

    public OrderRepositoryImpl(String connectionString) {
        try {
            connection = DriverManager.getConnection(connectionString);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void createTable() {
        try {
            Statement statement = connection.createStatement();
            statement.execute(
                    "CREATE TABLE IF NOT EXISTS orders (id INTEGER PRIMARY KEY AUTOINCREMENT, totalAmount REAL)");
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Order getById(int id) throws SQLException {
        Order order = new Order();
        order.setItems(getOrderItemsByOrderId(id));
        return order;
    }

    @Override
    private List<OrderItem> getOrderItemsByOrdersId(int orderId) throws SQLException {
        List<OrderItem> orderItems = new ArrayList<>();

        PreparedStatement stmt = connection.prepareStatement("SELECT * FROM order_items WHERE orderId=?");
        stmt.setInt(1, orderId);
        ResultSet rs = stmt.executeQuery();
        while (rs.next()) {
            int productId = rs.getInt("productId");
            int quantity = rs.getInt("quantity");
            Product product = new Product(productId, "Product " + productId, 10.0);
            orderItems.add(new OrderItem(product, quantity));
        }
        rs.close();
        stmt.close();

        return orderItems;
    }

    @Override
    public void save(Order order) throws SQLException {
        connection.setAutoCommit(false);

        // Р’СЃС‚Р°РІРєР° РґР°РЅРЅС‹С… Рѕ Р·Р°РєР°Р·Рµ РІ С‚Р°Р±Р»РёС†Сѓ Р·Р°РєР°Р·РѕРІ
        PreparedStatement orderStmt = connection.prepareStatement("INSERT INTO orders(totalAmount) VALUES (?)");
        orderStmt.setDouble(1, order.getTotalPrice());
        orderStmt.executeUpdate();
        ResultSet generatedKeys = orderStmt.getGeneratedKeys();
        int orderId = generatedKeys.getInt(1);
        generatedKeys.close();
        orderStmt.close();

        // Р’СЃС‚Р°РІРєР° РґР°РЅРЅС‹С… Рѕ СЌР»РµРјРµРЅС‚Р°С… Р·Р°РєР°Р·Р° РІ
        // С‚Р°Р±Р»РёС†Сѓ СЌР»РµРјРµРЅС‚РѕРІ Р·Р°РєР°Р·Р°
        PreparedStatement itemStmt = connection
                .prepareStatement("INSERT INTO order_items(orderId, productId, quantity) VALUES (?, ?, ?)");
        for (OrderItem item : order.getItems()) {
            itemStmt.setInt(1, orderId);
            itemStmt.setInt(2, item.getProduct().getId());
            itemStmt.setInt(3, item.getQuantity());
            itemStmt.executeUpdate();
        }
        itemStmt.close();

        connection.commit();
        connection.setAutoCommit(true);
    }

}
