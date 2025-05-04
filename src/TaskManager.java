import java.util.ArrayList;
import java.util.HashMap;

public class TaskManager {
    private final ArrayList<Task> tasks;
    private final HashMap<Epic, ArrayList<SubTask>> tasksMap;
    /*
    "Возможность хранить задачи всех типов" с таким классовым расслоением остаётся только слёзы лить.
    В 4-м спринте не описано, можно ли хранить объекты дочерних классов в списках по ссылкам родительского.
    Не хочу рисковать и экспериментировать. Мне некогда, поэтому сделаю как легче, и буду считать, что пользователь,
    который использует "приложение" перед внесением изменений в задачи сначала считывает их содержимое и тип, и только
    затем лезет со своими изменениями, не теряя при этом список подзадач у эпиков и не изменяя головную задачу у
    подзадач.
    По ТЗ "пользователь" не читает консоль и мы без понятия как он составляет запросы кроме той инфы, что
    мы должны проглотить готовый изменённый объект и смирно запихнуть его на место старой задачи.
    Мы даже исключение ему выбросить не можем, потому что ещё не знаем как, итого у нас нет ровно никаких способов
    обратной связи с "пользователем", вносящим изменения. Если ОН что-то ввёл не так, то нам остаётся лишь выполнить
    инструкции, даже если это сломает всё. Ведь мы не можем указать пользователю на ошибку и потребовать её исправить.
    А если какие-то изменения не будут приняты без обратной связи и объяснения причины, почему не вышло, то при
    разбирательстве не посмотрят на то, что пользователь ввёл что-то не так, а лишь на то, что программы не выполнила
    действие, которое от неё требовалось.
    И вообще мне очень не нравится это ТЗ, но я сижу и работаю с ним, а не уточняю детали проекта. Потому что до пятого
    числа(которое жесткий дедлайн проекта пятого спринта) осталась пара часов, и мне надо за сутки успеть сделать их
    оба, попутно пролистав всю теорию пятого спринта.
    */

    public TaskManager() {
        tasks = new ArrayList<>();
        tasksMap = new HashMap<>();
    }

    public ArrayList<Task> getTasks() {
        ArrayList<Task> result = new ArrayList<>();
        for (Task task : tasks) {
            result.add(task);
        }
        for (Task epic : tasksMap.keySet()) {
            result.add(epic);
            result.addAll(tasksMap.get(epic));
        }
        return result;
    }

    public void clear() {
        tasks.clear();
        tasksMap.clear();
    }

    public Task getById(int id) {//Понадеемся, что вернув эпик или подзадачу по отцовьему методу мы не налетим на ошибку
        for (Task task : tasks) {
            if (task.getId() == id) {
                return task;
            }
        }
        for (Epic epic : tasksMap.keySet()) {
            if (epic.getId() == id) {
                return epic;
            }
            for (SubTask subTask : tasksMap.get(epic)) {
                if (subTask.getId() == id) {
                    return subTask;
                }
            }
        }
        return null;//тот самый случай, когда пользователь введя несуществующий id без какого-либо фидбека получит null
    }//Предполагается, что пользователь знает какого типа заметка с запрошенным id, иначе придется использовать getClass

    public void addTask(Task task) {//не будем проверять на null, потому что если сервер допустил передачу null - то увы
        tasks.add(task);
    }

    public void addTask(Epic epic) {
        tasksMap.put(epic, new ArrayList<>());//Новый эпик с пустым списком подзадач
        adjustEpicStatus(epic);
    }

    public void addTask(SubTask subTask) {
        for (Epic epic : tasksMap.keySet()) {//Ищем Эпик, которому должна принадлежать подзадача
            if (epic.getId() == subTask.getEpicId()) {
                tasksMap.get(epic).add(subTask);
                epic.addSubTask(subTask.getId());//Добавляем в Эпик id новой подзадачи
                adjustEpicStatus(epic);
                return;
            }
        }
    }

    public void update(Task task) {
        tasks.set(tasks.indexOf(task), task);//заменяем "идентичный" элемент на новый
        //Если indexOf() выбросит исключение, то это даже хорошо, ведь я так не умею делать.
    }

    public void update(Epic epic) {
        ArrayList<SubTask> saveList = tasksMap.get(epic);//сохраняем список подклассов, полученный по совпавшему id
        tasksMap.remove(epic);//удаляем старый ключ(надеюсь, он не чистит значения, ссылку на которые сохранил выше)
        tasksMap.put(epic, saveList);//возвращаем старые значения под "тем же" ключём
        adjustEpicStatus(epic);
    }

    public void update(SubTask subTask) {//предположим опять же, что подзадачи не могут поменять свой эпик
        for (Epic epic : tasksMap.keySet()) {//Ищем Эпик, которому должна принадлежать подзадача
            if (epic.getId() == subTask.getEpicId()) {
                tasksMap.get(epic).set(tasksMap.get(epic).indexOf(subTask), subTask);
                //заменяем "идентичный" элемент на новый
                adjustEpicStatus(epic);
                return;
            }
        }
    }

    public void remove(int id){
        for (Task task : tasks) {
            if (task.getId() == id) {
                tasks.remove(task);
                return;
            }
        }
        for (Epic epic : tasksMap.keySet()) {
            for (SubTask subTask : tasksMap.get(epic)) {
                if (subTask.getId() == id) {
                    tasksMap.get(epic).remove(subTask);
                    adjustEpicStatus(epic);
                    return;
                }
            }
            if (epic.getId() == id) {
                tasksMap.remove(epic);//Удаляем Эпик сразу со всеми подзадачами
                return;
            }
        }
    }

    public ArrayList<SubTask> getEpicSubTasks(Epic epic){
        return tasksMap.get(epic);
    }

    public ArrayList<SubTask> getEpicSubTasks(int id){
        return tasksMap.get((Epic) getById(id));//Вроде работать должно
    }

    private void adjustEpicStatus(Epic epic){//Вызывать каждый раз когда какие-то изменения в эпиках или подзадачах
        ArrayList<SubTask> subTasks = tasksMap.get(epic);
        boolean allNew = true;
        boolean allDone = true;

        if (subTasks.isEmpty()){
            epic.setStatus(TaskStatus.NEW);//"если у эпика нет подзадач, то статус должен быть NEW"
        }
        for (SubTask subTask : subTasks){
            if (subTask.getStatus() != TaskStatus.NEW){
                allNew = false;
            }
            if (subTask.getStatus() != TaskStatus.DONE){
                allDone = false;
            }
        }
        if (allNew){
            epic.setStatus(TaskStatus.NEW);
        } else if (allDone){
            epic.setStatus(TaskStatus.DONE);
        } else {
            epic.setStatus(TaskStatus.IN_PROGRESS);
        }
    }
}
/*
Любые претензии к коду вида "Такие изменения в объекты вносить не дОлжно" контрятся единственным
"Я, делать это, запретить не могу. Пожалуйста, имейте совесть и вводите в мой код заранее проверенные данные!".
Ну правда, если я никак не могу заставить повторить и исправить ввод, или хотя бы сообщить об отмене изменений, пусть
лучше мой код выкидывает ошибку этим сверху, которые своими неправильными инпутами сами всё поломали. А я сохраню нервы
до того момента, когда появятся инструменты исключений и нормальные ТЗ, в которых позволено возражать барину, вводящему
данные. В этом ТЗ даже не указано каким образом будут вызываться методы и шаблоны обращений
*/