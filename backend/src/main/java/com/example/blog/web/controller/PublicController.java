package com.example.blog.web.controller;

import com.example.blog.service.CategoryService;
import com.example.blog.service.PostService;
import com.example.blog.web.ApiResponse;
import com.example.blog.web.dto.CategoryResponse;
import com.example.blog.web.dto.PageResponse;
import com.example.blog.web.dto.PostDetailResponse;
import com.example.blog.web.dto.PostSummaryResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/public")
public class PublicController {

    private final PostService postService;
    private final CategoryService categoryService;

    public PublicController(PostService postService, CategoryService categoryService) {
        this.postService = postService;
        this.categoryService = categoryService;
    }

    @GetMapping("/posts")
    public ApiResponse<PageResponse<PostSummaryResponse>> posts(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String categorySlug) {
        return ApiResponse.ok(postService.listPublished(page, size, keyword, categorySlug));
    }

    @GetMapping("/posts/{slug}")
    public ApiResponse<PostDetailResponse> post(@PathVariable String slug) {
        return ApiResponse.ok(postService.getPublishedBySlug(slug));
    }

    @GetMapping("/categories")
    public ApiResponse<List<CategoryResponse>> categories() {
        return ApiResponse.ok(categoryService.listPublicCategories());
    }

    @GetMapping("/featured")
    public ApiResponse<List<PostSummaryResponse>> featured() {
        return ApiResponse.ok(postService.latestPublished());
    }
}
