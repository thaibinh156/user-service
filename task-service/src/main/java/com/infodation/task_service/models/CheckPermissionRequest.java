package com.infodation.task_service.models;

public class CheckPermissionRequest {
    String resourceId;
    String subjectId;
    String resourceType;
    String subjectType;
    String permission;

    public CheckPermissionRequest() {
    }

    public CheckPermissionRequest(String resourceId, String subjectId, String resourceType, String subjectType, String permission) {
        this.resourceId = resourceId;
        this.subjectId = subjectId;
        this.resourceType = resourceType;
        this.subjectType = subjectType;
        this.permission = permission;
    }

    public String getPermission() {
        return permission;
    }

    public String getResourceId() {
        return resourceId;
    }

    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
    }

    public String getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(String subjectId) {
        this.subjectId = subjectId;
    }

    public String getResourceType() {
        return resourceType;
    }

    public void setResourceType(String resourceType) {
        this.resourceType = resourceType;
    }

    public String getSubjectType() {
        return subjectType;
    }

    public void setSubjectType(String subjectType) {
        this.subjectType = subjectType;
    }

    public void setPermission(String permission) {
        this.permission = permission;
    }
}
