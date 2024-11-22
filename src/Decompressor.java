public class Decompressor {

    private PriorityQueue queue;

    public Decompressor() {
        queue = new PriorityQueue();
    }
    public TreeNode createTree(int value) {
        queue.enqueue(new TreeNode(value, 1));
        return queue.get();
    }

    public void recreateFile(BitOutputStream outputStream, TreeNode node) {

    }
}
