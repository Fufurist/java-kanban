import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import managers.*;
import taskunits.*;

class InMemoryTaskManagerTest {
    public TaskManager taskManager;

    @BeforeEach
    public void beforeEach() {
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
    }

    @Test
    public void taskEqualsTest() {
        Task newTask = new Task("Bla-bla", "Bla-Bla-Bla",
                TaskStatus.DONE,
                LocalDateTime.of(0,1,1,0,0),
                Duration.ofMinutes(80));
        newTask.setId(4);
        Assertions.assertEquals(newTask, taskManager.getTaskById(4));
        Assertions.assertNotEquals(newTask, taskManager.getTaskById(1));
        newTask.setId(2);//id как у первого эпика
        Assertions.assertNotEquals(newTask, taskManager.getTaskById(2));
    }

    @Test
    public void subEpicSubTask() {
        SubTask newSubtask = new SubTask("Bla", "Bla-bla",
                TaskStatus.IN_PROGRESS,
                LocalDateTime.of(0,1,1,0,0),
                Duration.ofMinutes(80),
                8);//id - 8
        Assertions.assertEquals(-1, taskManager.addSubTask(newSubtask));
    }

    @Test
    public void gettersTest() {
        Task newTask = new Task("Помыть посуду",//id - 1
                "Прийти на кухню и вымыть все грязные тарелки",
                TaskStatus.IN_PROGRESS,
                LocalDateTime.of(2025,7,12,12,0),
                Duration.ofMinutes(30));
        newTask.setId(1);
        Assertions.assertEquals(newTask.toString(), taskManager.getTaskById(1).toString());
        Epic newEpic = new Epic("Сделать домашку",// id - 2
                "По очереди выполнить домашнее задание по всем предметам");
        newEpic.setId(2);
        Assertions.assertEquals(newEpic.toString(), taskManager.getEpicById(2).toString());
        SubTask newSubTask = new SubTask("Сделать ДЗ по Матану", // id - 3
                "Достать Тетрадь достать Демидовича сделать номера ...", TaskStatus.NEW,
                LocalDateTime.of(2025,7,12,12,0).plusMinutes(30),
                Duration.ofMinutes(30),  2);
        newSubTask.setId(3);
        Assertions.assertEquals(newSubTask.toString(), taskManager.getSubTaskById(3).toString());
        Assertions.assertEquals(newSubTask.getEpicId(), taskManager.getSubTaskById(3).getEpicId());
    }

    @Test
    public void historyTester() {
        Task firstMember = taskManager.getTaskById(1);//получаем КОПИИ элементов
        Task secondMember = taskManager.getTaskById(4);
        Epic thirdMember = taskManager.getEpicById(5);
        SubTask fourthMember = taskManager.getSubTaskById(3);
        Task fifthMember = new Task("Bla-bla", "Bla", TaskStatus.DONE,
                LocalDateTime.of(0,1,1,0,0), Duration.ofMinutes(80));
        fifthMember.setId(4);
        taskManager.updateTask(fifthMember);//изменили один из них
        fifthMember = taskManager.getTaskById(4);
        List<Task> history = taskManager.getHistory();
        Assertions.assertEquals(firstMember.toString(), history.get(0).toString());
        Assertions.assertNotEquals(secondMember.toString(), history.get(1).toString());
        Assertions.assertEquals(thirdMember.toString(), history.get(1).toString());
        Assertions.assertEquals(fourthMember.toString(), history.get(2).toString());
        Assertions.assertEquals(fifthMember.toString(), history.get(3).toString());

        for (int i = 0; i < 10; i++) {
            firstMember = taskManager.getTaskById(1);//забиваем историю
        }
        history = taskManager.getHistory();
        Assertions.assertEquals(4, history.size());
        Assertions.assertEquals(firstMember.toString(), history.get(3).toString());

        taskManager.clearEpics();
        history = taskManager.getHistory();
        Assertions.assertEquals(firstMember.toString(), history.get(1).toString());
        Assertions.assertEquals(fifthMember.toString(), history.get(0).toString());
    }

    @Test
    public void removalTester() {
        //System.out.println(taskManager.getTasks());
        //System.out.println(taskManager.getEpics());
        //System.out.println(taskManager.getSubTasks());
        taskManager.removeSubTask(6);
        //System.out.println(taskManager.getEpicById(2).getSubTasksIds());
        assertEquals(List.of(3), taskManager.getEpicById(2).getSubTasksIds());
        taskManager.removeEpic(2);
        assertEquals(List.of(taskManager.getEpicById(5)), taskManager.getEpics());
    }
}