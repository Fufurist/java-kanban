package managers;

import taskunits.Epic;
import taskunits.SubTask;
import taskunits.Task;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class FileBackedTaskManager extends InMemoryTaskManager {
    //
    private Path saveFile;

    public Path getSaveFile() {
        return saveFile;
    }

    public void setSaveFile(Path saveFile) {
        this.saveFile = saveFile;
    }

    public FileBackedTaskManager(Path saveFile) {
        super();
        this.saveFile = saveFile;
    }

    @Override
    public void clearAll() {
        super.clearAll();
        save();
    }

    @Override
    public int addTask(Task task) {
        int result = super.addTask(task);
        save();
        return result;
    }

    @Override
    public int addEpic(Epic epic) {
        int result = super.addEpic(epic);
        save();
        return result;
    }

    @Override
    public int addSubTask(SubTask subTask) {
        int result = super.addSubTask(subTask);
        save();
        return result;
    }

    @Override
    public boolean updateTask(Task task) {
        boolean result = super.updateTask(task);
        save();
        return result;
    }

    @Override
    public boolean updateEpic(Epic epic) {
        boolean result = super.updateEpic(epic);
        save();
        return result;
    }

    @Override
    public boolean updateSubTask(SubTask subTask) {
        boolean result = super.updateSubTask(subTask);
        save();
        return result;
    }

    @Override
    public boolean removeTask(int id) {
        boolean result = super.removeTask(id);
        save();
        return result;
    }

    @Override
    public boolean removeEpic(int id) {
        boolean result = super.removeEpic(id);
        save();
        return result;
    }

    @Override
    public boolean removeSubTask(int id) {
        boolean result = super.removeSubTask(id);
        save();
        return result;
    }

    @Override
    public void clearTasks() {
        super.clearTasks();
        save();
    }

    @Override
    public void clearSubTasks() {
        super.clearSubTasks();
        save();
    }

    @Override
    public void clearEpics() {
        super.clearEpics();
        save();
    }

    //Пусть каждое сохранение перезаписывает файл полностью, медленно, но как ещё?
    private void save() throws ManagerSaveException {
        try (BufferedWriter buffer =
                     Files.newBufferedWriter(saveFile, StandardCharsets.UTF_8, StandardOpenOption.CREATE)) {
            buffer.write("id,type,name,status,description,epic");
            buffer.newLine(); //По идее для правильной работы буфера надо делать именно так
            // Поскольку в ТЗ не сказано, что задачи в файле должны быть как-либо упорядочены, предположу, что создавать
            // файлы может только и только этот менеджер. А значит, я могу задать свой порядок сохранения и удобного
            // мне чтения (перед всеми подзадачами будут перечислены все эпики, к которым привяжутся эти подзадачи)
            for (Task task : getTasks()) {
                buffer.write(task.toString());
                buffer.newLine();
            }
            for (Epic epic : getEpics()) {
                buffer.write(epic.toString());
                buffer.newLine();
            }
            for (SubTask subTask : getSubTasks()) {
                buffer.write(subTask.toString());
                buffer.newLine();
            }
        } catch (IOException e) {
            throw new ManagerSaveException("Ещё не умеем обрабатывать такое", e);
        }
    }


    public static FileBackedTaskManager loadFromFile(Path path) {
        Path tmp;
        try {
            tmp = Files.createTempFile("kanban-tmp", null);
        } catch (IOException e) {
            return new FileBackedTaskManager(path);
        }
        FileBackedTaskManager result = new FileBackedTaskManager(tmp);
        try (BufferedReader buffer = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            String line = buffer.readLine();
            if (line != null && line.equals("id,type,name,status,description,epic")) {
                line = buffer.readLine();
                while (line != null) {
                    String[] words = line.split(",");
                    switch (words[1]) {
                        case "TASK":
                            result.addTask(Task.toTask(line));
                            break;
                        case "EPIC":
                            result.addEpic(Epic.toEpic(line));
                            break;
                        case "SUBTASK":
                            result.addSubTask(SubTask.toSubTask(line));
                            break;
                    }
                    line = buffer.readLine();
                }
            } else System.out.println("It makes no sense!");//Вернёт пустой
        } catch (IOException e) {
            System.out.println("To be or not to be?");
        }
        //все сохранения шли во временный файл, чтобы не потерять данные с того файла, который читаем
        result.setSaveFile(path);
        return result;
    }
}
