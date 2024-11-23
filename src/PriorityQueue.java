import java.util.LinkedList;

public class PriorityQueue {
    private LinkedList<TreeNode> queue;

    public PriorityQueue() {
        queue = new LinkedList<>();
    }

    /**
     * Add treenodes to the priority queue
     */
    public boolean enqueue(TreeNode node) {
        // store the node based on frequency
        int index = 0;
        while (index < queue.size()) {
            if (queue.get(index).compareTo(node) > 0) {
                queue.add(index, node);
                return true;
            }
            index++;
        }
        queue.add(node);
        return false;
    }

    public TreeNode dequeue() {
        while (queue.size() >= 2) {
            TreeNode left = queue.get(0);
            queue.remove();
            TreeNode right = queue.get(0);
            queue.remove();

            TreeNode node = new TreeNode(left,
                    left.getFrequency() + right.getFrequency(), right);
            enqueue(node);
        }
        return queue.get(0);
    }

    public TreeNode get() {
        return queue.get(0);
    }

    public void remove() {
        queue.remove(0);
    }

    public String toString() {
        return queue.toString();
    }

    public int size() {
        return queue.size();
    }
}
