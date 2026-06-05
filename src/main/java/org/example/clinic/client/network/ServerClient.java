package org.example.clinic.client.network;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.clinic.client.session.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantLock;


public class ServerClient implements AutoCloseable {

    private static final Logger log = LoggerFactory.getLogger(ServerClient.class);
    private static final ObjectMapper JSON = JsonMapper.get();

    private final String host;
    private final int port;
    private final int connectTimeoutMs;
    private final int readTimeoutMs;

    private Socket socket;
    private BufferedReader in;
    private BufferedWriter out;
    private final ReentrantLock lock = new ReentrantLock();

    public ServerClient(String host, int port, int connectTimeoutMs, int readTimeoutMs) {
        this.host = host;
        this.port = port;
        this.connectTimeoutMs = connectTimeoutMs;
        this.readTimeoutMs = readTimeoutMs;
    }

    private void connectIfNeeded() throws IOException {
        if (socket != null && socket.isConnected() && !socket.isClosed()) {
            return;
        }
        closeQuietly();
        Socket s = new Socket();
        s.connect(new InetSocketAddress(host, port), connectTimeoutMs);
        s.setSoTimeout(readTimeoutMs);
        s.setKeepAlive(true);
        socket = s;
        in = new BufferedReader(new InputStreamReader(s.getInputStream(), StandardCharsets.UTF_8));
        out = new BufferedWriter(new OutputStreamWriter(s.getOutputStream(), StandardCharsets.UTF_8));
        log.info("Connected to server at {}:{}", host, port);
    }

    public <T> T call(String command, Object payload, Class<T> resultType) {
        JsonNode raw = callRaw(command, payload);
        if (raw == null || raw.isNull()) {
            return null;
        }
        try {
            return JSON.treeToValue(raw, resultType);
        } catch (Exception ex) {
            throw new ApiException("PARSE_ERROR",
                    "Не удалось распарсить ответ: " + ex.getMessage(), ex);
        }
    }

    public <T> T call(String command, Object payload, TypeReference<T> typeRef) {
        JsonNode raw = callRaw(command, payload);
        if (raw == null || raw.isNull()) {
            return null;
        }
        try {
            return JSON.readValue(JSON.treeAsTokens(raw), typeRef);
        } catch (Exception ex) {
            throw new ApiException("PARSE_ERROR",
                    "Не удалось распарсить ответ: " + ex.getMessage(), ex);
        }
    }

    public JsonNode callRaw(String command, Object payload) {
        Request req = new Request(UUID.randomUUID().toString(), command,
                Session.get().getToken(), payload);
        Response resp = sendWithRetry(req);
        if (!resp.isSuccess()) {
            String code = resp.getErrorCode() == null ? "UNKNOWN" : resp.getErrorCode();
            throw new ApiException(code, resp.getError() == null ? "Ошибка сервера" : resp.getError());
        }
        return resp.getData();
    }

    private Response sendWithRetry(Request request) {
        lock.lock();
        try {
            try {
                return sendOnce(request);
            } catch (IOException firstEx) {
                log.warn("Send failed ({}), reconnect and retry", firstEx.getMessage());
                closeQuietly();
                try {
                    return sendOnce(request);
                } catch (IOException retryEx) {
                    throw new ApiException("NETWORK_ERROR",
                            "Нет связи с сервером: " + retryEx.getMessage(), retryEx);
                }
            }
        } finally {
            lock.unlock();
        }
    }

    private Response sendOnce(Request request) throws IOException {
        connectIfNeeded();
        try {
            String json = JSON.writeValueAsString(request);
            out.write(json);
            out.write('\n');
            out.flush();
        } catch (SocketException se) {
            throw se;
        }
        String line = in.readLine();
        if (line == null) {
            throw new IOException("Соединение закрыто сервером");
        }
        return JSON.readValue(line, Response.class);
    }

    private void closeQuietly() {
        try { if (in != null) in.close(); } catch (IOException ignored) {   }
        try { if (out != null) out.close(); } catch (IOException ignored) {   }
        try { if (socket != null) socket.close(); } catch (IOException ignored) {   }
        in = null;
        out = null;
        socket = null;
    }

    @Override
    public void close() {
        lock.lock();
        try {
            closeQuietly();
        } finally {
            lock.unlock();
        }
    }
}
