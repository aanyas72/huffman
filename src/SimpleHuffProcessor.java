/*  Student information for assignment:
 *
 *  On <MY|OUR> honor, <NAME1> and <NAME2), this programming assignment is <MY|OUR> own work
 *  and <I|WE> have not provided this code to any other student.
 *
 *  Number of slip days used:
 *
 *  Student 1 (Student whose Canvas account is being used)
 *  UTEID:
 *  email address:
 *  Grader name:
 *
 *  Student 2
 *  UTEID:
 *  email address:
 *
 */

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;

public class SimpleHuffProcessor implements IHuffProcessor {

    private IHuffViewer myViewer;
    private int[] frequencyArr;
    private HashMap<Integer, String> freqMap = new HashMap<>();
    // stores the root of the Huffman Tree
    private TreeNode root;

    private int countAfterEncoding;
    private int ogBits;
    private int headerFormat;
    private boolean isPreProcessed;

    /**
     * Preprocess data so that compression is possible ---
     * count characters/create tree/store state so that
     * a subsequent call to compress will work. The InputStream
     * is <em>not</em> a BitInputStream, so wrap it int one as needed.
     * @param in is the stream which could be subsequently compressed
     * @param headerFormat a constant from IHuffProcessor that determines what kind of
     * header to use, standard count format, standard tree format, or
     * possibly some format added in the future.
     * @return number of bits saved by compression or some other measure
     * Note, to determine the number of
     * bits saved, the number of bits written includes
     * ALL bits that will be written including the
     * magic number, the header format number, the header to
     * reproduce the tree, AND the actual data.
     * @throws IOException if an error occurs while reading from the input file.
     */
    public int preprocessCompress(InputStream in, int headerFormat) throws IOException {
        Compressor compressor = new Compressor();
        this.headerFormat = headerFormat;

        // create an array storing the frequencies of every bit
        frequencyArr = compressor.countFrequencies(in);

        // get the root of the huffman tree
        root = compressor.createHuffmanTree(frequencyArr);

        // create a frequency map
        freqMap = compressor.createMap(root);

        ogBits = compressor.getOgBits(frequencyArr);
        countAfterEncoding = compressor.getBitsAfterEncoding(frequencyArr, freqMap);

        // add the number of bits for the magic number and STF/STC number
        countAfterEncoding += (IHuffConstants.BITS_PER_INT + IHuffConstants.BITS_PER_INT);

        // add the number of bits of the header data
        if (headerFormat == IHuffConstants.STORE_COUNTS){
            countAfterEncoding += IHuffConstants.ALPH_SIZE * IHuffConstants.BITS_PER_INT;
        } else if (headerFormat == IHuffConstants.STORE_TREE) {
            countAfterEncoding += compressor.numOfTreeBits(root) + IHuffConstants.BITS_PER_INT;
        }

        // add bits for pseof
        countAfterEncoding += freqMap.get(IHuffConstants.PSEUDO_EOF).length();

        isPreProcessed = true;
        return ogBits - countAfterEncoding;
    }

    /**
	 * Compresses input to output, where the same InputStream has
     * previously been pre-processed via <code>preprocessCompress</code>
     * storing state used by this call.
     * <br> pre: <code>preprocessCompress</code> must be called before this method
     * @param in is the stream being compressed (NOT a BitInputStream)
     * @param out is bound to a file/stream to which bits are written
     * for the compressed file (not a BitOutputStream)
     * @param force if this is true create the output file even if it is larger than the input file.
     * If this is false do not create the output file if it is larger than the input file.
     * @return the number of bits written.
     * @throws IOException if an error occurs while reading from the input file or
     * writing to the output file.
     */
    public int compress(InputStream in, OutputStream out, boolean force) throws IOException {
        if (!isPreProcessed) {
            throw new IllegalStateException("Must preprocess to compress!");
        }
        if (!force && countAfterEncoding > ogBits){
            myViewer.showError("Compressed file is larger than original file!");
        }
        BitOutputStream outputStream = new BitOutputStream(out);

        // write magic number
        outputStream.writeBits(IHuffConstants.BITS_PER_INT, IHuffConstants.MAGIC_NUMBER);

        // write header format
        outputStream.writeBits(IHuffConstants.BITS_PER_INT, headerFormat);

        // write header data based on given format
        if (headerFormat == IHuffConstants.STORE_COUNTS) {
            for (int k = 0; k < IHuffConstants.ALPH_SIZE; k++) {
                outputStream.writeBits(BITS_PER_INT, frequencyArr[k]);
            }
        } else if (headerFormat == IHuffConstants.STORE_TREE) {
            Compressor compressor = new Compressor();
            // writing the number of bits the tree takes up
            outputStream.writeBits(BITS_PER_INT, compressor.numOfTreeBits(root));

            // want to rebuild text, so do pre-order traversal
            compressor.preOrder(outputStream, root);
        }

        // encode the text and put into the body
        BitInputStream inputStream = new BitInputStream(in);
        int nextBit = inputStream.readBits(IHuffConstants.BITS_PER_WORD);
        while (nextBit != -1) {
            String encodedVal = freqMap.get(nextBit);
            writeData(outputStream, encodedVal);
            nextBit = inputStream.readBits(IHuffConstants.BITS_PER_WORD);
        }

        writeData(outputStream, freqMap.get(IHuffConstants.PSEUDO_EOF));
        System.out.println(freqMap);

        inputStream.close();
        outputStream.close();

        return countAfterEncoding;
    }

