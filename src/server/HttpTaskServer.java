package server;

import com.sun.net.httpserver.HttpServer;
import managers.Managers;
import managers.TaskManager;

import java.io.IOException;
import java.net.InetSocketAddress;

public class HttpTaskServer {
    private static final int SOCKET = 8080;

    private final HttpServer server;
    static final TaskManager taskManager = Managers.getDefault();


    public HttpTaskServer() {
        try {
            server = HttpServer.create(new InetSocketAddress(SOCKET), 0);
            server.createContext("/tests", new TasksHandler());
            server.createContext("/epics", new EpicsHandler());
            server.createContext("/subtasks", new SubTasksHandler());
            server.createContext("/history", new HistoryHandler());
            server.createContext("/prioritized", new PrioritizedHandler());
        } catch (IOException e) {
            throw new RuntimeException("IO не избежать! Сервер не стоит. Попробуй снова", e);
        }
    }

    public void start(){
        server.start();
    }

    public void stop(){
        server.stop(0);
    }
}
