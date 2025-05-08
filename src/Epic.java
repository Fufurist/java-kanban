import java.util.ArrayList;

public class Epic extends Task {
    private final ArrayList<Integer> subTasksIds;

    //Поскольку теперь менеджер сам меняет поля, пользователям не нужно самим указывать список подзадач
    public Epic(String name, String description) {//убрал смену статуса из конструктора
        super(name, description, TaskStatus.NEW);//пусть по умолчанию новый будет
        this.subTasksIds = new ArrayList<>();
    }

    public void addSubTask(int subTaskId) {
        subTasksIds.add(subTaskId);
    }

    public ArrayList<Integer> getSubTasksIds() {
        return new ArrayList<>(subTasksIds);
    }

    //сделаю все методы, где может возникнуть ошибка булевскими, и не буду допускать ошибок
    public void removeSubTaskId(int id) {
        subTasksIds.remove(id);
    }

    public void removeAllSubTasks() {
        subTasksIds.clear();
    }
}
