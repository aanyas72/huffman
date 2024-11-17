import java.util.LinkedList;

public class PriorityQueue {
    private LinkedList<TreeNode> queue;

    public PriorityQueue() {
    }

    public boolean addQ(TreeNode node) {

        if (queue == null) {
            queue = new LinkedList<>();
            queue.add(node);
        } else {
            // store the node based on frequency
            int index = 0;
            while(index < queue.size()){
                if(queue.get(index).compareTo(node) > 0) {
                    queue.add(index, node);
                    return true;
                }
                index++;
            }
            queue.add(node);
        }
        return false;
    }

    public void dequeue() {
        while (queue.size() >= 2) {
            TreeNode left = queue.get(0);
            queue.remove();
            TreeNode right = queue.get(0);
            queue.remove();

            TreeNode node = new TreeNode(left,
                    left.getFrequency() + right.getFrequency(), right);
            addQ(node);
        }
    }

    public void remove() {
        queue.remove(0);
    }

    public String toString() {
        return queue.toString();
    }


}
