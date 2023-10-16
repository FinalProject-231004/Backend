package com.starta.project.domain.notification.entity;

public enum NotificationType {

    COMMENT("/api/quiz/{id}", "comment"),
    NOTICE("/", "notice"),
    LIKEQUIZ("/api/quiz/{id}", "likemember");



    private final String path;
    private final String alias;

    NotificationType(String path, String alias) {
        this.path = path;
        this.alias = alias;
    }

    public String getPath() {
        return path;
    }

    public String getAlias() {
        return alias;
    }
}
