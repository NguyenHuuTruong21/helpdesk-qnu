// PENDING, PROCESSING, RESOLVED

package edu.qnu.helpdesk.enums;

/*
    Trạng thái xử lý của một phản hồi
    - PENDING    : Chờ xử lý (mới gửi)
    - PROCESSING : Đang xử lý (admin đã tiếp nhận)
    - RESOLVED   : Đã trả lời (admin đã phản hồi xong)
 */

public enum FeedbackStatus {
    PENDING("Chờ xử lý", "warning"),
    PROCESSING("Đang xử lý", "info"),
    RESOLVED("Đã trả lời", "success");

    private final String label;
    private final String badgeClass; // class Bootstrap để hiển thị màu

    FeedbackStatus(String label, String badgeClass) {
        this.label = label;
        this.badgeClass = badgeClass;
    }

    public String getLabel() {
        return label;
    }

    public String getBadgeClass() {
        return badgeClass;
    }
}
