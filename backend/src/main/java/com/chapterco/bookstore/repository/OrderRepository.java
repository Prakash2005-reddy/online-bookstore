package com.chapterco.bookstore.repository;

import com.chapterco.bookstore.model.CustomerOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<CustomerOrder, Long> {
    @Query("select distinct o from CustomerOrder o left join fetch o.items where o.user.id = :userId order by o.createdAt desc")
    List<CustomerOrder> findAllForUser(@Param("userId") Long userId);

    @Query("select distinct o from CustomerOrder o join fetch o.user left join fetch o.items order by o.createdAt desc")
    List<CustomerOrder> findAllForAdmin();

    @Query("select o from CustomerOrder o left join fetch o.items where o.id = :id")
    Optional<CustomerOrder> findWithItemsById(@Param("id") Long id);
}
