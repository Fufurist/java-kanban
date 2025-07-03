package taskUnits;

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
}
