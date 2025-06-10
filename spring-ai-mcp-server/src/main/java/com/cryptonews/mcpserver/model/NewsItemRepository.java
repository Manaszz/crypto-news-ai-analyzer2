package com.cryptonews.mcpserver.model;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NewsItemRepository extends JpaRepository<NewsItem, Long> {

    List<NewsItem> findTop10ByCryptocurrencyIgnoreCaseOrderByPublishedDateDesc(String cryptocurrency);

    List<NewsItem> findByCryptocurrencyIgnoreCaseAndPublishedDateBetween(String cryptocurrency, LocalDateTime start, LocalDateTime end);

    List<NewsItem> findByCryptocurrencyIgnoreCase(String cryptocurrency);

    @Query("SELECT n FROM NewsItem n WHERE n.cryptocurrency = :cryptocurrency AND n.sentimentScore.compoundScore >= 0.05 ORDER BY n.publishedDate DESC")
    List<NewsItem> findPositiveNewsByCryptocurrency(@Param("cryptocurrency") String cryptocurrency);

    @Query("SELECT n FROM NewsItem n WHERE n.cryptocurrency = :cryptocurrency AND n.sentimentScore.compoundScore <= -0.05 ORDER BY n.publishedDate DESC")
    List<NewsItem> findNegativeNewsByCryptocurrency(@Param("cryptocurrency") String cryptocurrency);

    @Query("SELECT n FROM NewsItem n WHERE n.cryptocurrency = :cryptocurrency AND (LOWER(n.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(n.description) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<NewsItem> searchByKeyword(@Param("cryptocurrency") String cryptocurrency, @Param("keyword") String keyword);
} 