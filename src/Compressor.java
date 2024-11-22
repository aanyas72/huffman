import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class Compressor {

    public int[] countFrequencies(InputStream in) throws IOException {
        BitInputStream inputStream = new BitInputStream(in);
        int[] frequencies = new int[IHuffConstants.ALPH_SIZE + 1];

        int bits = inputStream.readBits(IHuffConstants.BITS_PER_WORD);
        while (bits != -1) {
            frequencies[bits]++;

            bits = inputStream.readBits(IHuffConstants.BITS_PER_WORD);
        }

        // store the pseof
        frequencies[IHuffConstants.ALPH_SIZE] = 1;

        inputStream.close();
        return frequencies;
    }

    public TreeNode createHuffmanTree(int[] frequencies) {
        PriorityQueue queue = new PriorityQueue();

        // add all the frequencies to the queue
        for (int i = 0; i < IHuffConstants.ALPH_SIZE + 1; i++) {
            if (frequencies[i] > 0) {
                queue.enqueue(new TreeNode(i, frequencies[i]));
            }
        }

        // return the root of the huffman tree
        return queue.dequeue();
    }

    /**
     * Gets the number of bits in the encoded tree representation
     * @param node is not null
     */
    public int numOfTreeBits(TreeNode node) {
        // if the node is a leaf,
        if (node.isLeaf()) {
            return 1 + (1 + IHuffConstants.BITS_PER_WORD);
        }
        return 1 + numOfTreeBits(node.getLeft()) + numOfTreeBits(node.getRight());

    }

    public void preOrder(BitOutputStream stream, TreeNode node){
        if(node.isLeaf()){
            // leaf node, add a 1
            stream.writeBits(1,1);
            stream.writeBits(IHuffConstants.BITS_PER_INT + 1, node.getValue());

        } else{
            // internal node, add a 0 and continue traversing
            stream.writeBits(1,0);
            preOrder(stream, node.getLeft());
            preOrder(stream, node.getRight());
        }
    }

    public HashMap<Integer, String> createMap(TreeNode root) {
        HashMap<Integer, String> m = new HashMap<>();
        mapHelper(root, "", m);
        return m;
    }

    private void mapHelper(TreeNode node, String encodedVal, Map<Integer, String> map) {
        if(!node.isLeaf()){
            mapHelper(node.getLeft(), encodedVal + "0", map);
            mapHelper(node.getRight(), encodedVal + "1", map);
        } else {
            map.put(node.getValue(), encodedVal);
        }

    }

    /**
     * Gets the original number of bits in a file
     * @param frequencies, an array of length 257 that stores the frequency of characters in file
     * @return the number of bits in the original file
     */
    public int getOgBits(int[] frequencies) {
        int bits = 0;

        // go through the frequency array (which was created by reading in 8-bit chunks)
        for (int i = 0; i < IHuffConstants.ALPH_SIZE; i++) {
            bits += frequencies[i] * IHuffConstants.BITS_PER_WORD;
        }
        return bits;
    }

    public int getBitsAfterEncoding(int[] frequencies, HashMap<Integer, String> map) {
        int bit = 0;

        // go through each encoded value
        for (int key : map.keySet()) {
            if (key != IHuffConstants.PSEUDO_EOF) {
                String encodedVal = map.get(key);
                int frequency = frequencies[key];
                bit += frequency * encodedVal.length();
            }
        }
        return bit;
    }




}
