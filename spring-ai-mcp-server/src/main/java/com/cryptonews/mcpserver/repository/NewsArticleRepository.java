package com.cryptonews.mcpserver.repository;

import com.cryptonews.mcpserver.domain.NewsArticle;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NewsArticleRepository extends JpaRepository<NewsArticle, Long> {
} 