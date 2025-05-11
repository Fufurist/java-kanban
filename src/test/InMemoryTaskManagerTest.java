package test;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import java.util.List;

import Managers.*;
import TaskUnits.*;

class InMemoryTaskManagerTest {
    public TaskManager taskManager;

    @BeforeEach
    public void beforeEach() {
        taskManager = Managers.getDefault();
        Task newTask = new Task("Помыть посуду",//id - 1
                "Прийти на кухню и вымыть все грязные тарелки", TaskStatus.IN_PROGRESS);
        taskManager.addTask(newTask);
        Epic newEpic = new Epic("Сделать домашку",// id - 2
                "По очереди выполнить домашнее задание по всем предметам");
        taskManager.addEpic(newEpic);
        SubTask newSubTask = new SubTask("Сделать ДЗ по Матану", // id - 3
                "Достать Тетрадь, достать Демидовича, сделать номера ...", TaskStatus.NEW, 2);
        taskManager.addSubTask(newSubTask);
        newTask = new Task("Подмести пол",//id - 4
                "Достать совок и метлу, смести мусор в совок, выкинуть мусор из совка", TaskStatus.NEW);
        taskManager.addTask(newTask);
        newEpic = new Epic("Успеть всё",//id - 5
                "Осталось всего 22 часа, пора поторопиться и завершить этот модуль!");
        taskManager.addEpic(newEpic);
        newSubTask = new SubTask("Сделать ДЗ по Диффурам",//id - 6
                "Достать Тетрадь, достать Филлипова, сделать номера ...", TaskStatus.NEW, 2);
        taskManager.addSubTask(newSubTask);
        newSubTask = new SubTask("Быстро выполнить пятый спринт",//id - 7
                "Поторопись! До конца света осталось всего ничего!", TaskStatus.NEW, 5);
        taskManager.addSubTask(newSubTask);
    }

    @Test
    public void taskEqualsTest() {
        Task newTask = new Task("Bla-bla", "Bla-Bla-Bla", TaskStatus.DONE);
        newTask.setId(4);
        Assertions.assertEquals(newTask, taskManager.getTaskById(4));
        Assertions.assertNotEquals(newTask, taskManager.getTaskById(1));
        newTask.setId(2);//id как у первого эпика
        Assertions.assertNotEquals(newTask, taskManager.getTaskById(2));
    }

    @Test
    public void subEpicSubTask() {
        SubTask newSubtask = new SubTask("Bla", "Bla-bla", TaskStatus.IN_PROGRESS, 8);//id - 8
        Assertions.assertEquals(-1, taskManager.addSubTask(newSubtask));
    }

    @Test
    public void gettersTest() {
        Task newTask = new Task("Помыть посуду",//id - 1
                "Прийти на кухню и вымыть все грязные тарелки", TaskStatus.IN_PROGRESS);
        newTask.setId(1);
        Assertions.assertEquals(newTask.toString(), taskManager.getTaskById(1).toString()); //простая проверка всех полей
        Epic newEpic = new Epic("Сделать домашку",// id - 2
                "По очереди выполнить домашнее задание по всем предметам");
        newEpic.setId(2);
        Assertions.assertEquals(newEpic.toString(), taskManager.getEpicById(2).toString());
        SubTask newSubTask = new SubTask("Сделать ДЗ по Матану", // id - 3
                "Достать Тетрадь, достать Демидовича, сделать номера ...", TaskStatus.NEW, 2);
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
        Task fifthMember = new Task("Bla-bla", "Bla", TaskStatus.DONE);
        fifthMember.setId(4);
        taskManager.updateTask(fifthMember);//изменили один из них
        fifthMember = taskManager.getTaskById(4);
        List<Task> history = taskManager.getHistory();
        Assertions.assertEquals(firstMember.toString(), history.get(0).toString());
        Assertions.assertEquals(secondMember.toString(), history.get(1).toString());
        Assertions.assertEquals(thirdMember.toString(), history.get(2).toString());
        Assertions.assertEquals(fourthMember.toString(), history.get(3).toString());
        Assertions.assertEquals(fifthMember.toString(), history.get(4).toString());

        for (int i = 0; i < 10; i++) {
            firstMember = taskManager.getTaskById(1);//забиваем историю
        }
        history = taskManager.getHistory();
        for (Task task : history) {
            assertEquals(firstMember, task);
        }
    }

}