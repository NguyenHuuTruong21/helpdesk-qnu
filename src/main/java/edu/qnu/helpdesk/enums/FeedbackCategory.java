package edu.qnu.helpdesk.enums;

/*
    Danh mục phản hồi - dựa trên phân loại thực tế của QNU
 */

public enum FeedbackCategory {

    EMAIL_ACCOUNT(
            "Tài khoản Email: Google, Office 365",
            "Quên tài khoản/mật khẩu, sự cố đăng nhập, không nhận được email...",
            "bi-envelope-at"),
    WIFI_ACCOUNT(
            "Tài khoản Internet Wifi",
            "Quên tài khoản/mật khẩu, sự cố đăng nhập, không kết nối được...",
            "bi-wifi"),
    QES_ELEARNING(
            "QES - Hỗ trợ hệ thống E-learning",
            "Gặp sự cố đăng nhập, thao tác trong khóa học, nộp bài...",
            "bi-mortarboard"),
    QNU_STUDIO(
            "Đăng ký bài giảng Video tại QNU Studio",
            "Yêu cầu ghi hình bài giảng, chỉnh sửa video...",
            "bi-camera-video"),
    IT_SERVICES(
            "Đăng ký tài khoản các dịch vụ CNTT",
            "Cấp mới tài khoản cho các hệ thống CNTT của trường",
            "bi-person-plus"),
    DOCUMENT_PURCHASE(
            "Đề xuất mua tài liệu",
            "Đề xuất mua sách, tài liệu phục vụ học tập và giảng dạy",
            "bi-book"),
    HOC_VU(
            "Học vụ - Đào tạo",
            "Vấn đề về điểm, chương trình học, thời khóa biểu...",
            "bi-journal-text"),
    KY_TUC_XA(
            "Ký túc xá",
            "Vấn đề về phòng ở, điện nước, an ninh...",
            "bi-building"),
    THU_VIEN(
            "Thư viện",
            "Mượn trả sách, gia hạn, tài khoản thư viện...",
            "bi-book-half"),
    OTP_2FA("Sự cố OTP / Xác thực 2 lớp", 
            "Mất điện thoại, hỏng thiết bị nhận mã OTP", 
            "bi-shield-exclamation"),
    ACCOUNT_HACKED("Tài khoản bị xâm nhập / khóa", 
            "Nghi ngờ tài khoản bị hack hoặc bị khóa", 
            "bi-person-x"),
    SYSTEM_ERROR("Lỗi cổng dịch vụ CNTT", 
            "Không truy cập được các hệ thống của trường", 
            "bi-exclamation-triangle"),        
    KHAC(
            "Vấn đề khác",
            "Các vấn đề chưa được liệt kê ở trên",
            "bi-three-dots");

    private final String label;
    private final String description;
    private final String icon;

    FeedbackCategory(String label, String description, String icon) {
        this.label = label;
        this.description = description;
        this.icon = icon;
    }

    public String getLabel() {
        return label;
    }

    public String getDescription() {
        return description;
    }

    public String getIcon() {
        return icon;
    }
}
