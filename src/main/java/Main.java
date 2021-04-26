import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import static java.util.concurrent.Executors.newFixedThreadPool;

public class Main {
    private static final int MAX_OPERATIONS = 10;
    // Approximate number to process per thread
    private static final int BLOCK_SIZE = 10000000;
    // Maximum value to search to
    private static final int SEARCH_MAX = 1000;
    // Threads to schedule
    private static final int MAX_SCHEDULE = 64;
    // Start scheduling when this many threads are left
    private static final int MIN_SCHEDULE = MAX_SCHEDULE / 2;

    public static int[] inputs;
    public static Set<String> validOutputs;

    private static List<Operation> operationsList;

    private static int sumString(String s) {
        int sum = 0;
        for (char c : s.toCharArray()) {
            sum += c;
        }
        return sum;
    }

    // User function to set inputs
    private static void setup() {
        // Set inputs
        String[] months = {
                "jan", "feb", "mar", "apr", "may", "jun", "jul", "aug", "sep", "oct", "nov", "dec"
        };

        inputs = Arrays.stream(months)
                .mapToInt(Main::sumString)
                .toArray();

/*
        String[] days = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};
        inputs = Arrays.stream(days)
                .map(s -> s + s + s)
                .mapToInt(s -> s.charAt(12))
                .toArray();

 */


        // Set outputs
        validOutputs = new HashSet<>();
        // Allows numbers [0, inputs.length) in increasing order as outputs
        int[] numbers = IntStream.range(0, inputs.length).toArray();
        for (int i = 0; i < numbers.length; i++) {
            StringBuilder output = new StringBuilder();
            for (int j = 0; j < numbers.length; j++) {
                output.append(numbers[(i + j) % numbers.length]).append(" ");
            }
            validOutputs.add(output.toString());
        }
    }

    public static String validate(int[] value) {
        StringBuilder builder = new StringBuilder();
        for (int i : value) {
            builder.append(i % inputs.length).append(" ");
        }

        if (validOutputs.contains(builder.toString())) {
            return builder.toString();
        } else {
            return null;
        }
    }

    public static void main(String[] args) throws InterruptedException {
        IdManager.setup();
        setup();

        System.out.println("MapSearch running with");
        System.out.println("Inputs: " + Arrays.toString(inputs));
        System.out.println("Outputs: " + validOutputs.toString());

        Runtime runtime = Runtime.getRuntime();
        int threads = runtime.availableProcessors();
        System.out.println(threads + " thread(s)");
        ExecutorService service = newFixedThreadPool(threads);

        operationsList = new ArrayList<>();
        operationsList.add(new Operation());
        int blockId = 0;
        int minBlockId = IdManager.getMaxComputedId() + 1;

        // Main thread starting
        while (operationsList.size() <= MAX_OPERATIONS) {
            // Add new threads for each block
            int position = 1;
            while (position < SEARCH_MAX) {
                // Wait if enough are scheduled
                if (blockId > IdManager.getMaxComputedId() + MAX_SCHEDULE) {
                    IdManager.awaitId(blockId - MIN_SCHEDULE);
                }

                int start = position;

                // Compute increment so that block size is somewhat constant
                // Increment has a minimum of 2
                int degree = operationsList.size();
                int increment = (int) Math.pow(BLOCK_SIZE + Math.pow(position, degree), 1.0/degree) - position + 1;
                increment = Math.max(increment, 2);

                // Add increment with a maximum of SEARCH_MAX
                try {
                    position = Math.addExact(position, increment);
                } catch (ArithmeticException e) {
                    position = SEARCH_MAX;
                }
                position = Math.min(position, SEARCH_MAX);

                // Ensure correct blockId
                if (blockId >= minBlockId) {
                    Searcher searcher = new Searcher(deepOpCopy(operationsList), start, position, blockId);
                    service.execute(searcher);
                }
                blockId++;
            }

            incrementOperations();
        }

        service.shutdown();
        service.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
    }

    // Copy operations to an array
    private static Operation[] deepOpCopy(List<Operation> operationList) {
        return operationList.stream().map(Operation::new).toArray(Operation[]::new);
    }

    private static void incrementOperations() {
        if (operationsList.get(0).isLast()) {
            int index = 0;
            while (operationsList.get(index).isLast()) {
                if (index == operationsList.size() - 1) {
                    operationsList.add(new Operation());
                    break;
                } else {
                    operationsList.get(index).next();
                }
                index++;
            }
            operationsList.get(index).next();
        } else {
            operationsList.get(0).next();
        }
    }

}
