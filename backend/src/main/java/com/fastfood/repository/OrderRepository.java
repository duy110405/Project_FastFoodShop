package com.fastfood.repository;

import com.fastfood.entity.transaction.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, String> {

    @Query("SELECT MAX(o.idOrder) FROM Order o")
    String findMaxIdOrder();

    // Tìm các đơn hàng chưa phục vụ xong để hiển thị trạng thái bàn trên UI
    List<Order> findByStatus(String status);
    
    // Tìm đơn hàng đang hoạt động của một bàn cụ thể
    @Query("SELECT o FROM Order o WHERE o.tableNumber = ?1 AND o.status = 'PENDING'")
    Order findActiveOrderByTable(String tableNumber);

    @Query("SELECT DISTINCT o FROM Order o " +
            "LEFT JOIN FETCH o.orderDetails od " +
            "LEFT JOIN FETCH od.food " +
            "WHERE o.status = 'PENDING' " +
            "ORDER BY o.orderTime ASC")
    List<Order> findPendingOrdersWithDetails();

    @Query("""
            SELECT COUNT(o)
            FROM Order o
            WHERE o.orderTime >= :fromDateTime
              AND o.orderTime < :toDateTime
            """)
    long countOrdersInRange(@Param("fromDateTime") LocalDateTime fromDateTime,
                            @Param("toDateTime") LocalDateTime toDateTime);
}