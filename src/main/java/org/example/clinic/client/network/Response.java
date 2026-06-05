package org.example.clinic.client.network;

import com.fasterxml.jackson.databind.JsonNode;

import java.io.Serial;
import java.io.Serializable;

public class Response implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String id;
    private boolean success;
    private JsonNode data;
    private String error;
    private String errorCode;

    public Response() {
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public JsonNode getData() { return data; }
    public void setData(JsonNode data) { this.data = data; }

    public String getError() { return error; }
    public void setError(String error) { this.error = error; }

    public String getErrorCode() { return errorCode; }
    public void setErrorCode(String errorCode) { this.errorCode = errorCode; }
}
