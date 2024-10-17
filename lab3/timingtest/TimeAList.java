package timingtest;
import edu.princeton.cs.algs4.Stopwatch;

import static java.lang.Math.pow;

/**
 * Created by hug.
 */
public class TimeAList {
    private static void printTimingTable(AList<Integer> Ns, AList<Double> times, AList<Integer> opCounts) {
        System.out.printf("%12s %12s %12s %12s\n", "N", "time (s)", "# ops", "microsec/op");
        System.out.printf("------------------------------------------------------------\n");
        for (int i = 0; i < Ns.size(); i += 1) {
            int N = Ns.get(i);
            double time = times.get(i);
            int opCount = opCounts.get(i);
            double timePerOp = time / opCount * 1e6;
            System.out.printf("%12d %12.2f %12d %12.2f\n", N, time, opCount, timePerOp);
        }
    }

    public static void main(String[] args) {
        timeAListConstruction();
    }

    public static void timeAListConstruction() {
        // TODO: YOUR CODE HERE
        AList<Integer> Ns = new AList<>();
        AList<Double> times = new AList<>();
        AList<Integer> opCounts = new AList<>();
        for (int i = 0; i < 8; ++i){
            int n = (int) (pow(2, i) * 1000);
            Ns.addLast(n);
            opCounts.addLast(n);
            AList<Integer> tmp = new AList<>();
            Stopwatch stopwatch = new Stopwatch();
            for (int j = 0; j < n; ++j) {
                tmp.addLast(j);
            }
            times.addLast(stopwatch.elapsedTime());
        }
        printTimingTable(Ns, times, opCounts);
    }
}
