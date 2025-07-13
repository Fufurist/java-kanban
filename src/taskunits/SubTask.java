package taskunits;

import java.time.LocalDateTime;
import java.time.Duration;

public class SubTask extends Task {
    private final int epicId;

    //Также единственный конструктор. При перезаписи поле принадлежности к эпику все равно будет игнорироваться
    public SubTask(String name, String description, TaskStatus status,
                   LocalDateTime startTime, Duration duration, int epicId) {
        super(name, description, status, startTime, duration);
        this.epicId = epicId;
    }

    public int getEpicId() {
        return epicId;
    }

    @Override
    public String toString() {
        return String.format("%d,SUBTASK,%s,%s,%s,%s,%d,%d", getId(), getName(), getStatus().name(),
                getDescription(), getStartTime() != null ? getStartTime().toString() : "null",
                getDuration().toMinutes(), getEpicId());
    }

    public static SubTask toSubTask(String line) {
        String[] parameters = line.split(",");
        SubTask result = new SubTask(parameters[2], parameters[4], TaskStatus.NEW,
                parameters[5].equals("null") ? null : LocalDateTime.parse(parameters[5]),
                Duration.ofMinutes(Long.parseLong(parameters[6])), Integer.parseInt(parameters[7]));
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
