package com.example.blog.web.dto;

import com.example.blog.domain.PostStatus;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

public class PostRequest {

    @NotBlank(message = "不能为空")
    private String title;

    private String summary;

    @NotBlank(message = "不能为空")
    private String content;

    private String coverImageUrl;

    @NotNull(message = "不能为空")
    private Long categoryId;

    @NotNull(message = "不能为空")
    private PostStatus status;

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getCoverImageUrl() { return coverImageUrl; }
    public void setCoverImageUrl(String coverImageUrl) { this.coverImageUrl = coverImageUrl; }
    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }
    public PostStatus getStatus() { return status; }
    public void setStatus(PostStatus status) { this.status = status; }
}

