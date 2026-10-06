package com.ewallet.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

    // --- Hệ thống & Chung (1000 - 1099) ---
    UNCATEGORIZED_EXCEPTION(1000, "Lỗi hệ thống không xác định", HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_REQUEST(1001, "Yêu cầu không hợp lệ", HttpStatus.BAD_REQUEST),
    UNAUTHORIZED(1002, "Yêu cầu xác thực để truy cập", HttpStatus.UNAUTHORIZED),
    FORBIDDEN(1003, "Bạn không có quyền thực hiện hành động này", HttpStatus.FORBIDDEN),
    RESOURCE_NOT_FOUND(1004, "Không tìm thấy tài nguyên yêu cầu", HttpStatus.NOT_FOUND),
    METHOD_NOT_ALLOWED(1005, "Phương thức HTTP không được hỗ trợ", HttpStatus.METHOD_NOT_ALLOWED),
    RESOURCE_CONFLICT(1006, "Xung đột tài nguyên dữ liệu", HttpStatus.CONFLICT),

    // --- Authentication & User (1100 - 1199) ---
    USER_NOT_FOUND(1100, "Không tìm thấy người dùng", HttpStatus.NOT_FOUND),
    EMAIL_ALREADY_EXISTS(1101, "Email này đã được sử dụng", HttpStatus.CONFLICT),
    PHONE_ALREADY_EXISTS(1102, "Số điện thoại này đã được sử dụng", HttpStatus.CONFLICT),
    INVALID_CREDENTIALS(1103, "Email hoặc mật khẩu không chính xác", HttpStatus.UNAUTHORIZED),
    USER_LOCKED(1104, "Tài khoản của bạn đã bị khóa. Vui lòng liên hệ Admin", HttpStatus.FORBIDDEN),
    ROLE_NOT_FOUND(1105, "Không tìm thấy vai trò chỉ định", HttpStatus.NOT_FOUND),
    TOKEN_INVALID(1106, "Token không hợp lệ hoặc đã bị chỉnh sửa", HttpStatus.UNAUTHORIZED),
    TOKEN_EXPIRED(1107, "Token đã hết hạn, vui lòng đăng nhập lại", HttpStatus.UNAUTHORIZED),
    REFRESH_TOKEN_EXPIRED(1108, "Refresh token đã hết hạn, vui lòng đăng nhập lại", HttpStatus.UNAUTHORIZED),
    OTP_INVALID(1109, "Mã OTP không chính xác", HttpStatus.BAD_REQUEST),
    OTP_EXPIRED(1110, "Mã OTP đã hết hạn", HttpStatus.BAD_REQUEST),
    OTP_MAX_ATTEMPTS_EXCEEDED(1111, "Đã vượt quá số lần nhập OTP cho phép", HttpStatus.BAD_REQUEST),

    // --- Wallet (1200 - 1299) ---
    WALLET_NOT_FOUND(1200, "Không tìm thấy ví của người dùng", HttpStatus.NOT_FOUND),
    WALLET_ALREADY_EXISTS(1201, "Người dùng đã có ví trong hệ thống", HttpStatus.CONFLICT),
    WALLET_NOT_ACTIVE(1202, "Ví không ở trạng thái hoạt động", HttpStatus.BAD_REQUEST),
    WALLET_FROZEN(1203, "Ví đã bị đóng băng, không thể thực hiện giao dịch", HttpStatus.BAD_REQUEST),
    WALLET_CLOSED(1204, "Ví đã bị đóng", HttpStatus.BAD_REQUEST),
    INSUFFICIENT_BALANCE(1205, "Số dư trong ví không đủ để thực hiện giao dịch", HttpStatus.BAD_REQUEST),
    INVALID_AMOUNT(1206, "Số tiền giao dịch phải lớn hơn 0", HttpStatus.BAD_REQUEST),
    INVALID_CURRENCY(1207, "Đơn vị tiền tệ không hợp lệ, hệ thống chỉ hỗ trợ VND", HttpStatus.BAD_REQUEST),

    // --- Transaction (1300 - 1399) ---
    TRANSACTION_NOT_FOUND(1300, "Không tìm thấy thông tin giao dịch", HttpStatus.NOT_FOUND),
    SELF_TRANSFER_NOT_ALLOWED(1301, "Không thể tự chuyển tiền cho chính mình", HttpStatus.BAD_REQUEST),
    TRANSACTION_FAILED(1302, "Thực hiện giao dịch thất bại", HttpStatus.BAD_REQUEST),
    CONCURRENT_TRANSACTION(1303, "Đang có giao dịch khác trên ví này. Vui lòng thử lại sau ít giây", HttpStatus.CONFLICT),

    // --- Idempotency (1400 - 1499) ---
    IDEMPOTENCY_KEY_MISSING(1400, "Thiếu header Idempotency-Key cho thao tác tài chính này", HttpStatus.BAD_REQUEST),
    IDEMPOTENCY_KEY_PROCESSING(1401, "Giao dịch với Idempotency-Key này đang được xử lý, vui lòng không gửi lại", HttpStatus.CONFLICT),
    IDEMPOTENCY_KEY_CONFLICT(1402, "Idempotency-Key này đã được dùng cho một payload giao dịch khác", HttpStatus.BAD_REQUEST);

    private final int code;
    private final String message;
    private final HttpStatus httpStatus;

    ErrorCode(int code, String message, HttpStatus httpStatus) {
        this.code = code;
        this.message = message;
        this.httpStatus = httpStatus;
    }
}
