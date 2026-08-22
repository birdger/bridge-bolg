package com.example.blog.web.controller;

import com.example.blog.service.PostService;
import com.example.blog.web.ApiResponse;
import com.example.blog.web.dto.PageResponse;
import com.example.blog.web.dto.PostDetailResponse;
import com.example.blog.web.dto.PostRequest;
import com.example.blog.web.dto.PostSummaryResponse;
import javax.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/posts")
public class AdminPostController {

    private final PostService postService;

    public AdminPostController(PostService postService) {
        this.postService = postService;
    }

    @GetMapping
    public ApiResponse<PageResponse<PostSummaryResponse>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword) {
        return ApiResponse.ok(postService.listAdminPosts(page, size, keyword));
    }

    @GetMapping("/{id}")
    public ApiResponse<PostDetailResponse> get(@PathVariable Long id) {
        return ApiResponse.ok(postService.getById(id));
    }

    @PostMapping
    public ApiResponse<PostDetailResponse> create(@Valid @RequestBody PostRequest request) {
        return ApiResponse.ok(postService.create(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<PostDetailResponse> update(@PathVariable Long id, @Valid @RequestBody PostRequest request) {
        return ApiResponse.ok(postService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        postService.delete(id);
        return ApiResponse.ok(null);
    }
}

