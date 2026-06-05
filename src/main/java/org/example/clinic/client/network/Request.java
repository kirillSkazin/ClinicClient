package org.example.clinic.client.network;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.io.Serial;
import java.io.Serializable;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Request implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String id;
    private String command;
    private String token;
    private Object payload;

    public Request() {
    }

    public Request(String id, String command, String token, Object payload) {
        this.id = id;
        this.command = command;
        this.token = token;
        this.payload = payload;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getCommand() { return command; }
    public void setCommand(String command) { this.command = command; }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public Object getPayload() { return payload; }
    public void setPayload(Object payload) { this.payload = payload; }
}
