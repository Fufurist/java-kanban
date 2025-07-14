package server;

import com.sun.net.httpserver.HttpServer;
import managers.Managers;
import managers.TaskManager;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class HttpTaskServer {
    private final int socket;

    private final HttpServer server;
    private final TaskManager taskManager = Managers.getDefault();
    static final Charset SERVER_DEFAULT_CHARSET = StandardCharsets.UTF_8;


    public HttpTaskServer(int socket) {
        try {
            this.socket = socket;
            server = HttpServer.create(new InetSocketAddress(this.socket), 0);
            //Почему-то меня переклинило на то, что хендлеры создаются уже внутри контекста.
            server.createContext("/tests", new TasksHandler(taskManager));
            server.createContext("/epics", new EpicsHandler(taskManager));
            server.createContext("/subtasks", new SubTasksHandler(taskManager));
            server.createContext("/history", new HistoryHandler(taskManager));
            server.createContext("/prioritized", new PrioritizedHandler(taskManager));
        } catch (IOException e) {
            throw new RuntimeException("IO не избежать! Сервер не стоит. Попробуй снова", e);
        }
    }

    public void start() {
        server.start();
    }

    public void stop() {
        server.stop(0);
    }
}
