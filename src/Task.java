import java.util.Objects;

public class Task {
    private static int idCounter = 0;
    private final int id;       //Эти поля все равно никак нельзя изменить кроме как
    private final String name;  //передачей в манагер новой версии задачи под тем же id
    private final String description;
    private TaskStatus status;  //А это поле мне понадобится менять в Эпиках

    //конструктор для создания абсолютно новой задачи с новым уникальным id
    public Task(String name, String description, TaskStatus status) {
        this.name = name;
        this.description = description;
        this.status = status;
        id = idCounter;
        idCounter++;
    }

    //конструктор для создания задач-заменителей, который будут передаваться в виде параметров в методы манагера
    public Task(int id, String name, String description, TaskStatus status) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.status = status;
    }

    @Override //Переопределяю метод как учили в 4 спринте
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (obj == this) return true;
        if (obj.getClass() != this.getClass()) return false;
        //В ТЗ не указано поведение метода, если пытаемся сравнить объект класса "задача" и объект класса "эпик"
        //Поэтому считаю, что пользователь всегда в курсе, задачу какого типа он отправляет на проверку сравнением
        Task newTask = (Task) obj;
        return id == newTask.getId();   //По ТЗ задачи являются идентичными, если имеют одинаковые идентификаторы
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);    //Так у "Идентичных" задач будет одинаковый хеш
    }

    //В ТЗ не указывают необходимость переопределения toSting() и вообще наличия хоть каких либо методов в этом классе
    //Поэтому оставлю за собой право объявлять и не объявлять любые методы, которые посчитаю нужными для реализации ТЗ

    public void setStatus(TaskStatus status) {  //пригодится
        this.status = status;
    }

    public int getId() {    //Пригодится
        return id;
    }

    public String getName() {   //Не нужна по ТЗ, но пусть будет
        return name;
    }

    public String getDescription() {    //Не нужна по ТЗ, но пусть будет
        return description;
    }

    public TaskStatus getStatus() { //Пригодится
        return status;
    }
}