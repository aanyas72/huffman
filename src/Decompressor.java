public class Decompressor {

    private PriorityQueue queue;

    public Decompressor() {
        queue = new PriorityQueue();
    }
    public TreeNode createTree(int value) {
        queue.enqueue(new TreeNode(value, 1));
        return queue.get();
    }

    // read 1 bit at a time and walk tree
    private int decode(BitInputStream inputStream, BitOutputStream outputStream,
                       TreeNode node) throws IOException {
        TreeNode currentNode = root;
        boolean done = false;
        int countBits = 0;
        while(!done) {
            int bit = inputStream.readBits(1);
            if (bit == -1) {
                throw new IOException("Error reading compressed file. \n" +
                        "unexpected end of input. No PSEUDO_EOF value.");
            } else {
                if (bit == 0) {
                    currentNode = currentNode.getLeft());
                }  else if (bit == 1) {
                    currentNode = currentNode.getRight());
                }
               if (node.isLeaf()) {
                   if (node.getValue() == IHuffConstants.PSEUDO_EOF) {
                       done = true;
                   } else {
                       outputStream.writeBits(IHuffConstants.BITS_PER_WORD, node.getValue());
                       countBits += IHuffConstants.BITS_PER_WORD;
                       currentNode = root;
                   }
               }
            }
        }
        return countBits;

    }
}
