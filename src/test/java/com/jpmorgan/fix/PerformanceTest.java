package com.jpmorgan.fix;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class PerformanceTest {
    private final Parser parser = new Parser();

    // Logon (A): 8=FIX.4.49=10235=A49=BuySide56=SellSide34=152=20190605-11:40:30.39298=0108=30141=Y553=Username554=Password10=104
    private final byte[] message = new byte[]{
            0x38, 0x3d, 0x46, 0x49, 0x58, 0x2e, 0x34, 0x2e, 0x34, 0x01,
            0x39, 0x3d, 0x31, 0x30, 0x32, 0x01,
            0x33, 0x35, 0x3d, 0x41, 0x01,
            0x34, 0x39, 0x3d, 0x42, 0x75, 0x79, 0x53, 0x69, 0x64, 0x65, 0x01,
            0x35, 0x36, 0x3d, 0x53, 0x65, 0x6c, 0x6c, 0x53, 0x69, 0x64, 0x65, 0x01,
            0x33, 0x34, 0x3d, 0x31, 0x01,
            0x35, 0x32, 0x3d, 0x32, 0x30, 0x31, 0x39, 0x30, 0x36, 0x30, 0x35, 0x2d, 0x31, 0x31, 0x3a, 0x34, 0x30, 0x3a, 0x33, 0x30, 0x2e, 0x33, 0x39, 0x32, 0x01,
            0x39, 0x38, 0x3d, 0x30, 0x01,
            0x31, 0x30, 0x38, 0x3d, 0x33, 0x30, 0x01,
            0x31, 0x34, 0x31, 0x3d, 0x59, 0x01,
            0x35, 0x35, 0x33, 0x3d, 0x55, 0x73, 0x65, 0x72, 0x6e, 0x61, 0x6d, 0x65, 0x01,
            0x35, 0x35, 0x34, 0x3d, 0x50, 0x61, 0x73, 0x73, 0x77, 0x6f, 0x72, 0x64, 0x01,
            0x31, 0x30, 0x3d, 0x31, 0x30, 0x34, 0x01};

    /**
     * Warm up JVM eden space and promote long-lived objects to tenured space prior to actually parse Logon (A) FIX message.
     * <p>
     * Statistically, 5,000 iterations are recommended to warm up the JVM heap. One sample has the following output:
     *
     * <pre>
     * 5 iterations to parse the message in 52.6250 μs
     * 50 iterations to parse the message in 17.2080 μs
     * 500 iterations to parse the message in 2.9590 μs
     * 5,000 iterations to parse the message in 3.4170 μs
     * 5,000,000 iterations to parse the message in 2.9170 μs
     * </pre>
     * <p>
     * 500 iterations warm up having 2.9590 μs outperforms 5000 iterations.
     *
     * @param numberOfTrial The number of times to iterate parsing Logon (A) FIX message for warm up
     */
    @ParameterizedTest
    @CsvSource({
            "5",
            "50",
            "500",
            "5_000",
            "5_000_000",
    })
    public void test(long numberOfTrial) {
        // given
        for (long i = 0; i < numberOfTrial; i++)
            parser.parse(message); // warm up objects inside Parser, promote long-lived objects to tenured space
        long begin = System.nanoTime();
        // when
        parser.parse(message);
        // then
        double elapsed = System.nanoTime() - begin;
        System.out.printf("%,d iterations to parse the message in %.4f μs%n", numberOfTrial, elapsed / 1000);
    }
}
