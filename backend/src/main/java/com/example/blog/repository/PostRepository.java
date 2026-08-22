package com.example.blog.repository;

import com.example.blog.domain.Post;
import com.example.blog.domain.PostStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, Long>, JpaSpecificationExecutor<Post> {
    Optional<Post> findBySlug(String slug);
    List<Post> findTop5ByStatusOrderByPublishedAtDescCreatedAtDesc(PostStatus status);
}
