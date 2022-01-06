package cw.feedhandler.benchmark;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.TimeValue;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class StringCompareBenchmark {
    private static final int RANDOM_SEED = 123456789;
    private static final String TEST_1 = "random string";
    private static final String TEST_2 = "is random str";
    private static final String TEST_3 = "another string";
    private static final String[] TESTS = {TEST_1, TEST_2, TEST_3};

    /**
     * Benchmark                                        Mode  Cnt  Score    Error  Units
     * StringCompareBenchmark.stringEquals              avgt   10  0.016 ±  0.001  ms/op
     * StringCompareBenchmark.stringGetFromMap          avgt   10  0.018 ±  0.001  ms/op
     * StringCompareBenchmark.stringLengthAndThenEqual  avgt   10  0.017 ±  0.001  ms/op
     */

    @Test
    public void runBenchmarks() throws Exception {
        Options options = new OptionsBuilder()
                .include(this.getClass().getName() + ".*")
                .mode(Mode.AverageTime)
                .warmupTime(TimeValue.seconds(1))
                .warmupIterations(10)
                .threads(1)
                .measurementIterations(10)
                .forks(1)
                .shouldFailOnError(true)
                .shouldDoGC(true)
                .build();

        new Runner(options).run();
    }

    @Benchmark
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    public void stringEquals() {
        Random random = new Random(RANDOM_SEED);
        int num = 0;

        for (int i = 0; i < 1000; i++) {
            String a = TESTS[random.nextInt(3)];
            String b = TESTS[random.nextInt(3)];

            if (a.equals(b)) {
                num++;
            }
        }

        Assertions.assertNotEquals(0, num);
    }

    @Benchmark
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    public void stringLengthAndThenEqual() {
        Random random = new Random(RANDOM_SEED);
        int num = 0;

        for (int i = 0; i < 1000; i++) {
            String a = TESTS[random.nextInt(3)];
            String b = TESTS[random.nextInt(3)];

            if ((a.length() == b.length()) && a.equals(b)) {
                num++;
            }
        }

        Assertions.assertNotEquals(0, num);
    }

    @Benchmark
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    public void stringGetFromMap() {
        Random random = new Random(RANDOM_SEED);
        int num = 0;
        Map<String, Boolean> map = new HashMap<>();
        map.put(TESTS[0], true);
        map.put(TESTS[1], true);
        map.put(TESTS[2], true);

        for (int i = 0; i < 1000; i++) {
            String a = TESTS[random.nextInt(3)];
            String b = TESTS[random.nextInt(3)];

            if (map.get(a) && b != null) {
                num++;
            }
        }

        Assertions.assertNotEquals(0, num);
    }
}
