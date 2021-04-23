import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import static java.util.concurrent.Executors.newFixedThreadPool;

public class Main {
    private static final int MAX_OPERATIONS = 3;
    // Approximate number to process per thread
    private static final int BLOCK_SIZE = 10000;
    // Maximum value to search to
    private static final int SEARCH_MAX = 1000;

    public static int[] inputs;
    public static Set<String> validOutputs;

    private static List<Operation> operationsList;

    // User function to set inputs
    private static void setInputs() {
        String[] days = {
                "Monday",
                "Tuesday",
                "Wednesday",
                "Thursday",
                "Friday",
                "Saturday",
                "Sunday"
        };

        inputs = Arrays.stream(days)
                .mapToInt(s -> (s + s + s).charAt(12))
                .toArray();
    }

    // User function to set valid outputs
    private static void setOutputs() {
        validOutputs = new HashSet<>();

        int[] numbers = new int[]{0, 1, 2, 3, 4, 5, 6};
        for (int i = 0; i < numbers.length; i++) {
            StringBuilder output = new StringBuilder();
            for (int j = 0; j < numbers.length; j++) {
                output.append(numbers[(i + j) % numbers.length]).append(" ");
            }
            validOutputs.add(output.toString());
        }
    }

    // User transformation function to result that occurs at end
    public static int finalTransformation(int input) {
        return input % 7;
    }

    public static void main(String[] args) throws InterruptedException {
        setInputs();
        setOutputs();

        Runtime runtime = Runtime.getRuntime();
        int threads = runtime.availableProcessors();
        ExecutorService service = newFixedThreadPool(threads);

        operationsList = new ArrayList<>();
        operationsList.add(new Operation());

        // Main thread starting
        while (operationsList.size() <= MAX_OPERATIONS) {
            // Add new threads for each block
            int position = 1;
            while (position < SEARCH_MAX) {
                int start = position;

                int degree = operationsList.size();
                int increment = (int) Math.pow(BLOCK_SIZE, degree);

                try {
                    position = Math.addExact(position, increment);
                } catch (ArithmeticException e) {
                    position = SEARCH_MAX;
                }
                position = Math.min(position, SEARCH_MAX);

                Searcher searcher = new Searcher(deepOpCopy(operationsList), start, position);
                service.execute(searcher);
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
