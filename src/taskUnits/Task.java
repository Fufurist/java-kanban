package taskUnits;

import java.util.Objects;

public class Task {
    private int id;
    private String name;
    private String description;
    private TaskStatus status;

    public Task(String name, String description, TaskStatus status) {
        this.id = 0;
        this.name = name;
        this.description = description;
        this.status = status;
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
        String output = "Задача " + id + ": " + name + " - ";
        if (description != null) {
            output += description.length() + "с. ";
        } else {
            output += "нет";
        }
        output += "описания, статус - " + status + ".";
        return output;
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
}