package edu.qnu.helpdesk.exception;

/*
    Ném ra khi dữ liệu đầu vào không hợp lệ ở tầng nghiệp vụ
*/
public class BadRequestException extends RuntimeException {

    public BadRequestException(String message) {
        super(message);
    }
}
