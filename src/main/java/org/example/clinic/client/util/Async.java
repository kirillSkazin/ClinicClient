package org.example.clinic.client.util;

import javafx.application.Platform;
import javafx.concurrent.Task;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.function.Supplier;


public final class Async {

    private static final ExecutorService EXECUTOR = Executors.newCachedThreadPool(r -> {
        Thread t = new Thread(r, "client-async");
        t.setDaemon(true);
        return t;
    });

    private Async() {
    }

    public static <T> void run(Supplier<T> work, Consumer<T> onSuccess, Consumer<Throwable> onError) {
        Task<T> task = new Task<>() {
            @Override
            protected T call() {
                return work.get();
            }
        };
        task.setOnSucceeded(e -> Platform.runLater(() -> onSuccess.accept(task.getValue())));
        task.setOnFailed(e -> Platform.runLater(() -> onError.accept(task.getException())));
        EXECUTOR.submit(task);
    }

    public static void runVoid(Runnable work, Runnable onSuccess, Consumer<Throwable> onError) {
        run(() -> { work.run(); return null; }, v -> onSuccess.run(), onError);
    }

    public static void shutdown() {
        EXECUTOR.shutdownNow();
    }
}
