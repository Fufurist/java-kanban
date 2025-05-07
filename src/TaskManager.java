import java.util.ArrayList;
import java.util.HashMap;

public class TaskManager {
    private final HashMap<Integer, Task> tasks;
    private final HashMap<Integer, Epic> epics;
    private final HashMap<Integer, SubTask> subTasks;
    private final ArrayList<Integer> freeIds; //сохраняя освободившиеся id мы делаем их более скомпонованными ближе к 0
    private int currentMaxId; //что повышает удобство взаимодействия с ними для пользователя

    public TaskManager() {
        tasks = new HashMap<>();
        epics = new HashMap<>();
        subTasks = new HashMap<>();
        freeIds = new ArrayList<>();
        currentMaxId = 0;
    }

    public HashMap<Integer, Task> getTasks() {
        return new HashMap<>(tasks);
    }//возвращаем в том же виде, что храним

    public HashMap<Integer, Epic> getEpics() {
        return new HashMap<>(epics);
    }

    public HashMap<Integer, SubTask> getSubTasks() {
        return new HashMap<>(subTasks);
    }

    public void clearAll() {
        tasks.clear();
        epics.clear();
        subTasks.clear();
        freeIds.clear();
        currentMaxId = 0;
    }//раздельные методы чистки каждой подкатегории реализую сильно ниже

    //будет три раздельных метода поиска по id, потому что явное преобразование типов проходится позже
    public Task getTaskById(int id) {
        if (tasks.containsKey(id)) {
            return tasks.get(id);//опять вопрос, заморочиться с созданием копии, или так и возвращать ссылку на сущ.
        } else {
            return null;
        }
    }

    public Epic getEpicById(int id) {
        if (epics.containsKey(id)) {//ИДЕЯ предлагает заменить на getOrDefault, но имея в виду вопрос выше оставлю так
            return epics.get(id);
        } else {
            return null;
        }
    }

    public SubTask getSubTaskById(int id) {
        if (subTasks.containsKey(id)) {
            return subTasks.get(id);
        } else {
            return null;
        }
    }

    public boolean addTask(Task task) {
        if (task == null) return false;
        Task newTask;
        if (freeIds.isEmpty()) {
            newTask = new Task(++currentMaxId, task.getName(), task.getDescription(), task.getStatus());
        } else {
            newTask = new Task(freeIds.getLast(), task.getName(), task.getDescription(), task.getStatus());
            freeIds.removeLast();//Почитал спецификации. Написано, что getLast и removeLast имеют сложность O(1);
        }
        tasks.put(newTask.getId(), newTask);
        return true;
    }

    public boolean addEpic(Epic epic) {//Пока не знаем, что такое перегрузка))
        if (epic == null) return false;
        Epic newEpic;
        if (freeIds.isEmpty()) {
            newEpic = new Epic(++currentMaxId, epic.getName(), epic.getDescription(), epic.getStatus());
        } else {
            newEpic = new Epic(freeIds.getLast(), epic.getName(), epic.getDescription(), epic.getStatus());
            freeIds.removeLast();
        }
        epics.put(newEpic.getId(), newEpic);//Новый эпик добавляется с пустым списком подзадач
        return true;
    }

    public boolean addSubTask(SubTask subTask) {
        if (subTask == null) return false;
        if (!epics.containsKey(subTask.getEpicId())) return false;
        SubTask newSubTask;
        if (freeIds.isEmpty()) {
            newSubTask = new SubTask(++currentMaxId, subTask.getName(), subTask.getDescription(),
                    subTask.getStatus(), subTask.getEpicId());
        } else {
            newSubTask = new SubTask(freeIds.getLast(), subTask.getName(), subTask.getDescription(),
                    subTask.getStatus(), subTask.getEpicId());
            freeIds.removeLast();
        }
        epics.get(subTask.getEpicId()).addSubTask(newSubTask.getId());
        subTasks.put(newSubTask.getId(), newSubTask);
        adjustEpicStatus(subTask.getEpicId());
        return true;
    }

