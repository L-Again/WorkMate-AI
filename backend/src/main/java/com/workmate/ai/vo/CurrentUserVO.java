package com.workmate.ai.vo;

public class CurrentUserVO {

    private Long id;

    private String username;

    private String displayName;

    private String role;

    private Integer status;

    public CurrentUserVO(Long id, String username, String displayName, String role, Integer status) {
        this.id = id;
        this.username = username;
        this.displayName = displayName;
        this.role = role;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getRole() {
        return role;
    }

    public Integer getStatus() {
        return status;
    }
}