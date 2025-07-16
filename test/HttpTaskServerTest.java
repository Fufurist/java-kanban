import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import managers.Managers;
import managers.TaskManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import server.GsonDateTimeCustomParse;
import server.HttpTaskServer;
import taskunits.Epic;
import taskunits.SubTask;
import taskunits.Task;
import taskunits.TaskStatus;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

public class HttpTaskServerTest {
    Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class, new GsonDateTimeCustomParse())
            .create();
    private final String urlBase = "http://localhost:8080";
    HttpClient client = HttpClient.newHttpClient();
    Charset charCharset = StandardCharsets.UTF_8;
    TaskManager taskManager;
    HttpTaskServer server;

    @BeforeEach
    public void ServerStartupSequence(){
        taskManager = Managers.getDefault();
        Task newTask = new Task("Помыть посуду",//id - 1
                "Прийти на кухню и вымыть все грязные тарелки",
                TaskStatus.IN_PROGRESS,
                LocalDateTime.of(2025,7,12,12,0),
                Duration.ofMinutes(30));
        taskManager.addTask(newTask);
        Epic newEpic = new Epic("Сделать домашку",// id - 2
                "По очереди выполнить домашнее задание по всем предметам");
        taskManager.addEpic(newEpic);
        SubTask newSubTask = new SubTask("Сделать ДЗ по Матану", // id - 3
                "Достать Тетрадь достать Демидовича сделать номера ...",
                TaskStatus.NEW,
                LocalDateTime.of(2025,7,12,12,0).plusMinutes(30),
                Duration.ofMinutes(30),
                2);
        taskManager.addSubTask(newSubTask);
        newTask = new Task("Подмести пол",//id - 4
                "Достать совок и метлу смести мусор в совок выкинуть мусор из совка",
                TaskStatus.NEW,
                LocalDateTime.of(2025,7,12,12,0).plusMinutes(60),
                Duration.ofMinutes(30));
        taskManager.addTask(newTask);
        newEpic = new Epic("Успеть всё",//id - 5
                "Осталось всего 22 часа пора поторопиться и завершить этот модуль!");
        taskManager.addEpic(newEpic);
        newSubTask = new SubTask("Сделать ДЗ по Диффурам",//id - 6
                "Достать Тетрадь достать Филлипова сделать номера ...",
                TaskStatus.NEW,
                LocalDateTime.of(2025,7,12,12,0).plusMinutes(90),
                Duration.ofMinutes(30),
                2);
        taskManager.addSubTask(newSubTask);
        newSubTask = new SubTask("Быстро выполнить пятый спринт",//id - 7
                "Поторопись! До конца света осталось всего ничего!",
                TaskStatus.NEW,
                LocalDateTime.of(2025,7,12,12,0).plusMinutes(120),
                Duration.ofMinutes(30),
                5);
        taskManager.addSubTask(newSubTask);

        server = new HttpTaskServer(8080, taskManager);
        server.start();
    }

    @AfterEach
    public void ServerShutdownSequence(){
        server.stop();
    }

    /*
    Я просидел с java.io.IOException: HTTP/1.1 header parser received no bytes часов 8 точно
    Вроде на стороне сервера всё как надо, пробовал даже какой-нибудь заголовок добавить, со стороны клиента всё как
    в примере в ТЗ. Скорее всего я всё-таки что-то не так в хендлере делаю, или где-нибудь указать версию надо, в
    которых я не разбираюсь. Я слишком затянул это уже. Вчера тяжелый день был. Сегодня весь день на этой ошибке
    просидел никуда не продвинулся. Надо разобраться. Но самому уже разобраться не получается.
    Если добавить sendheaders и close в начало, то оно обработается. При этом хэндлер вроде не выбрасывает моих
    исключений, которые должен был бы кидать, если б попал в ветку где не вызываются сендеры. Причем Если не вернуть
    ответ сразу, любые sout-ы выводят значения дважды(


    @Test
    public void tasksGetTest() throws IOException, InterruptedException {
        List<Task> tasks = taskManager.getTasks();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(urlBase + "/tasks"))
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString(charCharset));
        System.out.println(response.body());
        Assertions.assertEquals(200, response.statusCode());
        Assertions.assertEquals(gson.toJson(tasks), response.body());
    }
    /**/
}
