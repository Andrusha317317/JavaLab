package ua.regesha.mkr;

public class NumberListImpl<T> {

    private Node<T> head;
    private int size = 0;

    public NumberListImpl() {
    }

    // Додавання елемента (Кільцевий двонаправлений список)
    public void add(T data) {
        Node<T> newNode = new Node<>(data);
        if (head == null) {
            head = newNode;
            head.setNext(head);
            head.setPrev(head);
        } else {
            Node<T> tail = head.getPrev();
            tail.setNext(newNode);
            newNode.setPrev(tail);
            newNode.setNext(head);
            head.setPrev(newNode);
        }
        size++;
    }

    // Завдання 4 (C7=6): Побітове OR (АБО)
    public static NumberListImpl<Integer> bitwiseOr(NumberListImpl<Integer> a, NumberListImpl<Integer> b) {
        long valA = listToLong(a);
        long valB = listToLong(b);
        long resVal = valA | valB; // Операція OR

        NumberListImpl<Integer> result = new NumberListImpl<>();
        String binaryString = Long.toBinaryString(resVal);

        for (char c : binaryString.toCharArray()) {
            result.add(Character.getNumericValue(c));
        }
        return result;
    }

    // Завдання 3 та 5: Зміна системи з двійкової (0) на трійкову (1)
    public void changeScale() {
        long value = listToLong((NumberListImpl<Integer>) this);
        this.head = null;
        this.size = 0;

        if (value == 0) {
            add((T) Integer.valueOf(0));
            return;
        }

        // Переводимо в трійкову систему
        String ternaryString = Long.toString(value, 3);
        for (char c : ternaryString.toCharArray()) {
            add((T) Integer.valueOf(Character.getNumericValue(c)));
        }
    }

    // Допоміжний метод: перетворює список у число
    private static long listToLong(NumberListImpl<Integer> list) {
        if (list.head == null) return 0;
        Node<Integer> current = list.head;
        StringBuilder sb = new StringBuilder();
        do {
            sb.append(current.getData());
            current = current.getNext();
        } while (current != list.head);
        return Long.parseLong(sb.toString(), 2);
    }

    @Override
    public String toString() {
        if (head == null) return "[]";
        StringBuilder sb = new StringBuilder("[");
        Node<T> curr = head;
        do {
            sb.append(curr.getData());
            curr = curr.getNext();
            if (curr != head) sb.append(", ");
        } while (curr != head);
        sb.append("]");
        return sb.toString();
    }
}