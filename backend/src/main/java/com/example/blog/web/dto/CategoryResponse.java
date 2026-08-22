package com.example.blog.web.dto;

public class CategoryResponse {

    private Long id;
    private String name;
    private String slug;
    private String description;
    private boolean enabled;
    private long postCount;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getSlug() { return slug; }
    public void setSlug(String slug) { this.slug = slug; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public long getPostCount() { return postCount; }
    public void setPostCount(long postCount) { this.postCount = postCount; }
}
