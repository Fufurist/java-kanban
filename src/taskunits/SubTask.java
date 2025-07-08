package taskunits;

public class SubTask extends Task {
    private final int epicId;

    //Также единственный конструктор. При перезаписи поле принадлежности к эпику все равно будет игнорироваться
    public SubTask(String name, String description, TaskStatus status, int epicId) {
        super(name, description, status);
        this.epicId = epicId;
    }

    public int getEpicId() {
        return epicId;
    }

    @Override
    public String toString() {
        return String.format("%d,SUBTASK,%s,%s,%s,%d", getId(), getName(), getStatus().name(),
                getDescription(), getEpicId());
    }

    public static SubTask toSubTask(String line) {
        String[] parameters = line.split(",");
        SubTask result = new SubTask(parameters[2], parameters[4], TaskStatus.NEW, Integer.parseInt(parameters[5]));
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
