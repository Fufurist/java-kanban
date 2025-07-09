
import managers.FileBackedTaskManager;
import managers.TaskManager;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import taskunits.Epic;
import taskunits.SubTask;
import taskunits.Task;
import taskunits.TaskStatus;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;

public class FileBackedTaskManagerTester {
    @Test
    public void emptyFileHandlingTest() {
        try {
            Path file = Files.createTempFile("emptyFileHandlingTest", null);
            TaskManager manager = FileBackedTaskManager.loadFromFile(file);
            Assertions.assertEquals(new ArrayList<>(),manager.getTasks());
            Assertions.assertEquals(new ArrayList<>(),manager.getEpics());
            Assertions.assertEquals(new ArrayList<>(),manager.getSubTasks());
            manager.clearAll();
            manager = FileBackedTaskManager.loadFromFile(file);
            Assertions.assertEquals(new ArrayList<>(),manager.getTasks());
            Assertions.assertEquals(new ArrayList<>(),manager.getEpics());
            Assertions.assertEquals(new ArrayList<>(),manager.getSubTasks());
            Files.delete(file);
        } catch (IOException e){
            Assertions.fail("Random IO fail", e);
        } catch (Throwable e){
            Assertions.fail("Unexpected exception", e);
        }
    }

    @Test
    public void dataSavingTest(){
        try {
            Path file1 = Files.createTempFile("dataSaveFile1", null);
            Path file2 = Files.createTempFile("dataSaveFile2", null);


            TaskManager taskManager = FileBackedTaskManager.loadFromFile(file1);
            Task newTask = new Task("Помыть посуду",//id - 1
                    "Прийти на кухню и вымыть все грязные тарелки", TaskStatus.IN_PROGRESS);
            taskManager.addTask(newTask);
            Epic newEpic = new Epic("Сделать домашку",// id - 2
                    "По очереди выполнить домашнее задание по всем предметам");
            taskManager.addEpic(newEpic);
            SubTask newSubTask = new SubTask("Сделать ДЗ по Матану", // id - 3
                    "Достать Тетрадь достать Демидовича сделать номера ...", TaskStatus.NEW, 2);
            taskManager.addSubTask(newSubTask);
            newTask = new Task("Подмести пол",//id - 4
                    "Достать совок и метлу смести мусор в совок выкинуть мусор из совка", TaskStatus.NEW);
            taskManager.addTask(newTask);
            newEpic = new Epic("Успеть всё",//id - 5
                    "Осталось всего 22 часа пора поторопиться и завершить этот модуль!");
            taskManager.addEpic(newEpic);
            newSubTask = new SubTask("Сделать ДЗ по Диффурам",//id - 6
                    "Достать Тетрадь достать Филлипова сделать номера ...", TaskStatus.NEW, 2);
            taskManager.addSubTask(newSubTask);
            newSubTask = new SubTask("Быстро выполнить пятый спринт",//id - 7
                    "Поторопись! До конца света осталось всего ничего!", TaskStatus.NEW, 5);
            taskManager.addSubTask(newSubTask);

            Files.copy(file1, file2, StandardCopyOption.REPLACE_EXISTING);

            TaskManager manager2 = FileBackedTaskManager.loadFromFile(file2);

            for (Task task : taskManager.getTasks()){
                System.out.println(task.toString());
                Assertions.assertEquals(task.toString(), manager2.getTaskById(task.getId()).toString());
            }
            for (Epic task : taskManager.getEpics()){
                System.out.println(task.toString());
                Assertions.assertEquals(task.toString(), manager2.getEpicById(task.getId()).toString());
            }
            for (SubTask task : taskManager.getSubTasks()){
                System.out.println(task.toString());
                Assertions.assertEquals(task.toString(), manager2.getSubTaskById(task.getId()).toString());
            }

            Files.delete(file1);
            Files.delete(file2);
        } catch (IOException e){
            Assertions.fail("Random IO fail", e);
        } catch (Throwable e){
            Assertions.fail("Unexpected exception", e);
        }
    }
}
