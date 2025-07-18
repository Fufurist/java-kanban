package server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpExchange;
import managers.TaskManager;
import taskunits.Epic;

import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.time.LocalDateTime;

import static server.HttpTaskServer.SERVER_DEFAULT_CHARSET;

public class EpicsHandler extends BaseHttpHandler {

    public EpicsHandler(TaskManager taskManager) {
        super(taskManager);
    }

    @Override
    public void handle(HttpExchange exchange) {
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, new GsonDateTimeCustomParse())
                .registerTypeAdapter(Duration.class, new DurationTypeAdapter())
                .create();

        try {
            String[] path = exchange.getRequestURI().toString().split("/");
            int id;
            Epic epic;
            switch (exchange.getRequestMethod()) {
                case "GET":
                    switch (path.length) {
                        case 4:
                            if (!path[3].equals("subtasks"))
                                throw new NoSuchEndpoint("No such path /" + path[3] + " for /epics/{id}");
                            try {
                                id = Integer.parseInt(path[2]);
                            } catch (NumberFormatException e) {
                                throw new NoSuchEndpoint("Id should be int", e);
                            }
                            epic = taskManager.getEpicById(id);
                            if (epic == null) sendNotFound(exchange);
                                //Ну, я и вызываю getSubTasksIds, который возвращает список id
                            else sendText(exchange, 200, gson.toJson(epic.getSubTasksIds()));
                            break;
                        case 3:
                            try {
                                id = Integer.parseInt(path[2]);
                            } catch (NumberFormatException e) {
                                throw new NoSuchEndpoint("Id should be int", e);
                            }
                            epic = taskManager.getEpicById(id);
                            if (epic == null) sendNotFound(exchange);
                            else sendText(exchange, 200, gson.toJson(epic));
                            break;
                        case 2:
                            //А, ну да, токены нужны ведь только при десериализации.
                            sendText(exchange, 200, gson.toJson(taskManager.getEpics()));
                            break;
                        default:
                            throw new NoSuchEndpoint("No valuable path for GET " + exchange.getRequestURI().toString());
                    }
                    break;
                case "POST":
                    try (InputStream iS = exchange.getRequestBody()) {
                        epic = gson.fromJson(new String(iS.readAllBytes(), SERVER_DEFAULT_CHARSET), Epic.class);
                        if (epic.getId() <= 0) {
                            id = taskManager.addEpic(epic);
                            sendText(exchange, 201, gson.toJson(id));
                        } else {
                            taskManager.updateEpic(epic);
                            sendText(exchange, 201, gson.toJson(epic.getId()));
                        }
                    }
                    break;
                case "DELETE":
                    id = Integer.parseInt(path[2]);
                    if (taskManager.removeEpic(id)) {
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
                //если я даже ответ не могу послать обратно
                System.out.println("Ну это уже совсем свинство!");
            }
        }
    }
}
