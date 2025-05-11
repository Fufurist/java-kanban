import java.util.ArrayList;

public interface HistoryManager {
    boolean add(Task task);

    //Поскольку других списков не проходили, будем пользоваться этим
    ArrayList<Task> getHistory();
}