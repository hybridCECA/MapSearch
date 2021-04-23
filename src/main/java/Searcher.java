import java.util.Arrays;

public class Searcher implements Runnable{
    private Operation[] operations;
    private int[] operands;
    private int start;
    private int end;

    public Searcher(Operation[] operations, int start, int end) {
        this.operations = operations;
        this.start = start;
        this.end = end;

        operands = new int[operations.length];
        Arrays.fill(operands, start);
    }

    public void run() {
        System.out.println("Search start");
        System.out.println(Arrays.toString(operations));
        System.out.println("From " + start + " to " + end);

        operands[0]--;

        while (incrementOperands()) {
            compute();
        }
    }

    private void compute() {
        StringBuilder result = new StringBuilder();
        for (int input : Main.inputs) {
            int value = input;
            for (int i = 0; i < operations.length; i++) {
                value = operations[i].execute(value, operands[i]);
            }
            value = Main.finalTransformation(value);
            result.append(value).append(" ");
        }

        if (Main.validOutputs.contains(result.toString())) {
            System.out.println("FOUND: ");
            System.out.println(Arrays.toString(operations));
            System.out.println(Arrays.toString(operands));
        }
    }

    // Returns false if we are done
    private boolean incrementOperands() {
        int index = 0;
        if (operands[0] == end) {
            while (operands[index] == end) {
                operands[index] = start;
                index++;

                if (index == operands.length) {
                    return false;
                }
            }

            operands[index]++;
        } else {
            operands[0]++;
        }

        return true;
    }
}