    public void writeData(BitOutputStream outputStream, String encodedValue) {
        for (int i = 0; i < encodedValue.length(); i++) {
            if (encodedValue.charAt(i) == 0) {
                outputStream.writeBits(1, 0);
            } else {
                outputStream.writeBits(1, 1);
            }
        }
    }

    /**
     * Uncompress a previously compressed stream in, writing the
     * uncompressed bits/data to out.
     * @param in is the previously compressed data (not a BitInputStream)
     * @param out is the uncompressed file/stream
     * @return the number of bits written to the uncompressed file/stream
     * @throws IOException if an error occurs while reading from the input file or
     * writing to the output file.
     */
    public int uncompress(InputStream in, OutputStream out) throws IOException {
	        BitInputStream inputStream = new BitInputStream(in);
            BitOutputStream outputStream = new BitOutputStream(out);

            // Check if the file is a huff file
            if (inputStream.readBits(IHuffConstants.BITS_PER_INT) != IHuffConstants.MAGIC_NUMBER) {
                myViewer.showError("Error reading compressed file.\n" +
                        "File did not start with the huff magic number.");
            }

            // Check which header the file has
            int headerFormat = inputStream.readBits(IHuffConstants.BITS_PER_INT);
            Compressor compressor = new Compressor();
            Decompressor decompressor = new Decompressor();

            // Reconstruct the tree
            if (headerFormat == IHuffConstants.STORE_TREE) {
                int typeOfNode = inputStream.readBits(BITS_PER_INT);
                while (typeOfNode != -1) {
                    if (typeOfNode == 1) {
                        int value = inputStream
                                .readBits(IHuffConstants.BITS_PER_WORD + 1);
                        root = decompressor.createTree(value);
                    }
                    typeOfNode = inputStream.readBits(1);
                }

            } else if (headerFormat == IHuffConstants.STORE_COUNTS) {
                for (int i = 0 ; i < IHuffConstants.ALPH_SIZE; i++) {
                    int freqInOgFile = inputStream.readBits(IHuffConstants.BITS_PER_INT);
                    frequencyArr[i] = freqInOgFile;
                }
                root = compressor.createHuffmanTree(frequencyArr);


            }




            inputStream.close();
            outputStream.close();
	        return 0;
    }

    // read 1 bit at a time and walk tree
    private int decode(BitInputStream inputStream, BitOutputStream outputStream,
                       TreeNode node) throws IOException {
        // get ready to walk tree, start at root
//        boolean done = false;
//        while(!done) {
//            int bit = inputStream.readBits(1);
//            if (bit == -1) {
//                throw new IOException("Error reading compressed file. \n" +
//                        "unexpected end of input. No PSEUDO_EOF value.");
//            } else {
//                if (bit == 0) {
//                    decode(inputStream, node.getLeft());
//                }  else if (bit == 1) {
//                    decode(inputStream, node.getRight());
//                }
//               if (node.isLeaf()) {
//                   if (node.getValue() == IHuffConstants.PSEUDO_EOF) {
//                       done = true;
//                   } else {
//                       outputStream.writeBits(IHuffConstants.BITS_PER_WORD, node.getValue());
//                   }
//               }
//            }
//        }
//        return node;
        return 0;
    }

    public void setViewer(IHuffViewer viewer) {
        myViewer = viewer;
    }

    private void showString(String s){
        if (myViewer != null) {
            myViewer.update(s);
        }
    }
}
