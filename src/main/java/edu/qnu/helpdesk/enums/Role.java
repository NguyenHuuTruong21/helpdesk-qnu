// User, Admin

package edu.qnu.helpdesk.enums;

// Vai trò người dùng trong hệ thống

public enum Role {
    USER("Người dùng"),
    ADMIN("Quản trị viên");

    private final String label;

    Role(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
