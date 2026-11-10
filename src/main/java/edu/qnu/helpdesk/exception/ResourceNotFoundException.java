package edu.qnu.helpdesk.exception;

/*
    Ném ra khi không tìm thấy tài nguyên (user, feedback, reply...)
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String resource, Long id) {
        super(resource + " không tồn tại với ID = " + id);
    }
}
