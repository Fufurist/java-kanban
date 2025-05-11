import java.util.List;

public interface HistoryManager {
    boolean add(Task task);

    //Поскольку других списков не проходили, будем пользоваться этим
    List<Task> getHistory();
}