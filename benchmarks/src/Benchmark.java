import java.util.*;

public class Benchmark {
    static int[] NUM_THREADS = new int[]{1, 10, 100, 1000, 10000};
    static int[] NUM_OPS = new int[]{1000, 10000, 100000};

    static int AVERAGE_OVER = 10;

    static TreeMap<Double, Op> WEIGHTS;
    static List<skipListInterface.SkipListInterface> sl = new ArrayList<>();

    static void setOpWeights() {
        WEIGHTS = new TreeMap<>();
        WEIGHTS.put(0.9, Op.Get);
        WEIGHTS.put(0.09, Op.Add);
        WEIGHTS.put(0.01, Op.Remove);
    }

    public static void main(String args[]) {
        setOpWeights();
        runAll();
        sl.add(new fineGrained.SkipList());
    }

    static void runAll() {
        for (int numThreads : NUM_THREADS) {
            for (int numOps: NUM_OPS) {
                makeData(numThreads, numOps);
            }
        }
    }

    static void makeData(int numThreads, int numOps) {
        List<Thread> threads = new ArrayList<>();

        for (int i = 0; i < numThreads; i++) {
            Thread t = new Thread(new Runner(numOps, WEIGHTS));
            threads.add(t);
        }
    }
}

class Runner implements Runnable {
    int numOps;
    WeightedOp wop;

    public Runner(int numOps, NavigableMap<Double, Op> weights) {
        this.numOps = numOps;
        this.wop = new WeightedOp(new Random(System.nanoTime()), weights);
    }

    @Override
    public void run() {
        for (int i = 0; i < numOps; i++) {
            switch (this.wop.next()) {
                case Remove:
                    break;
                case Add:
                    break;
                case Get:
                    break;
            }
        }
    }
}

enum Op {
    Get,
    Add,
    Remove,
};

class WeightedOp {
    private final NavigableMap<Double, Op> map;
    private final Random random;
    private double total = 0;

    public WeightedOp(Random random, NavigableMap<Double, Op> map) {
        this.random = random;
        this.map = map;

        for (Double w : this.map.keySet()) {
            total += w;
        }
    }

    public Op next() {
        double value = random.nextDouble() * total;
        return map.ceilingEntry(value).getValue();
    }
}