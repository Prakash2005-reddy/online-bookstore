package com.chapterco.bookstore.repository;

import com.chapterco.bookstore.model.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    @Query("select ci from CartItem ci join fetch ci.book b join fetch b.category where ci.user.id = :userId order by ci.id")
    List<CartItem> findCartByUserId(@Param("userId") Long userId);

    Optional<CartItem> findByUserIdAndBookId(Long userId, Long bookId);
    void deleteByUserIdAndBookId(Long userId, Long bookId);
    boolean existsByBookId(Long bookId);

    @Modifying
    @Query("delete from CartItem ci where ci.user.id = :userId")
    void deleteAllByUserId(@Param("userId") Long userId);
}
