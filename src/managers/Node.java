package managers;

import taskunits.Task;

class Node { //класс не публичный, потому что я хочу, чтоб им пользовался только InMemoryHistoryManager в этом же пакете
    private Node prev;
    private Task info; //допустим, при просмотре истории нам не важен тип задачи.
    private Node next;

    Node(Task info) {
        prev = null;
        this.info = info;
        next = null;
    }

    public Task getInfo() { //Если я намеренно делаю package private класс, то как в нём работают public методы...?
        return this.info;
    }

    public Node getPrev() {
        return prev;
    }

    public Node getNext() {
        return next;
    }

    public void setPrev(Node prev) {
        this.prev = prev;
    }

    public void setNext(Node next) {
        this.next = next;
    }

    @Override
    public int hashCode() { //для конкретной задачи не нужен, но пусть будет
        return info.hashCode();
    }

    @Override
    public boolean equals(Object obj) { //для поиска по значению в мапе, хотя я такой проводить не буду
        if (obj == null) return false;
        if (obj == this) return true;
        if (!(obj instanceof Node)) return false;
        Node node = (Node) obj;

        return info.equals(node.getInfo());
    }

}
