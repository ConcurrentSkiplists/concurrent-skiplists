import java.util.*;

public class Benchmark {
    static int[] NUM_THREADS = new int[]{1, 10, 100, 1000, 10000};
    static int[] NUM_OPS = new int[]{1000, 10000, 100000};
    static TreeMap<Double, Op> t;
    static WeightedOp weightedOp;

    static List<skipListInterface.SkipListInterface> sl = new ArrayList<>();

    static void setOpWeights() {
        t = new TreeMap<>();
        t.put(0.9, Op.Get);
        t.put(0.09, Op.Add);
        t.put(0.01, Op.Remove);
        weightedOp = new WeightedOp(new Random(System.nanoTime()), t);
    }

    public static void main(String args[]) {
        setOpWeights();
        runAll();
        sl.add(new fineGrained.SkipList());
    }

    static void runAll() {
        for (int numThreads : NUM_THREADS) {
            for (int numOps: NUM_OPS) {
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