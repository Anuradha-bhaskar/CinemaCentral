
public class LList {
    // Node class for linked List implementation

    Node head = null;

    // Insert Method of linked list which takes Theater as input
    public void insert(Theater Th) {
        Node newNode = new Node(Th);
        if (head == null) {
            head = newNode;
        } else {
            Node current = head;
            while (current.next != null) {
                current = current.next;
            }
            current.next = newNode;
        }
    }
}