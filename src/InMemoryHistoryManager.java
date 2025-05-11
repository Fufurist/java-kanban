import java.util.ArrayList;
import java.util.List;

public class InMemoryHistoryManager implements HistoryManager{
    private static final int HISTORY_LENGTH = 10;//Не придумал места лучше, куда эту константу засунуть
    List<Task> history;

    public InMemoryHistoryManager(){
        history = new ArrayList<>(HISTORY_LENGTH);
    }

    @Override
    public boolean add(Task task) {
        //раз длина истории ограничена, не важно, под каким индексом был сохранен новый элемент
        if (task == null) return false;
        if (history.size() == HISTORY_LENGTH - 1) history.removeFirst();
        history.add(task);
        return true;
    }

    @Override
    public List<Task> getHistory() {
        return new ArrayList<>(history);
    }
}
