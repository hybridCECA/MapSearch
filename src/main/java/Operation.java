public class Operation {
    /*
        0: Modulo
        1: Xor
        2: Multiply
     */
    private int type;
    private final static int NUM_OPERATIONS = 3;

    public Operation(Operation operation) {
        this.type = operation.type;
    }

    public Operation() {
        this.type = 0;
    }

    public void next() {
        type = (type + 1) % NUM_OPERATIONS;
    }

    public boolean isLast() {
        return type == NUM_OPERATIONS - 1;
    }

    public int execute(int lhs, int rhs) {
        switch (type) {
            case 0:
                return lhs % rhs;
            case 1:
                return lhs ^ rhs;
            case 2:
                return lhs * rhs;
        }

        throw new RuntimeException("Operator type out of bounds");
    }

    @Override
    public String toString() {
        return "Operation{" +
                "type=" + type +
                '}';
    }
}
