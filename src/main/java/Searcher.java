import java.util.Arrays;

public class Searcher implements Runnable{
    private Operation[] operations;
    private int[] operands;
    private int start;
    private int end;
    private int blockId;

    public Searcher(Operation[] operations, int start, int end, int blockId) {
        this.operations = operations;
        this.start = start;
        this.end = end;
        this.blockId = blockId;

        operands = new int[operations.length];
        Arrays.fill(operands, 1);
        operands[0] = start;
    }

    public void run() {
        System.out.println("ID: " + blockId + " " + Arrays.toString(operations) + " " + start + " to " + end);

        operands[0]--;

        while (incrementOperands()) {
            compute();
        }

        IdManager.markIdComplete(blockId);
    }

    private void compute() {
        int[] result = Arrays.copyOf(Main.inputs, Main.inputs.length);

        for (int i = 0; i < Main.inputs.length; i++) {
            for (int j = 0; j < operations.length; j++) {
                result[i] = operations[j].execute(result[i], operands[j]);
            }
        }

        String value = Main.validate(result);
        if (value != null) {
            System.out.println("FOUND: " + Arrays.toString(operations) + " " + Arrays.toString(operands) + " " + value);
        }
    }

    // Returns false if we are done
    private boolean incrementOperands() {
        int index = 0;
        boolean setFirst = false;
        while (operands[index] == end - 1) {
            setFirst = true;
            if (index != 0) {
                operands[index] = 1;
            }
            index++;

            if (index == operands.length) {
                return false;
            }
        }

        operands[index]++;

        if (setFirst) {
            operands[0] = allNonFirstLessThanStart() ? start : 1;
        }

        return true;
    }

    private boolean allNonFirstLessThanStart() {
        boolean valid = true;
        for (int i = 1; i < operands.length; i++) {
            valid = valid && operands[i] < start;
        }

        return valid;
    }
}
