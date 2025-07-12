package taskunits;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;

public class Epic extends Task {
    private final ArrayList<Integer> subTasksIds;
    private LocalDateTime endTime;

    //Поскольку теперь менеджер сам меняет поля, пользователям не нужно самим указывать список подзадач
    public Epic(String name, String description) {
        //сюда нужны плейсхолдеры до прибавления первых подзадач
        super(name, description, TaskStatus.NEW,
                LocalDateTime.of(0, 1, 1, 0, 0), Duration.ofMinutes(1));
        this.endTime = LocalDateTime.of(0, 1, 1, 0, 0);
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
        return String.format("%d,EPIC,%s,%s,%s,%s,%d", getId(), getName(), getStatus().name(),
                getDescription(), getStartTime().toString(), getDuration().toMinutes());
    }

    public static Epic toEpic(String line) {
        String[] parameters = line.split(",");
        Epic result = new Epic(parameters[2], parameters[4]);
        //время/длительность сохранятся, но при загрузке из файла все равно высчитаются снова из привязанных подзадач
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

    @Override
    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }
}
