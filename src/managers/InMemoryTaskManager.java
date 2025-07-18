package managers;

import taskunits.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

public class InMemoryTaskManager implements TaskManager {
    private final Map<Integer, Task> tasks;
    private final Map<Integer, Epic> epics;
    private final Map<Integer, SubTask> subTasks;
    //Раз уж полез менять реализацию тут, для хранения свободных id как раз подойдёт TreeSet
    private final NavigableSet<Integer> freeIds;
    private int currentMaxId; //что повышает удобство взаимодействия с ними для пользователя
    private final HistoryManager history;
    private final NavigableSet<Task> prioritySortedTaskSet;

    public InMemoryTaskManager() {
        tasks = new HashMap<>();
        epics = new HashMap<>();
        subTasks = new HashMap<>();
        freeIds = new TreeSet<>();
        currentMaxId = 1;
        this.history = Managers.getDefaultHistory();
        this.prioritySortedTaskSet = new TreeSet<>(Task::byStartTimeTaskComparator);// Не вижу смысла в отдельном классе
    }

    @Override
    public List<Task> getTasks() {
        return new ArrayList<>(tasks.values());
    } //возвращаем списком

    @Override
    public List<Epic> getEpics() {
        return new ArrayList<>(epics.values());
    }

    @Override
    public List<SubTask> getSubTasks() {
        return new ArrayList<>(subTasks.values());
    }

    @Override
    public void clearAll() {
        tasks.clear();
        epics.clear();
        subTasks.clear();
        freeIds.clear();
        currentMaxId = 1;
        prioritySortedTaskSet.clear();
    } //раздельные методы чистки каждой подкатегории сильно ниже

    @Override
    public Task getTaskById(int id) {
        if (!tasks.containsKey(id)) return null;
        //Оставлю по одной копии(
        Task copiedTask = tasks.get(id);
        Task toReturnTask = new Task(copiedTask.getName(), copiedTask.getDescription(), copiedTask.getStatus(),
                copiedTask.getStartTime(), copiedTask.getDuration());
        toReturnTask.setId(copiedTask.getId());
        //Скопировал все поля
        history.add(toReturnTask);
        return toReturnTask;
    }

    @Override
    public Epic getEpicById(int id) {
        if (!epics.containsKey(id)) return null;
        Epic copiedEpic = epics.get(id);
        Epic toReturnEpic = new Epic(copiedEpic.getName(), copiedEpic.getDescription());
        toReturnEpic.setId(copiedEpic.getId());
        toReturnEpic.setStartTime(copiedEpic.getStartTime());
        toReturnEpic.setDuration(copiedEpic.getDuration());
        toReturnEpic.setEndTime(copiedEpic.getEndTime());
        toReturnEpic.setStatus(copiedEpic.getStatus());
        for (int i : copiedEpic.getSubTasksIds()) { //вручную скопировать список подзадач
            toReturnEpic.addSubTask(i);
        }
        history.add(toReturnEpic);
        return toReturnEpic;
    }

    @Override
    public SubTask getSubTaskById(int id) {
        if (!subTasks.containsKey(id)) return null;
        SubTask copiedSubTask = subTasks.get(id);
        SubTask toReturnSubTask = new SubTask(copiedSubTask.getName(), copiedSubTask.getDescription(),
                copiedSubTask.getStatus(), copiedSubTask.getStartTime(), copiedSubTask.getDuration(),
                copiedSubTask.getEpicId());
        toReturnSubTask.setId(copiedSubTask.getId());
        history.add(toReturnSubTask);
        return toReturnSubTask;
    }