    public boolean updateTask(Task task) {//на этом моменте ИДЕЯ предложила добавить параметр @notNull
        if (task == null) return false;//поэтому решил перестраховаться еще больше и везде воткнуть проверку параметра
        if (tasks.containsKey(task.getId())) {
            Task upTask = tasks.get(task.getId());
            upTask.setName(task.getName());
            upTask.setDescription(task.getDescription());
            upTask.setStatus(task.getStatus());
            return true;
        }
        return false;
    }

    public boolean updateEpic(Epic epic) {
        if (epic == null) return false;
        if (tasks.containsKey(epic.getId())) {
            Epic upEpic = epics.get(epic.getId());
            upEpic.setName(epic.getName());
            upEpic.setDescription(epic.getDescription());
            upEpic.setStatus(epic.getStatus());
            return true;//изменять список подзадач пользователю не положено
        }
        return false;
    }

    public boolean updateSubTask(SubTask subTask) {
        if (subTask == null) return false;
        if (tasks.containsKey(subTask.getId())) {
            SubTask upSubTask = subTasks.get(subTask.getId());
            upSubTask.setName(subTask.getName());
            upSubTask.setDescription(subTask.getDescription());
            upSubTask.setStatus(subTask.getStatus());
            adjustEpicStatus(upSubTask.getEpicId());
            return true;//А вот стоит ли добавить возможность менять принадлежность к эпику - новый вопрос
        }               //Хотя пока возможности это сделать всё равно нет, потому что нет соответствующих методов.
        return false;
    }

    public boolean removeTask(int id) {
        if (tasks.containsKey(id)) {
            freeIds.addLast(id);//по-факту пользуюсь этим списком как стеком, используя только операции O(1)
            tasks.remove(id);
        }
        return false;
    }

    public boolean removeEpic(int id) {
        if (epics.containsKey(id)) {
            freeIds.addLast(id);
            //надо также удалить все подзадачи
            ArrayList<Integer> subs = epics.get(id).getSubTasksIds();
            for (int i : subs){//поскольку сами ведём список подзадач, можем быть уверены, что в нём валидные id
                subTasks.remove(i);
            }
            epics.remove(id);
        }
        return false;
    }

    public boolean removeSubTask(int id) {
        if (subTasks.containsKey(id)) {
            epics.get(subTasks.get(id).getEpicId()).removeSubTaskId(id);
            adjustEpicStatus(subTasks.get(id).getEpicId());
            freeIds.addLast(id);
            subTasks.remove(id);
        }
        return false;
    }

    public ArrayList<Integer> getEpicSubTasksIds(int id) {//
        if (epics.containsKey(id)) {
            return new ArrayList<>(epics.get(id).getSubTasksIds());
        }
        return null;
    }

    //пришло время чистки отдельных списков
    public void clearTasks(){
        freeIds.addAll(tasks.keySet());
        tasks.clear();
    }

    public void clearSubTasks(){
        freeIds.addAll(subTasks.keySet());
        subTasks.clear();
        for (Epic epic : epics.values()){//сначала хотел вызвать перебор всех adjustEpicStatus
            epic.setStatus(TaskStatus.NEW);//но так лучше быстродействие
        }
    }

    public void clearEpics(){
        freeIds.addAll(epics.keySet());
        epics.clear();
        freeIds.addAll(subTasks.keySet());//подзадачи не существуют без эпиков
        subTasks.clear();
    }

    private void adjustEpicStatus(int id) {//Вызывать каждый раз когда какие-то изменения в эпиках или подзадачах
        Epic epic = epics.get(id);
        ArrayList<Integer> subTasksIds = epic.getSubTasksIds();
        boolean allNew = true;
        boolean allDone = true;

        if (subTasksIds.isEmpty()) {
            epic.setStatus(TaskStatus.NEW);//"если у эпика нет подзадач, то статус должен быть NEW"
            return;
        }
        for (int i : subTasksIds){
            switch (subTasks.get(id).getStatus()) {
                case NEW:
                    allDone = false;
                    break;
                case IN_PROGRESS:
                    allDone = false;
                    allNew = false;
                    break;
                case DONE:
                    allNew = false;
                    break;
            }
        }
        if (allNew) {
            epic.setStatus(TaskStatus.NEW);
        } else if (allDone) {
            epic.setStatus(TaskStatus.DONE);
        } else {
            epic.setStatus(TaskStatus.IN_PROGRESS);
        }
    }
}