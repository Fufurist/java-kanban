package managers;

import taskUnits.Epic;
import taskUnits.SubTask;
import taskUnits.Task;

import java.util.List;

public interface TaskManager {
    List<Task> getTasks();

    List<Epic> getEpics();

    List<SubTask> getSubTasks();

    void clearAll();

    Task getTaskById(int id);

    Epic getEpicById(int id);

    SubTask getSubTaskById(int id);

    int addTask(Task task);

    int addEpic(Epic epic);

    int addSubTask(SubTask subTask);

    boolean updateTask(Task task);

    boolean updateEpic(Epic epic);

    boolean updateSubTask(SubTask subTask);

    boolean removeTask(int id);

    boolean removeEpic(int id);

    boolean removeSubTask(int id);

    List<SubTask> getEpicSubTasksIds(int id);

    void clearTasks();

    void clearSubTasks();

    void clearEpics();

    List<Task> getHistory();
}