    // Методы add нуждаются в доработке: чтобы корректно загружать задачи из файла, нам надо при добавлении присваивать
    // задачам конкретный id, который присвоен им в файле.
    // Поэтому, если мы добавляем задачу с ненулевым id, нам надо проверить, свободен ли этот Id, и, если да, присвоить
    // новой задаче именно его. Если не свободен или равен нулю (отрицательному) менеджер сам распределит его на новое
    // место и отправит дальше.
    private int idHandler(int suggestedId) {
        int resultId;
        if (suggestedId <= 0) {
            // Старое назначение свободного id
            if (freeIds.isEmpty()) {
                resultId = currentMaxId;
                currentMaxId++;
            } else {
                // IDEA жалуется на Nullability and data flow problems на pollFirst(), но он не может выдать null,
                // поскольку перед этим я проверяю, что хоть что-то в наборе есть. Скорее всего IDEA просто не может
                // обнаружить, что на этом этапе этот метод никак не может выдать null.
                resultId = freeIds.pollFirst();
            }
        } else if (suggestedId > currentMaxId) {
            //если id вне зоны текущего покрытия
            while (currentMaxId < suggestedId) {
                freeIds.add(currentMaxId);
                currentMaxId++;
            }
            resultId = currentMaxId; // Эквивалентно resultId = suggestedId
            currentMaxId++;
        } else {
            if (freeIds.contains(suggestedId)) {
                // если id свободен, отмечаем его несвободным, и отправляем далее
                resultId = suggestedId;
                freeIds.remove(suggestedId);
            } else {
                // Если id занят, по стандартной схеме выделяем задаче новый id. Если пользователь вводил ненулевой id,
                // он сможет проверить, что всё в порядке, сравнив возвращаемый id и тот, который он хотел. Это
                // просигнализирует ему о его просчёте, если они не совпадут.
                resultId = currentMaxId;
                currentMaxId++;
            }
        }
        return resultId;
    }

    @Override
    public int addTask(Task task) {
        if (task == null) return -1;
        if (overlapCheck(task)) return -1;
        Task newTask = new Task(task.getName(), task.getDescription(), task.getStatus(),
                task.getStartTime(), task.getDuration());
        int id = task.getId();
        id = idHandler(id);
        newTask.setId(id);
        tasks.put(id, newTask);
        if (newTask.getStartTime() != null) prioritySortedTaskSet.add(newTask);
        return id;
    }

    @Override
    public int addEpic(Epic epic) {
        if (epic == null) return -1;
        //Всё кроме имени и описания в чистом эпике - плейсхолдеры, манагер сам будет назначать остальные поля
        Epic newEpic = new Epic(epic.getName(), epic.getDescription());
        int id = epic.getId();
        id = idHandler(id);
        newEpic.setId(id);
        epics.put(id, newEpic);//Новый эпик добавляется с пустым списком подзадач
        //Эпики не будут содержаться в списке приоритетов, потому что они дублируют свои подзадачи
        return id;
    }

    @Override
    public int addSubTask(SubTask subTask) {
        if (subTask == null) return -1;
        if (overlapCheck(subTask)) return -1;
        if (!epics.containsKey(subTask.getEpicId())) return -1;//если не существует подходящего эпика
        SubTask newSubTask = new SubTask(subTask.getName(), subTask.getDescription(),
                subTask.getStatus(), subTask.getStartTime(), subTask.getDuration(), subTask.getEpicId());
        int id = subTask.getId();
        id = idHandler(id);
        newSubTask.setId(id);
        subTasks.put(id, newSubTask);
        if (newSubTask.getStartTime() != null) prioritySortedTaskSet.add(newSubTask);
        epics.get(subTask.getEpicId()).addSubTask(id);
        adjustEpicStatus(subTask.getEpicId());
        adjustEpicTime(subTask.getEpicId());
        return id;
    }

    @Override
    public boolean updateTask(Task task) { //на этом моменте ИДЕЯ предложила добавить параметр @notNull
        if (task == null) return false;//поэтому решил перестраховаться еще больше и везде воткнуть проверку параметра
        if (!tasks.containsKey(task.getId())) return false;
        Task upTask = tasks.get(task.getId());
        //Удаление и возвращение в дерево нужны, чтобы обновить порядок.
        if (upTask.getStartTime() != null) {
            prioritySortedTaskSet.remove(upTask);
            if (overlapCheck(task)) {
                prioritySortedTaskSet.add(upTask);
                return false;
            }
        } else {
            if (overlapCheck(task)) return false;
        }
        upTask.setName(task.getName());
        upTask.setDescription(task.getDescription());
        upTask.setStatus(task.getStatus());
        upTask.setStartTime(task.getStartTime());
        upTask.setDuration(task.getDuration());
        if (upTask.getStartTime() != null) prioritySortedTaskSet.add(upTask);
        return true;
    }

