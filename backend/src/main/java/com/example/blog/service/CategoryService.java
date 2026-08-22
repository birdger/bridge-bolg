package com.example.blog.service;

import com.example.blog.domain.Category;
import com.example.blog.repository.CategoryRepository;
import com.example.blog.repository.PostRepository;
import com.example.blog.web.BizException;
import com.example.blog.web.dto.CategoryRequest;
import com.example.blog.web.dto.CategoryResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final PostRepository postRepository;

    public CategoryService(CategoryRepository categoryRepository, PostRepository postRepository) {
        this.categoryRepository = categoryRepository;
        this.postRepository = postRepository;
    }

    public List<CategoryResponse> listPublicCategories() {
        return categoryRepository.findByEnabledTrueOrderByNameAsc().stream().map(this::toResponse).collect(Collectors.toList());
    }

    public List<CategoryResponse> listAllCategories() {
        return categoryRepository.findAll().stream().map(this::toResponse).collect(Collectors.toList());
    }

    public Category requireCategory(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new BizException(404, "分类不存在"));
    }

    @Transactional
    public CategoryResponse create(CategoryRequest request) {
        Category category = new Category();
        category.setName(request.getName());
        category.setSlug(SlugUtil.uniqueSlug(categoryRepository::existsBySlug, request.getName()));
        category.setDescription(request.getDescription());
        category.setEnabled(request.getEnabled() == null || request.getEnabled());
        return toResponse(categoryRepository.save(category));
    }

    @Transactional
    public CategoryResponse update(Long id, CategoryRequest request) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new BizException(404, "分类不存在"));
        category.setName(request.getName());
        category.setDescription(request.getDescription());
        category.setEnabled(request.getEnabled() == null || request.getEnabled());
        return toResponse(categoryRepository.save(category));
    }

    @Transactional
    public void delete(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new BizException(404, "分类不存在"));
        boolean hasPost = postRepository.findAll().stream()
                .anyMatch(post -> post.getCategory() != null && post.getCategory().getId().equals(id));
        if (hasPost) {
            throw new BizException(400, "该分类下还有文章，不能删除");
        }
        categoryRepository.delete(category);
    }

    public CategoryResponse toResponse(Category category) {
        CategoryResponse response = new CategoryResponse();
        response.setId(category.getId());
        response.setName(category.getName());
        response.setSlug(category.getSlug());
        response.setDescription(category.getDescription());
        response.setEnabled(category.isEnabled());
        response.setPostCount(postRepository.findAll().stream()
                .filter(post -> post.getCategory() != null && post.getCategory().getId().equals(category.getId()))
                .count());
        return response;
    }
}
