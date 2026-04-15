package com.fastfood.exception;

import com.fastfood.dto.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice // Dùng cái này thay vì @ControllerAdvice để nó tự hiểu là trả về JSON
public class GlobalExceptionHandler {

    // 1. Bắt lỗi Logic chung
    @ExceptionHandler(value = RuntimeException.class)
    public ResponseEntity<ApiResponse<Object>> handlingRuntimeException(RuntimeException exception) {
        ApiResponse<Object> response = ApiResponse.builder()
                .code(400) // Mã lỗi Bad Request
                .message(exception.getMessage()) // (VD: "Không tìm thấy KH")
                .data(null)
                .build();
        return ResponseEntity.badRequest().body(response);
    }

    // 2. Bắt lỗi Validate Form (Ví dụ: Bỏ trống tên, SĐT sai định dạng)
    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Object>> handlingValidation(MethodArgumentNotValidException exception) {
        // Lấy lỗi đầu tiên một cách an toàn
        String errorMessage = "Dữ liệu không hợp lệ";
        if (exception.getFieldError() != null) {
            errorMessage = exception.getFieldError().getDefaultMessage();
        }

        ApiResponse<Object> response = ApiResponse.builder()
                .code(400)
                .message(errorMessage)
                .build();
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(value = Exception.class)
    public ResponseEntity<ApiResponse<Object>> handlingException(Exception exception) {


        //vanh
        exception.printStackTrace();

        ApiResponse<Object> response = ApiResponse.builder()
                .code(500) // Lỗi hệ thống thường để 500
                .message("Có lỗi hệ thống xảy ra, vui lòng liên hệ admin!")
                .build();
        return ResponseEntity.internalServerError().body(response);
    }

}
