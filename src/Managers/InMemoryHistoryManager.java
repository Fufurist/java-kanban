package Managers;

import TaskUnits.Task;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class InMemoryHistoryManager implements HistoryManager{
    //В ТЗ сказано, что свою реализацию связного списка с доступом по id надо писать прямо в этом классе.
    //Пусть вся реализация будет приватной, а Node вообще будет package-private, и собственный пакет этому манагеру.
    private Map<Integer, Node> history;
    private Node firstElem;
    private Node lastElem;

    public InMemoryHistoryManager(){
        history = new HashMap<>();
        firstElem = null;
        lastElem = null;
    }

    private void linkLast(Node elem){
        if (elem == null) return;
        elem.setNext(null);
        if (lastElem == null){
            firstElem = elem;
            lastElem = elem;
            elem.setPrev(null);
        } else {
            lastElem.setNext(elem);
            elem.setPrev(lastElem);
            lastElem = elem;
        }
    }

    private void removeNode(Node elem){
        if (elem == null) return;
        if (elem.getNext() != null){
            elem.getNext().setPrev(elem.getPrev());
        } else {//если удаляемый был последним.
            lastElem = elem.getPrev();
        }
        if (elem.getPrev() != null){
            elem.getPrev().setNext(elem.getNext());
        } else {//если удаляемый был первым.
            firstElem = elem.getNext();
        }
        //Вытащили элемент из цепочки. Потом при помощи linkLast переназначатся его ссылки.
        //Для задачи мы только вырываем и вставляем элементы в конец, никогда не удаляя их полностью.
    }

    private List<Task> getTasks(){
        List<Task> historySequence = new ArrayList<>();
        Node curr = firstElem;
        while (curr != null){
            historySequence.add(curr.getInfo());
            curr = curr.getNext();
        }
        return historySequence;
    }

    @Override
    public boolean add(Task task) {
        if (task == null) return false;
        int id = task.getId();
        Node node = new Node(task);
        removeNode(history.put(id, node));
        linkLast(node);
        return true;
    }

    @Override
    public List<Task> getHistory(){
        return getTasks();
    }

    @Override
    public boolean remove(int id){
        if (!history.containsKey(id)){
            return false;
        } else {
            removeNode(history.get(id));
            history.remove(id);
        }
        return true;
    }
}
