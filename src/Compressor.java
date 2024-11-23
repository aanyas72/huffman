import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class Compressor {

    /**
     * Counts the frequencies of the values in the file and puts them into a frequency array
     * where each index represents the ascii value and the value at the index represents the
     * amount of times this value occurs.
     */
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

    /**
     * Creates a huffman tree from the frequency array
     */
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
     *
     * @param node is not null
     */
    public int numOfTreeBits(TreeNode node) {
        // if the node is a leaf,
        if (node.isLeaf()) {
            return 1 + (1 + IHuffConstants.BITS_PER_WORD);
        }
        return 1 + numOfTreeBits(node.getLeft()) + numOfTreeBits(node.getRight());

    }

    /**
     * Write the encoded values of the tree into the header so it can be reconstructed
     */
    public void preOrder(BitOutputStream stream, TreeNode node) {
        if (node.isLeaf()) {
            // leaf node, add a 1
            stream.writeBits(1, 1);
            stream.writeBits(IHuffConstants.BITS_PER_WORD + 1, node.getValue());

        } else {
            // internal node, add a 0 and continue traversing
            stream.writeBits(1, 0);
            preOrder(stream, node.getLeft());
            preOrder(stream, node.getRight());
        }
    }

    /**
     * Create a map using the tree that maps the ascii representation of the value (Integer) to the
     * encoded value (String)
     */
    public HashMap<Integer, String> createMap(TreeNode root) {
        HashMap<Integer, String> m = new HashMap<>();
        mapHelper(root, "", m);
        return m;
    }

    /**
     * Helper method to create the encoding values for the value.
     */
    private void mapHelper(TreeNode node, String encodedVal, Map<Integer, String> map) {
        if (!node.isLeaf()) {
            mapHelper(node.getLeft(), encodedVal + "0", map);
            mapHelper(node.getRight(), encodedVal + "1", map);
        } else {
            map.put(node.getValue(), encodedVal);
        }

    }

    /**
     * Gets the original number of bits in a file
     *
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

    /**
     * Find the number of bits in the file body after compressing the file
     */
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

    /**
     * Helper method to write the encoded value of the actual value onto the file
     */
    public void writeData(BitOutputStream outputStream, String encodedValue) {
        for (int i = 0; i < encodedValue.length(); i++) {
            if (encodedValue.charAt(i) == '0') {
                outputStream.writeBits(1, 0);
            } else {
                outputStream.writeBits(1, 1);
            }
        }
    }


}
