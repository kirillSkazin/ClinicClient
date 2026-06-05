package org.example.clinic.client;

import javafx.application.Application;
import javafx.stage.Stage;
import org.example.clinic.client.config.ClientConfig;
import org.example.clinic.client.network.ServerClient;
import org.example.clinic.client.service.Services;
import org.example.clinic.client.ui.MainWindow;
import org.example.clinic.client.util.Async;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ClientApplication extends Application {

    private static final Logger log = LoggerFactory.getLogger(ClientApplication.class);

    private ServerClient serverClient;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void init() {
        ClientConfig.initialize(getParameters().getRaw().toArray(String[]::new));
    }

    @Override
    public void start(Stage stage) {
        ClientConfig cfg = ClientConfig.get();
        serverClient = new ServerClient(
                cfg.getString("server.host"),
                cfg.getInt("server.port", 8765),
                cfg.getInt("server.connect-timeout-ms", 5000),
                cfg.getInt("server.read-timeout-ms", 15000));
        Services services = new Services(serverClient);

        MainWindow window = new MainWindow(stage, services);
        window.showLogin();

        stage.show();
        log.info("Clinic client started, server={}:{}",
                cfg.getString("server.host"),
                cfg.getInt("server.port", 8765));
    }

    @Override
    public void stop() {
        Async.shutdown();
        if (serverClient != null) {
            serverClient.close();
        }
    }
}
