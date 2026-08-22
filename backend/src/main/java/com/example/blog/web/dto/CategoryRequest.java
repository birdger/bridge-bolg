package com.example.blog.web.dto;

import javax.validation.constraints.NotBlank;

public class CategoryRequest {

    @NotBlank(message = "不能为空")
    private String name;

    private String description;

    private Boolean enabled = true;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Boolean getEnabled() { return enabled; }
    public void setEnabled(Boolean enabled) { this.enabled = enabled; }
}

