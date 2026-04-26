package com.mbstu.campussafety.models;

public class OTPResponse {
    private boolean success;
    private String message;
    private String otpId;

    public OTPResponse() {}

    public OTPResponse(boolean success, String message, String otpId) {
        this.success = success;
        this.message = message;
        this.otpId = otpId;
    }

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getOtpId() { return otpId; }
    public void setOtpId(String otpId) { this.otpId = otpId; }
}