    @Override
    public boolean updateEpic(Epic epic) {
        if (epic == null) return false;
        if (tasks.containsKey(epic.getId())) {
            Epic upEpic = epics.get(epic.getId());
            upEpic.setName(epic.getName());
            upEpic.setDescription(epic.getDescription());
            return true;//изменять что-либо кроме имени и описания пользователю не положено
        }
        return false;
    }

    @Override
    public boolean updateSubTask(SubTask subTask) {
        if (subTask == null) return false;
        if (!subTasks.containsKey(subTask.getId())) return false;
        SubTask upSubTask = subTasks.get(subTask.getId());
        if (upSubTask.getEpicId() != subTask.getEpicId()) return false; //проверяем, что новый принадлежит тому же эпику
        if (upSubTask.getStartTime() != null) {
            prioritySortedTaskSet.remove(upSubTask);
            if (overlapCheck(subTask)) { // и не будет оверлапиться
                prioritySortedTaskSet.add(upSubTask);
                return false;
            }
        } else {
            if (overlapCheck(subTask)) return false;
        }
        upSubTask.setName(subTask.getName());
        upSubTask.setDescription(subTask.getDescription());
        upSubTask.setStatus(subTask.getStatus());
        upSubTask.setStartTime(subTask.getStartTime());
        upSubTask.setDuration(subTask.getDuration());
        if (upSubTask.getStartTime() != null) prioritySortedTaskSet.add(upSubTask);
        adjustEpicStatus(upSubTask.getEpicId());
        adjustEpicTime(upSubTask.getEpicId());
        return true;
    }

    @Override
    public boolean removeTask(int id) {
        if (tasks.containsKey(id)) {
            if (tasks.get(id).getStartTime() != null) prioritySortedTaskSet.remove(tasks.get(id));
            freeIds.add(id);
            tasks.remove(id);
            history.remove(id);
            return true;
        }
        return false;
    }

    @Override
    public boolean removeEpic(int id) {
        if (epics.containsKey(id)) {
            freeIds.add(id);
            //надо также удалить все подзадачи
            ArrayList<Integer> subs = epics.get(id).getSubTasksIds();
            for (int i : subs) { //поскольку сами ведём список подзадач, можем быть уверены, что в нём валидные id
                if (subTasks.get(i).getStartTime() != null) prioritySortedTaskSet.remove(subTasks.get(i));
                subTasks.remove(i);
                history.remove(i);
            }
            epics.remove(id);
            history.remove(id);
            return true;
        }
        return false;
    }

    @Override
    public boolean removeSubTask(int id) {
        if (subTasks.containsKey(id)) {
            if (subTasks.get(id).getStartTime() != null) prioritySortedTaskSet.remove(subTasks.get(id));
            epics.get(subTasks.get(id).getEpicId()).removeSubTaskId(id);
            adjustEpicStatus(subTasks.get(id).getEpicId());
            adjustEpicTime(subTasks.get(id).getEpicId());
            freeIds.add(id);
            subTasks.remove(id);
            history.remove(id);
            return true;
        }
        return false;
    }

    @Override
    public List<SubTask> getEpicSubTasksIds(int id) { //изменил сигнатуру
        if (epics.containsKey(id)) {
            ArrayList<Integer> subTasksIds = new ArrayList<>(epics.get(id).getSubTasksIds());
            ArrayList<SubTask> output = new ArrayList<>();
            for (int i : subTasksIds) { //Специально проверил маны. В HashMap нет методов, по набору ключей возвращающих
                output.add(subTasks.get(i));//Коллекцию значений. Скорее всего потому, что коллекций много.
            }
            return output;
        }
        return null;
    }

    //пришло время чистки отдельных списков
    @Override
    public void clearTasks() {
        freeIds.addAll(tasks.keySet());
        for (Integer id : tasks.keySet()) {
            history.remove(id);
            if (tasks.get(id).getStartTime() != null) prioritySortedTaskSet.remove(tasks.get(id));
        }
        tasks.clear();
    }

