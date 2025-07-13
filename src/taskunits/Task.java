package taskunits;

import java.time.Duration;
import java.util.Objects;
import java.time.LocalDateTime;

public class Task {
    private int id;
    private String name;
    private String description;
    private TaskStatus status;
    private LocalDateTime startTime;
    private Duration duration;

    public Task(String name, String description, TaskStatus status, LocalDateTime startTime, Duration duration) {
        this.id = 0;
        this.name = name;
        this.description = description;
        this.status = status;
        this.startTime = startTime;
        this.duration = duration;
    }

    @Override //Переопределяю метод как учили в 4 спринте
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (obj == this) return true;
        if (obj.getClass() != this.getClass()) return false;
        Task newTask = (Task) obj;
        return id == newTask.getId();   //По ТЗ задачи являются идентичными, если имеют одинаковые идентификаторы
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);    //Так у "Идентичных" задач будет одинаковый хеш
    }

    @Override
    public String toString() {
        return String.format("%d,TASK,%s,%s,%s,%s,%d",
                id, name, status.name(), description,
                startTime != null ? startTime.toString() : "null", duration.toMinutes());
    }

    public static Task toTask(String line) {
        String[] parameters = line.split(",");
        Task result = new Task(parameters[2], parameters[4], TaskStatus.NEW,
                parameters[5].equals("null") ? null : LocalDateTime.parse(parameters[5]),
                Duration.ofMinutes(Long.parseLong(parameters[6])));
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

    public LocalDateTime getEndTime() {
        if (startTime == null) return null;
        return startTime.plus(duration);
    }

    public static int byStartTimeTaskComparator(Task task1, Task task2) {
        if (task1 == null && task2 == null) return 0;
        if (task1 == null) return -1;
        if (task2 == null) return 1;
        return task1.getStartTime().compareTo(task2.getStartTime());
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setStatus(TaskStatus status) {  //пригодится
        this.status = status;
    }

    public void setDuration(Duration duration) {
        this.duration = duration;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public Duration getDuration() {
        return duration;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }
}