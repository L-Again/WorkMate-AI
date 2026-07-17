package com.workmate.ai.vo;

public class CategoryVO {

    private Long id;

    private String name;

    private String description;

    private Integer sortOrder;

    private Integer status;

    public CategoryVO(Long id, String name, String description, Integer sortOrder, Integer status) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.sortOrder = sortOrder;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public Integer getStatus() {
        return status;
    }
}