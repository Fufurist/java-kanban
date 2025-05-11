import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
/*
Добавил библиотеку jUnit5 по гайду из ТЗ. Файлы jUnit лежат в lib, но как бы я не пытался поменять путь импортов,
Идея не видит библиотечек и не компилирует. Даже когда создавал этот тестовый класс автоматически через идею она всё
равно не могла слинковать импорты с библиотечкой. Потратил слишком много времени на это, чтобы отправить хотя бы первую
итерацию до завтра, набросал примерно как должны выглядеть Обязательные тесты, упомянутые напрямую в ТЗ. Создал файлик с
заметками, где немного подробнее написал почему не реализовал тот или иной тестик. Отправляю первую итерацию в таком
виде, чтобы было с чем работать.
*/

import java.util.ArrayList;

class InMemoryTaskManagerTest {
    public TaskManager taskManager;

    @BeforeEach
    public void beforeEach(){
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
        taskManager.addTask(newEpic);
        newSubTask = new SubTask("Сделать ДЗ по Диффурам",//id - 6
                "Достать Тетрадь, достать Филлипова, сделать номера ...", TaskStatus.NEW, 2);
        taskManager.addTask(newSubTask);
        newSubTask = new SubTask("Быстро выполнить пятый спринт",//id - 7
                "Поторопись! До конца света осталось всего ничего!", TaskStatus.NEW, 5);
        taskManager.addTask(newSubTask);
    }

    @Test
    public void taskEqualsTest(){
        Task newTask = new Task("Bla-bla", "Bla-Bla-Bla", TaskStatus.DONE);
        newTask.setId(4);
        assertTrue(newTask.equals(taskManager.getTaskById(4)));
        assertFalse(newTask.equals(1));
        newTask.setId(2);//id как у первого эпика
        assertFalse(newTask.equals(2));
    }

    @Test
    public void subEpicSubTask(){
        SubTask newSubtask = new SubTask("Bla", "Bla-bla", TaskStatus.IN_PROGRESS, 8);//id - 8
        assertEquals(-1, taskManager.addSubTask(newSubtask));
    }

    @Test
    public void gettersTest(){
        Task newTask = new Task("Помыть посуду",//id - 1
                "Прийти на кухню и вымыть все грязные тарелки", TaskStatus.IN_PROGRESS);
        assertEquals(newTask.toString(), taskManager.getTaskById(1).toString()); //простая проверка всех полей
        Epic newEpic = new Epic("Сделать домашку",// id - 2
                "По очереди выполнить домашнее задание по всем предметам");
        assertEquals(newEpic.toString(), taskManager.getEpicById(2).toString());
        SubTask newSubTask = new SubTask("Сделать ДЗ по Матану", // id - 3
                "Достать Тетрадь, достать Демидовича, сделать номера ...", TaskStatus.NEW, 2);
        assertEquals(newSubTask.toString(), taskManager.getSubTaskById(3).toString());
        assertEquals(newSubTask.getEpicId(), taskManager.getSubTaskById(3).getEpicId());
    }

    @Test
    public void historyTester(){
        Task firstMember = taskManager.getTaskById(1);//получаем КОПИИ элементов
        Task secondMember = taskManager.getTaskById(4);
        Epic thirdMember = taskManager.getEpicById(5);
        SubTask fourthMember = taskManager.getSubTaskById(3);
        Task fifthMember = new Task("Bla-bla", "Bla", TaskStatus.DONE);
        fifthMember.setId(4);
        taskManager.updateTask(fifthMember);//изменили один из них
        fifthMember = taskManager.getTaskById(4);
        ArrayList<Task> history = taskManager.getHistory();
        assertEquals(firstMember.toString(), history.get(0));
        assertEquals(secondMember.toString(), history.get(1));
        assertEquals(thirdMember.toString(), history.get(2));
        assertEquals(fourthMember.toString(), history.get(3));
        assertEquals(fifthMember.toString(), history.get(4));

        for (int i = 0; i < 10; i++){
            firstMember = taskManager.getTaskById(1);//забиваем историю
        }
        history = taskManager.getHistory();
        for (Task task : history){
            assertEquals(firstMember, task);
        }
    }

}