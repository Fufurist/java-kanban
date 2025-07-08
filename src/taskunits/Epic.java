package taskunits;

import java.util.ArrayList;

public class Epic extends Task {
    private final ArrayList<Integer> subTasksIds;

    //Поскольку теперь менеджер сам меняет поля, пользователям не нужно самим указывать список подзадач
    public Epic(String name, String description) {
        super(name, description, TaskStatus.NEW);//пусть по умолчанию новый будет
        this.subTasksIds = new ArrayList<>();
    }

    public void addSubTask(int subTaskId) {
        subTasksIds.add(subTaskId);
    }

    public ArrayList<Integer> getSubTasksIds() {
        return new ArrayList<>(subTasksIds);
    }

    public void removeSubTaskId(int id) {
        subTasksIds.remove(Integer.valueOf(id));//ИДЕЯ говорит, что теперь вызывается второй метод
    }

    public void removeAllSubTasks() {
        subTasksIds.clear();
    }

    @Override
    public String toString() {
        return String.format("%d,EPIC,%s,%s,%s", getId(), getName(), getStatus().name(),
                getDescription());
    }

    public static Epic toEpic(String line) {
        String[] parameters = line.split(",");
        Epic result = new Epic(parameters[2], parameters[4]);
        result.setId(Integer.parseInt(parameters[0]));
        switch (parameters[3]) {
            case "NEW":
                result.setStatus(TaskStatus.NEW);
                break;
            case "IN_PROGRESS":
                result.setStatus(TaskStatus.IN_PROGRESS);
                break;
            case "DONE":
                result.setStatus(TaskStatus.DONE);
                break;
        }
        return result;
    }
}