    @Override
    public void clearSubTasks() {
        for (Epic epic : epics.values()) {
            epic.removeAllSubTasks();
            adjustEpicStatus(epic.getId());//поскольку список подзадач пустой, метод выйдет уже после второй проверки
            adjustEpicTime(epic.getId()); //Если у эпика нет списка подзадач, то ничего не найдётся
        }
        freeIds.addAll(subTasks.keySet());
        for (Integer id : subTasks.keySet()) {
            history.remove(id);
            if (subTasks.get(id).getStartTime() != null) prioritySortedTaskSet.remove(subTasks.get(id));
        }
        subTasks.clear();
    }

    @Override
    public void clearEpics() {
        freeIds.addAll(epics.keySet());
        for (Integer id : epics.keySet()) {
            history.remove(id);
        }
        epics.clear();
        freeIds.addAll(subTasks.keySet());//подзадачи не существуют без эпиков
        for (Integer id : subTasks.keySet()) {
            history.remove(id);
            if (subTasks.get(id).getStartTime() != null) prioritySortedTaskSet.remove(subTasks.get(id));
        }
        subTasks.clear();
    }

    private void adjustEpicStatus(int id) { //Вызывать каждый раз когда какие-то изменения в подзадачах
        Epic epic = epics.get(id);
        ArrayList<Integer> subTasksIds = epic.getSubTasksIds();
        boolean allNew = true;
        boolean allDone = true;

        if (subTasksIds.isEmpty()) {
            epic.setStatus(TaskStatus.NEW);//"если у эпика нет подзадач, то статус должен быть NEW"
            return;
        }
        for (int i : subTasksIds) {
            switch (subTasks.get(i).getStatus()) { //теперь перебор идет по списку id из списка подзадач
                case NEW:
                    allDone = false;//противоположные статусы опускают флаги друг друга
                    break;
                case IN_PROGRESS:
                    epic.setStatus(TaskStatus.IN_PROGRESS);
                    return;//если хоть одна подзадача в прогрессе, то и эпик сразу в прогрессе
                case DONE:
                    allNew = false;
                    break;
            }
        }
        if (allNew) {
            epic.setStatus(TaskStatus.NEW);
        } else if (allDone) {
            epic.setStatus(TaskStatus.DONE);
        } else { //вызовется, если часть новые, а часть выполнена
            epic.setStatus(TaskStatus.IN_PROGRESS);
        }
    }

    private boolean overlapCheck(Task task) {
        if (task == null) return true;
        if (prioritySortedTaskSet.contains(task)) return true;
        Task prev = prioritySortedTaskSet.lower(task);
        if (prev != null && prev.getEndTime().isAfter(task.getStartTime())) return true;
        Task next = prioritySortedTaskSet.higher(task);
        return next != null && task.getEndTime().isAfter(next.getStartTime());
    }

    private void adjustEpicTime(int id) {
        Epic epic = epics.get(id);
        List<SubTask> itsSubTasks = epic.getSubTasksIds().stream().map(subTasks::get).toList();
        if (itsSubTasks.isEmpty()) {
            epic.setStartTime(null);
            epic.setDuration(Duration.ofMinutes(0));
            epic.setEndTime(null);
        } else {
            epic.setStartTime(itsSubTasks.stream()
                    .map(SubTask::getStartTime)
                    .filter(Objects::nonNull)
                    .min(LocalDateTime::compareTo).orElse(null));
            epic.setDuration(itsSubTasks.stream()
                    .filter(sub -> sub.getStartTime() != null)
                    .map(SubTask::getDuration)
                    .reduce(Duration::plus).orElse(Duration.ofMinutes(0)));
            epic.setEndTime(itsSubTasks.stream()
                    .filter(sub -> sub.getStartTime() != null)
                    .map(SubTask::getEndTime)
                    .max(LocalDateTime::compareTo).orElse(null));
        }
    }

    public List<Task> getPrioritizedTasks() {
        // List проще по структуре и компактнее. Отправлять буду его
        // Конструктор строит лист в порядке, в котором элементы возвращает итератор коллекции оборачиваемой в него.
        // Поэтому сортированный порядок приоритета сохранится, а что что с этим списком будет дальше - нам не важно
        return new ArrayList<>(prioritySortedTaskSet);
    }


    @Override
    public List<Task> getHistory() {
        return history.getHistory();
    }
}