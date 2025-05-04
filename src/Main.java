public class Main {

    public static void main(String[] args) {
        System.out.println("Поехали!");

        TaskManager taskManager = new TaskManager();
        Task newTask = new Task("Помыть посуду",
                "Прийти на кухню и вымыть все грязные тарелки", TaskStatus.NEW);
        taskManager.addTask(newTask);
        System.out.println(taskManager.getTasks());//да, он выводит список ссылок, но хотя бы наглядно

        Epic newEpic = new Epic("Сделать домашку",
                "По очереди выполнить домашнее задание по всем предметам", TaskStatus.NEW);
        taskManager.addTask(newEpic);
        System.out.println(taskManager.getTasks());

        SubTask newSubTask = new SubTask("Сделать ДЗ по Матану",
                "Достать Тетрать, достать Демидовича, сделать номера ...", TaskStatus.NEW, 1);
        taskManager.addTask(newSubTask);
        //Если заменить epicId на тот, что не ссылается на Эпик, то подзадача не добавится - проверено
        //Согласно ТЗ это правильное выполнение добавления новой задачи, однако без исключений я не смогу отследить,
        //что элемент не был добавлен.
        System.out.println(taskManager.getTasks());

        newTask = new Task("Подмести пол",
                "Достать совок и метлу, смести мусор в совок, выкинуть мусор из совка", TaskStatus.NEW);
        taskManager.addTask(newTask);
        System.out.println(taskManager.getTasks());
        //задача с ID 3 добавилась в середину списка - это правильное поведение в моей реализации

        newEpic = new Epic("Успеть всё",
                "Осталось всего 22 часа, пора поторопиться и завершить этот модуль!", TaskStatus.IN_PROGRESS);
        //поставил в прогрессе, чтобы проверить, что автоподстройка статуса эпика работает
        taskManager.addTask(newEpic);
        System.out.println(taskManager.getTasks());
        System.out.println(taskManager.getById(4).getStatus());

        newSubTask = new SubTask("Сделать ДЗ по Диффурам",
                "Достать Тетрать, достать Филлипова, сделать номера ...", TaskStatus.NEW, 1);
        taskManager.addTask(newSubTask);
        System.out.println(taskManager.getTasks());
        System.out.println(((SubTask) taskManager.getById(5)).getEpicId());

        newSubTask = new SubTask("Быстро выполнить пятый спринт",
                "Поторопись! До конца света осталось всего ничего!", TaskStatus.NEW, 4);
        taskManager.addTask(newSubTask);
        System.out.println(taskManager.getTasks());
        System.out.println();

        newSubTask = (SubTask) taskManager.getById(5);
        //необходимое преобразование, потому что общей функцией можно вытащить только общий родительский класс
        System.out.println(newSubTask.getId());
        System.out.println(newSubTask.getName());
        System.out.println(newSubTask.getDescription());
        System.out.println(newSubTask.getStatus());
        System.out.println(newSubTask.getEpicId());

        newSubTask = new SubTask(5,"Сделать ДЗ по Диффурам",
                "Достать Тетрать, достать Филлипова, сделать номера ...", TaskStatus.IN_PROGRESS, 1);
        taskManager.update(newSubTask);
        System.out.println(taskManager.getById(5).getStatus());

        System.out.println();

        newEpic = (Epic) taskManager.getById(1);
        //необходимое преобразование, потому что общей функцией можно вытащить только общий родительский класс
        System.out.println(newEpic.getId());
        System.out.println(newEpic.getName());
        System.out.println(newEpic.getDescription());
        System.out.println(newEpic.getStatus());
        System.out.println(newEpic.getSubTasksIds());
        System.out.println();
    }
}
