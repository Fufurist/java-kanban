package server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpExchange;
import taskunits.Task;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;

import static server.HttpTaskServer.SERVER_DEFAULT_CHARSET;
import static server.HttpTaskServer.taskManager;

public class TasksHandler extends BaseHttpHandler {

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
                    // По ТЗ не указана возможность неправильного пути(А именно какую ошибку кидать в этом случае),
                    // поэтому действую из предположения, что на том конце сидит приложение, которое точно знает все
                    // эндпоинты и правильно посылает данные
                    // (То есть этот парс мне не даст ошибки при правильной работе фронтэнда)
                    id = Integer.parseInt(path[2]);
                    task = taskManager.getTaskById(id);
                    if (task == null) sendNotFound(exchange);
                    else sendText(exchange, gson.toJson(task));
                    break;
                case "POST":
                    try (InputStream iS = exchange.getRequestBody()){
                        task = gson.fromJson(new String(iS.readAllBytes(), SERVER_DEFAULT_CHARSET), Task.class);
                        if(task.getId() <= 0) {
                            id = taskManager.addTask(task);
                            if (id == -1) sendHasOverlaps(exchange);
                            sendText(exchange, null);
                        } else {
                            boolean success = taskManager.updateTask(task);
                            if (!success) sendHasOverlaps(exchange);
                            sendText(exchange, null);
                        }
                    }
                    break;
                case "DELETE":
                    //В ТЗ есть табличка с эндпоинтами, в которой нет удаления всех задач/подзадач/эпиков
                    id = Integer.parseInt(path[2]);
                    taskManager.removeTask(id);
                    break;
                default:
                    // В ТЗ не указано. Допустим, фронтэнд приложение отправляет только правильно
                    // сгенерированные экземпляры классов, и только по правильным адресам
                    break;
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
