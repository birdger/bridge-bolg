package com.example.blog.bootstrap;

import com.example.blog.domain.Category;
import com.example.blog.domain.Post;
import com.example.blog.domain.PostStatus;
import com.example.blog.repository.CategoryRepository;
import com.example.blog.repository.PostRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class DataInitializer implements CommandLineRunner {

    private final CategoryRepository categoryRepository;
    private final PostRepository postRepository;

    public DataInitializer(CategoryRepository categoryRepository, PostRepository postRepository) {
        this.categoryRepository = categoryRepository;
        this.postRepository = postRepository;
    }

    @Override
    public void run(String... args) {
        if (categoryRepository.count() > 0) {
            return;
        }

        Category life = createCategory("生活记录", "life", "日常和感悟");
        Category tech = createCategory("技术笔记", "tech", "Spring Boot、前端和开发记录");
        Category project = createCategory("项目拆解", "project", "项目实践与复盘");

        createPost("Spring Boot 项目从零开始", "spring-boot-start", "介绍一个最小可用的博客系统如何搭起来。", "这里放一篇示例文章内容。", tech);
        createPost("我为什么记录自己的项目", "my-project-log", "个人博客是最好用的作品集。", "在这里写你的个人经历、学习笔记和项目总结。", life);
        createPost("前后端联调的三个关键点", "frontend-backend-connect", "接口、数据结构和错误处理是核心。", "这是一个示例内容，后续可以替换成你的真实文章。", project);
    }

    private Category createCategory(String name, String slug, String description) {
        Category category = new Category();
        category.setName(name);
        category.setSlug(slug);
        category.setDescription(description);
        category.setEnabled(true);
        return categoryRepository.save(category);
    }

    private void createPost(String title, String slug, String summary, String content, Category category) {
        Post post = new Post();
        post.setTitle(title);
        post.setSlug(slug);
        post.setSummary(summary);
        post.setContent(content);
        post.setCategory(category);
        post.setStatus(PostStatus.PUBLISHED);
        post.setPublishedAt(LocalDateTime.now().minusDays(1));
        postRepository.save(post);
    }
}
