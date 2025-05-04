import java.util.ArrayList;

public class Epic extends Task {
    private final ArrayList<Integer> subTasksIds;

    public Epic(String name, String description, TaskStatus status) {
        super(name, description, status);
        this.subTasksIds = new ArrayList<>();
        //Новый эпик всегда создаётся первым, а потом к нему цепляются подзадачи
    }

    public Epic(int id, String name, String description, TaskStatus status, ArrayList<Integer> subclassesIds) {
        super(id, name, description, status);
        this.subTasksIds = subclassesIds;
        //Создание нового эпика на замену существующему
    }

    public void addSubTask(int subTaskId) {
        subTasksIds.add(subTaskId);
    }

    public ArrayList<Integer> getSubTasksIds() {
        return subTasksIds;
    }
}
