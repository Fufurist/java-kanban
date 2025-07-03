package Managers;

import TaskUnits.Task;

import java.util.List;

public interface HistoryManager {
    boolean add(Task task);
    boolean remove(int id);
    List<Task> getHistory();
}