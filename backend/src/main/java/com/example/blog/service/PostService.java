package com.example.blog.service;

import com.example.blog.domain.Category;
import com.example.blog.domain.Post;
import com.example.blog.domain.PostStatus;
import com.example.blog.repository.PostRepository;
import com.example.blog.web.BizException;
import com.example.blog.web.dto.PageResponse;
import com.example.blog.web.dto.PostDetailResponse;
import com.example.blog.web.dto.PostRequest;
import com.example.blog.web.dto.PostSummaryResponse;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Join;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PostService {

    private final PostRepository postRepository;
    private final CategoryService categoryService;

    public PostService(PostRepository postRepository, CategoryService categoryService) {
        this.postRepository = postRepository;
        this.categoryService = categoryService;
    }

    public PageResponse<PostSummaryResponse> listPublished(int page, int size, String keyword, String categorySlug) {
        Pageable pageable = PageRequest.of(Math.max(page - 1, 0), Math.max(size, 1), Sort.by(Sort.Direction.DESC, "publishedAt", "id"));
        Page<Post> result = postRepository.findAll(buildPublicSpec(keyword, categorySlug), pageable);
        return toPage(result, page, size);
    }

    public PageResponse<PostSummaryResponse> listAdminPosts(int page, int size, String keyword) {
        Pageable pageable = PageRequest.of(Math.max(page - 1, 0), Math.max(size, 1), Sort.by(Sort.Direction.DESC, "updatedAt", "id"));
        Specification<Post> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (keyword != null && !keyword.trim().isEmpty()) {
                String like = "%" + keyword.trim().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("title")), like),
                        cb.like(cb.lower(root.get("summary")), like)
                ));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        Page<Post> result = postRepository.findAll(spec, pageable);
        return toPage(result, page, size);
    }

    public PostDetailResponse getPublishedBySlug(String slug) {
        Post post = postRepository.findBySlug(slug)
                .filter(item -> item.getStatus() == PostStatus.PUBLISHED)
                .orElseThrow(() -> new BizException(404, "文章不存在"));
        return toDetail(post);
    }

    public PostDetailResponse getById(Long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new BizException(404, "文章不存在"));
        return toDetail(post);
    }

    public List<PostSummaryResponse> latestPublished() {
        return postRepository.findTop5ByStatusOrderByPublishedAtDescCreatedAtDesc(PostStatus.PUBLISHED).stream()
                .map(this::toSummary)
                .collect(Collectors.toList());
    }

    @Transactional
    public PostDetailResponse create(PostRequest request) {
        Post post = new Post();
        fill(post, request);
        if (post.getStatus() == PostStatus.PUBLISHED) {
            post.setPublishedAt(LocalDateTime.now());
        }
        return toDetail(postRepository.save(post));
    }

    @Transactional
    public PostDetailResponse update(Long id, PostRequest request) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new BizException(404, "文章不存在"));
        fill(post, request);
        if (post.getStatus() == PostStatus.PUBLISHED && post.getPublishedAt() == null) {
            post.setPublishedAt(LocalDateTime.now());
        }
        return toDetail(postRepository.save(post));
    }

    @Transactional
    public void delete(Long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new BizException(404, "文章不存在"));
        postRepository.delete(post);
    }

    private void fill(Post post, PostRequest request) {
        post.setTitle(request.getTitle());
        post.setSlug(SlugUtil.uniqueSlug(slug -> postRepository.findBySlug(slug).isPresent(), request.getTitle()));
        post.setSummary(request.getSummary());
        post.setContent(request.getContent());
        post.setCoverImageUrl(request.getCoverImageUrl());
        post.setStatus(request.getStatus());
        Category category = categoryService.requireCategory(request.getCategoryId());
        post.setCategory(category);
        if (post.getStatus() != PostStatus.PUBLISHED) {
            post.setPublishedAt(null);
        }
    }

    private Specification<Post> buildPublicSpec(String keyword, String categorySlug) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("status"), PostStatus.PUBLISHED));
            if (keyword != null && !keyword.trim().isEmpty()) {
                String like = "%" + keyword.trim().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("title")), like),
                        cb.like(cb.lower(root.get("summary")), like),
                        cb.like(cb.lower(root.get("content")), like)
                ));
            }
            if (categorySlug != null && !categorySlug.trim().isEmpty()) {
                Join<Post, Category> categoryJoin = root.join("category");
                predicates.add(cb.equal(categoryJoin.get("slug"), categorySlug));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private PageResponse<PostSummaryResponse> toPage(Page<Post> pageData, int page, int size) {
        return new PageResponse<>(pageData.getContent().stream().map(this::toSummary).collect(Collectors.toList()), page, size, pageData.getTotalElements(), pageData.getTotalPages());
    }

    private PostSummaryResponse toSummary(Post post) {
        PostSummaryResponse response = new PostSummaryResponse();
        response.setId(post.getId());
        response.setTitle(post.getTitle());
        response.setSlug(post.getSlug());
        response.setSummary(post.getSummary());
        response.setCoverImageUrl(post.getCoverImageUrl());
        response.setStatus(post.getStatus().name());
        response.setPublishedAt(post.getPublishedAt());
        response.setCreatedAt(post.getCreatedAt());
        if (post.getCategory() != null) {
            response.setCategoryName(post.getCategory().getName());
            response.setCategorySlug(post.getCategory().getSlug());
        }
        return response;
    }

    private PostDetailResponse toDetail(Post post) {
        PostDetailResponse response = new PostDetailResponse();
        response.setId(post.getId());
        response.setTitle(post.getTitle());
        response.setSlug(post.getSlug());
        response.setSummary(post.getSummary());
        response.setContent(post.getContent());
        response.setCoverImageUrl(post.getCoverImageUrl());
        response.setStatus(post.getStatus().name());
        response.setPublishedAt(post.getPublishedAt());
        response.setCreatedAt(post.getCreatedAt());
        if (post.getCategory() != null) {
            response.setCategoryName(post.getCategory().getName());
            response.setCategorySlug(post.getCategory().getSlug());
        }
        return response;
    }
}

