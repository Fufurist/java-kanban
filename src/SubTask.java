public class SubTask extends Task {
    private final int epicId;

    //также единственный конструктор. При перезаписи поле принадлежности к эпику все равно будет игнорироваться
    public SubTask(int id, String name, String description, TaskStatus status, int epicId) {
        super(id, name, description, status);
        this.epicId = epicId;
    }

    public int getEpicId() {
        return epicId;
    }
}
