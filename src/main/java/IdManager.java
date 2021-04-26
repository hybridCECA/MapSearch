import java.io.*;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.CountDownLatch;

public class IdManager {
    public static final String filename = "max_computed_id.txt";
    private static int maxComputedId;
    private static SortedSet<Integer> computedIds;
    private static CountDownLatch latch;
    private static int idWaitingFor;

    public static void setup() {
        computedIds = new TreeSet<>();
        File file = new File(filename);

        try {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line = reader.readLine();
            maxComputedId = Integer.parseInt(line);
            reader.close();
        } catch (IOException | NumberFormatException e) {
            maxComputedId = -1;
        }
    }

    public static void markIdComplete(int id) {
        computedIds.add(id);

        if (id >= idWaitingFor && latch != null && latch.getCount() > 0) {
            latch.countDown();
        }

        // Check for flush until max
        int max = computedIds.last();
        if (computedIds.size() == max - maxComputedId) {
            maxComputedId = max;
            computedIds.clear();
            write();
        } else {
            // Check for flush until id
            SortedSet<Integer> rangeSet = computedIds.headSet(id + 1);
            if (rangeSet.size() == id - maxComputedId) {
                maxComputedId = id;
                rangeSet.clear();
                write();
            }
        }
    }

    private static void write() {
        File file = new File(filename);

        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(file));
            writer.write(Integer.toString(maxComputedId));
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static int getMaxComputedId() {
        return maxComputedId;
    }

    // Waits until id is done
    public static void awaitId(int id) {
        idWaitingFor = id;
        latch = new CountDownLatch(1);
        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
