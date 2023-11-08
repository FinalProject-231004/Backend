package com.starta.project.domain.notification.entity;

public enum NotificationType {

    COMMENT("/quiz/{id}", "comment"),
    NOTICE("/", "notice"),
    LIKEQUIZ("/quiz/{id}", "likemember");



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
