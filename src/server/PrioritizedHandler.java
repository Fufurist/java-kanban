package server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpExchange;
import managers.TaskManager;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;

public class PrioritizedHandler extends BaseHttpHandler {

    protected PrioritizedHandler(TaskManager taskManager) {
        super(taskManager);
    }

    @Override
    public void handle(HttpExchange exchange) {
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, new GsonDateTimeCustomParse())
                .registerTypeAdapter(Duration.class, new DurationTypeAdapter())
                .create();

        try {
            if (exchange.getRequestMethod().equals("GET")) {
                sendText(exchange, 200, gson.toJson(taskManager.getPrioritizedTasks())); //ArrayList<Task>
            } else throw new NoSuchEndpoint("Unknown method " + exchange.getRequestMethod());
        } catch (IOException e) {
            try {
                exchange.sendResponseHeaders(500, -1);
            } catch (IOException ex) {
                //если я даже ответ не могу послать обратно
                System.out.println("Ну это уже совсем свинство!");
            }
        }
    }
}
