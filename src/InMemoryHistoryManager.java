import java.util.ArrayList;

public class InMemoryHistoryManager implements HistoryManager{
    private static final int HISTORY_LENGTH = 10;//Не придумал места лучше, куда эту константу засунуть
    ArrayList<Task> history;

    public InMemoryHistoryManager(){
        history = new ArrayList<>(HISTORY_LENGTH);
    }

    @Override
    public boolean add(Task task) {
        //раз длина истории ограничена, не важно, под каким индексом был сохранен новый элемент
        if (task == null) return false;
        if (history.size() < HISTORY_LENGTH - 1){
            history.add(task);
            //Создание копии задачи для сохранения оставим на стороне менеджера задач, поскольку его реализация
            //действует в памяти, и это его забота, чтобы сохранять в истории копии объектов, а не ссылку на них.
            //Нам же по-факту даже не важно что мы сохраняем, лишь бы оно было наследовано от Задачи
        } else {
            history.removeFirst();
            //жутко неэффективная операция, которая переставляет все элементы на 1 назад)))
            history.add(task);
        }
        return true;
    }

    @Override
    public ArrayList<Task> getHistory() {
        return history;
    }
}
