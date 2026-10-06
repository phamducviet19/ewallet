package com.ewallet.user.service;

import com.ewallet.user.entity.OtpPurpose;
import com.ewallet.user.entity.User;

public interface OtpService {

    String generateAndSendOtp(User user, OtpPurpose purpose);

    User verifyOtp(String email, String rawOtp, OtpPurpose purpose);
}
