package com.jpmorgan.fix;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ParserTest {
    private final Parser parser = new Parser();

    @ParameterizedTest
    @MethodSource("invalidByteArraySource")
    void testParseInvalidByteArray(byte[] message) {
        // when,then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> parser.parse(message, Map.of()));
        assertEquals("Invalid FIX message. The Parser accepts only one and only one complete FIX message", exception.getMessage());
    }

    private static Stream<byte[]> invalidByteArraySource() {
        return Stream.of(
                new byte[0],
                // 8=FIX.4.49=1235=A108=3010=036
                new byte[]{0x38, 0x3d, 0x46, 0x49, 0x58, 0x2e, 0x34, 0x2e, 0x34, 0x01,
                        0x39, 0x3d, 0x31, 0x32, 0x01,
                        0x33, 0x35, 0x3d, 0x41, 0x01,
                        0x31, 0x30, 0x38, 0x3d, 0x33, 0x30, 0x01,
                        0x31, 0x30, 0x3d, 0x30, 0x33, 0x36, 0x01}
        );
    }
}