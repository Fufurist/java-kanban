package server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpExchange;
import managers.TaskManager;
import taskunits.Task;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;

import static server.HttpTaskServer.SERVER_DEFAULT_CHARSET;

public class TasksHandler extends BaseHttpHandler {

    protected TasksHandler(TaskManager taskManager) {
        super(taskManager);
    }

    @Override
    public void handle(HttpExchange exchange) {
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, new GsonDateTimeCustomParse())
                .create();

        try {
            String[] path = exchange.getRequestURI().toString().split("/");
            int id;
            Task task;
            switch (exchange.getRequestMethod()) {
                case "GET":
                    switch (path.length) {
                        case 3:
                            try {
                                id = Integer.parseInt(path[2]);
                            } catch (NumberFormatException e) {
                                throw new NoSuchEndpoint("Id should be int", e);
                            }
                            task = taskManager.getTaskById(id);
                            if (task == null) sendNotFound(exchange);
                            else sendText(exchange, 200, gson.toJson(task));
                            break;
                        case 2:
                            //А, ну да, токены нужны ведь только при десериализации.
                            sendText(exchange, 200, gson.toJson(taskManager.getTasks()));
                            break;
                        default:
                            throw new NoSuchEndpoint("No valuable path for GET " + exchange.getRequestURI().toString());
                    }
                    break;
                case "POST":
                    try (InputStream iS = exchange.getRequestBody()) {
                        task = gson.fromJson(new String(iS.readAllBytes(), SERVER_DEFAULT_CHARSET), Task.class);
                        if (task.getId() <= 0) {
                            id = taskManager.addTask(task);
                            if (id == -1) sendHasOverlaps(exchange);
                            sendText(exchange, 201, gson.toJson(id));
                        } else {
                            boolean success = taskManager.updateTask(task);
                            if (!success) sendHasOverlaps(exchange);
                            sendText(exchange, 201, gson.toJson(task.getId()));
                        }
                    }
                    break;
                case "DELETE":
                    //В ТЗ есть табличка с эндпоинтами, в которой нет удаления всех задач/подзадач/эпиков
                    id = Integer.parseInt(path[2]);
                    if (taskManager.removeTask(id)) {
                        sendText(exchange, 200, "\"result\":\"Deleted.\"");
                    } else {
                        //Запрос к Delete не предполагает ответа 404, поэтому просто меняем тело при неудаче
                        sendText(exchange, 200, "\"result\":\"No such element.\"");
                    }
                    break;
                default:
                    throw new NoSuchEndpoint("Unknown method " + exchange.getRequestMethod());
            }
        } catch (IOException e) {
            try {
                exchange.sendResponseHeaders(500, -1);
            } catch (IOException ex) {
                System.out.println("Ну это уже совсем свинство!");
            }
        }
    }
}
