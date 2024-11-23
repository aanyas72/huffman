import java.io.IOException;

public class Decompressor {

    private PriorityQueue queue;

    public Decompressor() {
        queue = new PriorityQueue();
    }

    /**
     * Helper method to recreate the huffman tree by adding new nodes to the priority queue
     */
    public TreeNode createTree(int value, int frequency) {
        queue.enqueue(new TreeNode(value, frequency));
        return queue.dequeue();
    }

    /**
     * Helper method to decode the huffman tree from the encoded data
     */
    public TreeNode stfTreeHelper(BitInputStream inputStream) throws IOException {
        int typeOfNode = inputStream.readBits(IHuffConstants.BITS_PER_INT);
        TreeNode currNode = null;

        while (typeOfNode != -1) {
            if (typeOfNode == 1) {
                int value = inputStream
                        .readBits(IHuffConstants.BITS_PER_WORD + 1);
                currNode = createTree(value, 0);
            } else {
                currNode = new TreeNode(-1, 0);
                currNode.setLeft(stfTreeHelper(inputStream));
                currNode.setRight(stfTreeHelper(inputStream));
            }


            typeOfNode = inputStream.readBits(1);
        }


        return currNode;
    }


    /**
     * Read one bit at a time and decode the tree
     */
    public int decode(BitInputStream inputStream, BitOutputStream outputStream,
                      TreeNode node) throws IOException {
        TreeNode currentNode = node;
        boolean done = false;
        int countBits = 0;
        while (!done) {
            int bit = inputStream.readBits(1);
            if (bit == -1) {
                throw new IOException("Error reading compressed file. \n" +
                        "unexpected end of input. No PSEUDO_EOF value.");
            } else {
                if (bit == 0) {
                    currentNode = currentNode.getLeft();
                } else if (bit == 1) {
                    currentNode = currentNode.getRight();
                }
                if (node.isLeaf()) {
                    if (node.getValue() == IHuffConstants.PSEUDO_EOF) {
                        done = true;
                    } else {
                        outputStream.writeBits(IHuffConstants.BITS_PER_WORD, node.getValue());
                        countBits += IHuffConstants.BITS_PER_WORD;
                    }
                    currentNode = node;
                }
            }
        }
        return countBits;

    }
}
