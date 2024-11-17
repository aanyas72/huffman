import java.io.IOException;
import java.io.InputStream;
import java.util.TreeMap;

public class Compressor {

    public void countFrequencies(InputStream in, int headerFormat) throws IOException {
        TreeMap<Integer, Integer> bitCounts = new TreeMap<>();
        BitInputStream inputStream = new BitInputStream(in);

        int bits = inputStream.readBits(IHuffConstants.BITS_PER_WORD);
        while (bits != -1) {
            if (bitCounts.containsKey(bits)) {
                Integer total = bitCounts.get(bits);
                bitCounts.put(bits, total + 1);
            } else {
                bitCounts.put(bits, 1);
            }

            bits = inputStream.readBits(IHuffConstants.BITS_PER_WORD);
        }


    }
}
