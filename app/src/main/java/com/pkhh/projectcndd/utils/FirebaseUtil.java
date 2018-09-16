package com.pkhh.projectcndd.utils;

import java.util.HashMap;
import java.util.Map;

import androidx.annotation.NonNull;

public final class FirebaseUtil {
    private static final Map<String, String> firebaseAuthExceptionMessage = new HashMap<>();

    static {
        firebaseAuthExceptionMessage.put("ERROR_INVALID_CUSTOM_TOKEN", "Định dạng token tùy chỉnh không chính xác. Vui lòng kiểm tra tài liệu.");
        firebaseAuthExceptionMessage.put("ERROR_CUSTOM_TOKEN_MISMATCH", "Token tùy chỉnh tương ứng với một đối tượng khác.");
        firebaseAuthExceptionMessage.put("ERROR_INVALID_CREDENTIAL", "Thông tin xác thực xác thực được cung cấp không đúng định dạng hoặc đã hết hạn.");
        firebaseAuthExceptionMessage.put("ERROR_INVALID_EMAIL", "Địa chỉ email bị định dạng sai.");
        firebaseAuthExceptionMessage.put("ERROR_WRONG_PASSWORD", "Mật khẩu không hợp lệ hoặc người dùng không có mật khẩu.");
        firebaseAuthExceptionMessage.put("ERROR_USER_MISMATCH", "Thông tin đăng nhập được cung cấp không tương ứng với người dùng đã đăng nhập trước đó.");
        firebaseAuthExceptionMessage.put("ERROR_REQUIRES_RECENT_LOGIN", "Thao tác này nhạy cảm và yêu cầu xác thực gần đây. Đăng nhập lại trước khi thử lại yêu cầu này");
        firebaseAuthExceptionMessage.put("ERROR_ACCOUNT_EXISTS_WITH_DIFFERENT_CREDENTIAL", "Tài khoản đã tồn tại với cùng địa chỉ email nhưng có thông tin đăng nhập khác nhau. Đăng nhập bằng nhà cung cấp được liên kết với địa chỉ email này.");
        firebaseAuthExceptionMessage.put("ERROR_EMAIL_ALREADY_IN_USE", "Địa chỉ email đã được một tài khoản khác sử dụng.");
        firebaseAuthExceptionMessage.put("ERROR_CREDENTIAL_ALREADY_IN_USE", "Thông tin xác thực này đã được liên kết với một tài khoản người dùng khác.");
        firebaseAuthExceptionMessage.put("ERROR_USER_DISABLED", "Tài khoản người dùng đã bị quản trị viên vô hiệu hóa.");
        firebaseAuthExceptionMessage.put("ERROR_USER_TOKEN_EXPIRED", "Thông tin đăng nhập của người dùng không còn hợp lệ. Người dùng phải đăng nhập lại.");
        firebaseAuthExceptionMessage.put("ERROR_USER_NOT_FOUND", "Không có người dùng nào tương ứng với số nhận dạng này. Người dùng có thể đã bị xóa.");
        firebaseAuthExceptionMessage.put("ERROR_INVALID_USER_TOKEN", "Thông tin đăng nhập của người dùng không còn hợp lệ. Người dùng phải đăng nhập lại.");
        firebaseAuthExceptionMessage.put("ERROR_OPERATION_NOT_ALLOWED", "Thao tác này không được phép. Bạn phải bật dịch vụ này trong bảng điều khiển.");
        firebaseAuthExceptionMessage.put("ERROR_WEAK_PASSWORD", "Mật khẩu đã cho không hợp lệ.");
    }

    @NonNull
    public static String getMessageFromFirebaseAuthExceptionErrorCode(String errorCode) {
        final String s = firebaseAuthExceptionMessage.get(errorCode);
        return s == null ? "Lỗi chưa xác định" : s;
    }
}
