package com.starta.project.domain.member.entity;

public enum UserRoleEnum {
    USER(Authority.USER),  // 유저 권한
    BLOCK(Authority.BLOCKED),  // 차단된 유저
    ADMIN(Authority.ADMIN);  // 관리자 권한

    private final String authority;

    UserRoleEnum(String authority) {
        this.authority = authority;
    }

    public String getAuthority() {
        return this.authority;
    }

    public static class Authority {
        public static final String USER = "ROLE_USER";
        public static final String BLOCKED = "ROLE_BLOCKED";
        public static final String ADMIN = "ROLE_ADMIN";
    }
}
