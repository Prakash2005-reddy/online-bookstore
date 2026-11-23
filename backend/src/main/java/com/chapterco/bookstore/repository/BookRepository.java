package com.chapterco.bookstore.repository;

import com.chapterco.bookstore.model.Book;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface BookRepository extends JpaRepository<Book, Long> {
    @Query("""
        select b from Book b join fetch b.category c
        where (:query is null or lower(b.title) like lower(concat('%', :query, '%'))
           or lower(b.author) like lower(concat('%', :query, '%'))
           or lower(c.name) like lower(concat('%', :query, '%')))
          and (:categoryId is null or c.id = :categoryId)
        order by b.id desc
        """)
    List<Book> search(@Param("query") String query, @Param("categoryId") Long categoryId);

    @Query("select b from Book b join fetch b.category where b.id = :id")
    Optional<Book> findWithCategoryById(@Param("id") Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select b from Book b where b.id = :id")
    Optional<Book> findForUpdateById(@Param("id") Long id);
}
