import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import managers.Managers;
import managers.TaskManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import server.DurationTypeAdapter;
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
            .registerTypeAdapter(Duration.class, new DurationTypeAdapter())
            .create();
    private final String urlBase = "http://localhost:8080";
    HttpClient client = HttpClient.newHttpClient();
    Charset charCharset = StandardCharsets.UTF_8;
    TaskManager taskManager;
    HttpTaskServer server;

    @BeforeEach
    public void ServerStartupSequence() {
        taskManager = Managers.getDefault();
        Task newTask = new Task("Помыть посуду",//id - 1
                "Прийти на кухню и вымыть все грязные тарелки",
                TaskStatus.IN_PROGRESS,
                LocalDateTime.of(2025, 7, 12, 12, 0),
                Duration.ofMinutes(30));
        taskManager.addTask(newTask);
        Epic newEpic = new Epic("Сделать домашку",// id - 2
                "По очереди выполнить домашнее задание по всем предметам");
        taskManager.addEpic(newEpic);
        SubTask newSubTask = new SubTask("Сделать ДЗ по Матану", // id - 3
                "Достать Тетрадь достать Демидовича сделать номера ...",
                TaskStatus.NEW,
                LocalDateTime.of(2025, 7, 12, 12, 0).plusMinutes(30),
                Duration.ofMinutes(30),
                2);
        taskManager.addSubTask(newSubTask);
        newTask = new Task("Подмести пол",//id - 4
                "Достать совок и метлу смести мусор в совок выкинуть мусор из совка",
                TaskStatus.NEW,
                LocalDateTime.of(2025, 7, 12, 12, 0).plusMinutes(60),
                Duration.ofMinutes(30));
        taskManager.addTask(newTask);
        newEpic = new Epic("Успеть всё",//id - 5
                "Осталось всего 22 часа пора поторопиться и завершить этот модуль!");
        taskManager.addEpic(newEpic);
        newSubTask = new SubTask("Сделать ДЗ по Диффурам",//id - 6
                "Достать Тетрадь достать Филлипова сделать номера ...",
                TaskStatus.NEW,
                LocalDateTime.of(2025, 7, 12, 12, 0).plusMinutes(90),
                Duration.ofMinutes(30),
                2);
        taskManager.addSubTask(newSubTask);
        newSubTask = new SubTask("Быстро выполнить пятый спринт",//id - 7
                "Поторопись! До конца света осталось всего ничего!",
                TaskStatus.NEW,
                LocalDateTime.of(2025, 7, 12, 12, 0).plusMinutes(120),
                Duration.ofMinutes(30),
                5);
        taskManager.addSubTask(newSubTask);

        server = new HttpTaskServer(8080, taskManager);
        server.start();
    }

    @AfterEach
    public void ServerShutdownSequence() {
        server.stop();
    }

    @Test
    public void tasksGetTest() throws IOException, InterruptedException {
        List<Task> tasks = taskManager.getTasks();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(urlBase + "/tasks"))
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString(charCharset));
        Assertions.assertEquals(200, response.statusCode());
        Assertions.assertEquals(gson.toJson(tasks), response.body());
        taskManager.clearAll();
        tasks = taskManager.getTasks();
        response = client.send(request, HttpResponse.BodyHandlers.ofString(charCharset));
        Assertions.assertEquals(200, response.statusCode());
        Assertions.assertEquals(gson.toJson(tasks), response.body());
    }

    @Test
    public void taskIdGetTest() throws IOException, InterruptedException {
        Task task = taskManager.getTaskById(4);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(urlBase + "/tasks/4"))
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString(charCharset));
        Assertions.assertEquals(200, response.statusCode());
        Assertions.assertEquals(gson.toJson(task), response.body());
        request = HttpRequest.newBuilder()
                .uri(URI.create(urlBase + "/tasks/3"))
                .GET()
                .build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString(charCharset));
        Assertions.assertEquals(404, response.statusCode());
    }


    @Test
    public void taskPost() throws IOException, InterruptedException {
        Task task = new Task("T1", "SKT team!", TaskStatus.NEW,
                LocalDateTime.of(2025, 7, 17, 20, 20), Duration.ofMinutes(60));
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(urlBase + "/tasks"))
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(task)))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString(charCharset));
        Assertions.assertEquals(201, response.statusCode());
        int id = gson.fromJson(response.body(), int.class);
        // тот же самый проверка на overlap
        request = HttpRequest.newBuilder()
                .uri(URI.create(urlBase + "/tasks"))
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(task)))
                .build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString(charCharset));
        Assertions.assertEquals(406, response.statusCode());
        // тот же самый, но перезапись
        task.setId(id);
        request = HttpRequest.newBuilder()
                .uri(URI.create(urlBase + "/tasks"))
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(task)))
                .build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString(charCharset));
        Assertions.assertEquals(201, response.statusCode());
        Assertions.assertEquals(gson.toJson(id), response.body());
        // тот же самый, но пересечение в перезаписи
        task.setStartTime(LocalDateTime.of(2025, 7, 12, 12, 0));
        request = HttpRequest.newBuilder()
                .uri(URI.create(urlBase + "/tasks"))
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(task)))
                .build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString(charCharset));
        Assertions.assertEquals(406, response.statusCode());
    }


    @Test
    public void taskDelete() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(urlBase + "/tasks/4"))
                .DELETE()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString(charCharset));
        Assertions.assertEquals(200, response.statusCode());
        Assertions.assertEquals("\"result\":\"Deleted.\"", response.body());
        taskManager.clearAll();
        //пытаемся удалить уже удалённый
        response = client.send(request, HttpResponse.BodyHandlers.ofString(charCharset));
        Assertions.assertEquals(200, response.statusCode());
        Assertions.assertEquals("\"result\":\"No such element.\"", response.body());
    }

    @Test
    public void epicsGetTest() throws IOException, InterruptedException {
        List<Epic> epics = taskManager.getEpics();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(urlBase + "/epics"))
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString(charCharset));
        Assertions.assertEquals(200, response.statusCode());
        Assertions.assertEquals(gson.toJson(epics), response.body());
        taskManager.clearAll();
        epics = taskManager.getEpics();
        response = client.send(request, HttpResponse.BodyHandlers.ofString(charCharset));
        Assertions.assertEquals(200, response.statusCode());
        Assertions.assertEquals(gson.toJson(epics), response.body());
    }

    @Test
    public void epicsIdGetTest() throws IOException, InterruptedException {
        Task epic = taskManager.getEpicById(2);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(urlBase + "/epics/2"))
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString(charCharset));
        Assertions.assertEquals(200, response.statusCode());
        Assertions.assertEquals(gson.toJson(epic), response.body());
        request = HttpRequest.newBuilder()
                .uri(URI.create(urlBase + "/epics/1"))
                .GET()
                .build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString(charCharset));
        Assertions.assertEquals(404, response.statusCode());
    }


    @Test
    public void epicsPost() throws IOException, InterruptedException {
        Epic epic = new Epic("T1", "SKT team!");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(urlBase + "/epics"))
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(epic)))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString(charCharset));
        Assertions.assertEquals(201, response.statusCode());
        int id = gson.fromJson(response.body(), int.class);
        // тот же самый, но перезапись
        epic.setId(id);
        request = HttpRequest.newBuilder()
                .uri(URI.create(urlBase + "/epics"))
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(epic)))
                .build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString(charCharset));
        Assertions.assertEquals(201, response.statusCode());
        Assertions.assertEquals(gson.toJson(id), response.body());
    }


    @Test
    public void epicsDelete() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(urlBase + "/epics/2"))
                .DELETE()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString(charCharset));
        Assertions.assertEquals(200, response.statusCode());
        Assertions.assertEquals("\"result\":\"Deleted.\"", response.body());
        taskManager.clearAll();
        //пытаемся удалить уже удалённый
        response = client.send(request, HttpResponse.BodyHandlers.ofString(charCharset));
        Assertions.assertEquals(200, response.statusCode());
        Assertions.assertEquals("\"result\":\"No such element.\"", response.body());
        //Работа логики inMemoryTaskManager уже проверена, потому всё удалилось как надо, и не надо проверять подзадачи
    }

    @Test
    public void subTasksGetTest() throws IOException, InterruptedException {
        List<SubTask> subTasks = taskManager.getSubTasks();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(urlBase + "/subtasks"))
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString(charCharset));
        Assertions.assertEquals(200, response.statusCode());
        Assertions.assertEquals(gson.toJson(subTasks), response.body());
        taskManager.clearAll();
        subTasks = taskManager.getSubTasks();
        response = client.send(request, HttpResponse.BodyHandlers.ofString(charCharset));
        Assertions.assertEquals(200, response.statusCode());
        Assertions.assertEquals(gson.toJson(subTasks), response.body());
    }

    @Test
    public void subTaskIdGetTest() throws IOException, InterruptedException {
        SubTask subTask = taskManager.getSubTaskById(3);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(urlBase + "/subtasks/3"))
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString(charCharset));
        Assertions.assertEquals(200, response.statusCode());
        Assertions.assertEquals(gson.toJson(subTask), response.body());
        request = HttpRequest.newBuilder()
                .uri(URI.create(urlBase + "/subtasks/2"))
                .GET()
                .build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString(charCharset));
        Assertions.assertEquals(404, response.statusCode());
    }


    @Test
    public void subTaskPost() throws IOException, InterruptedException {
        SubTask subTask = new SubTask("T1", "SKT team!", TaskStatus.NEW,
                LocalDateTime.of(2025, 7, 17, 20, 20), Duration.ofMinutes(60),
                2);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(urlBase + "/subtasks"))
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(subTask)))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString(charCharset));
        Assertions.assertEquals(201, response.statusCode());
        int id = gson.fromJson(response.body(), int.class);
        // тот же самый проверка на overlap
        request = HttpRequest.newBuilder()
                .uri(URI.create(urlBase + "/subtasks"))
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(subTask)))
                .build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString(charCharset));
        Assertions.assertEquals(406, response.statusCode());
        // тот же самый, но перезапись
        subTask.setId(id);
        request = HttpRequest.newBuilder()
                .uri(URI.create(urlBase + "/subtasks/3"))
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(subTask)))
                .build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString(charCharset));
        Assertions.assertEquals(201, response.statusCode());
        Assertions.assertEquals(gson.toJson(id), response.body());
        // тот же самый, но пересечение в перезаписи
        subTask.setStartTime(LocalDateTime.of(2025, 7, 12, 12, 0));
        request = HttpRequest.newBuilder()
                .uri(URI.create(urlBase + "/subtasks/3"))
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(subTask)))
                .build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString(charCharset));
        Assertions.assertEquals(406, response.statusCode());
        // новый c несуществующим эпиком
        subTask = new SubTask("T1", "SKT team!", TaskStatus.NEW,
                LocalDateTime.of(2025, 8, 17, 22, 0), Duration.ofMinutes(60),
                3);
        request = HttpRequest.newBuilder()
                .uri(URI.create(urlBase + "/subtasks/4"))
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(subTask)))
                .build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString(charCharset));
        Assertions.assertEquals(406, response.statusCode());
        // такая же ошибка как при пересечении, потому что не назначено по ТЗ и отслеживаются одинаково: не вернулось id
    }


    @Test
    public void subTaskDelete() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(urlBase + "/subtasks/3"))
                .DELETE()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString(charCharset));
        Assertions.assertEquals(200, response.statusCode());
        Assertions.assertEquals("\"result\":\"Deleted.\"", response.body());
        taskManager.clearAll();
        //пытаемся удалить уже удалённый
        response = client.send(request, HttpResponse.BodyHandlers.ofString(charCharset));
        Assertions.assertEquals(200, response.statusCode());
        Assertions.assertEquals("\"result\":\"No such element.\"", response.body());
    }

    @Test
    public void historyTest() throws IOException, InterruptedException {
        taskManager.getTaskById(1);
        taskManager.getEpicById(2);
        taskManager.getSubTaskById(3);
        List<Task> history = taskManager.getHistory();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(urlBase + "/history"))
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString(charCharset));
        Assertions.assertEquals(200, response.statusCode());
        Assertions.assertEquals(gson.toJson(history), response.body());
        taskManager.clearAll();
    }

    @Test
    public void priorityTest() throws IOException, InterruptedException {
        List<Task> priority = taskManager.getPrioritizedTasks();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(urlBase + "/prioritized"))
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString(charCharset));
        Assertions.assertEquals(200, response.statusCode());
        Assertions.assertEquals(gson.toJson(priority), response.body());
        taskManager.clearAll();
    }
}
