public class QueueTester {
    public static void main(String[] args) {
        PriorityQueue queue = new PriorityQueue();
        TreeNode t1 = new TreeNode(1, 1);
        queue.enqueue(t1);
        System.out.println(queue);
        TreeNode t2 = new TreeNode(2, 2);
        queue.enqueue(t2);
        System.out.println(queue);
        TreeNode t3 = new TreeNode(3, 0);
        queue.enqueue(t3);
        System.out.println(queue);
        TreeNode t4 = new TreeNode(4, 1);
        queue.enqueue(t4);
        System.out.println(queue);

        queue.dequeue();
        queue.dequeue();
        queue.dequeue();

        System.out.println(queue);
//        SimpleHuffProcessor huffProcessor = new SimpleHuffProcessor();
//        huffProcessor.preprocessCompress(new In)
    }
}
