import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static java.util.concurrent.Executors.newFixedThreadPool;

public class Main {
    private static final int MAX_OPERATIONS = 2;
    private static final int BLOCK_SIZE = 1000;
    private static final int SEARCH_MAX = 1000;

    public static int[] inputs;
    public static Set<String> validOutputs;

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

    public static int finalTransformation(int input) {
        return input % 7;
    }

    public static void main(String[] args) throws InterruptedException {
        setInputs();
        setOutputs();

        Runtime runtime = Runtime.getRuntime();
        ExecutorService service = newFixedThreadPool(runtime.availableProcessors());

        List<Operation> operationsList = new ArrayList<>();
        operationsList.add(new Operation());

        while (operationsList.size() <= MAX_OPERATIONS) {
            int position = 1;
            while (position < SEARCH_MAX) {
                int start = position;
                int degree = operationsList.size();
                int increment = (int) Math.pow(BLOCK_SIZE, degree);
                position += increment;
                position = Math.min(position, SEARCH_MAX);

                Searcher searcher = new Searcher(deepOpCopy(operationsList), start, position);
                service.execute(searcher);
            }

            // Increment
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

        service.shutdown();
        service.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
    }

    private static Operation[] deepOpCopy(List<Operation> operationList) {
        return operationList.stream().map(Operation::new).toArray(Operation[]::new);
    }

}
