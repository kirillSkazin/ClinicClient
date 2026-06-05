package org.example.clinic.client.network;


public class ApiException extends RuntimeException {

    private final String code;

    public ApiException(String code, String message) {
        super(message);
        this.code = code;
    }

    public ApiException(String code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public boolean isUnauthorized() {
        return "UNAUTHORIZED".equals(code);
    }

    public boolean isNetwork() {
        return "NETWORK_ERROR".equals(code);
    }
}
