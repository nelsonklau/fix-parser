package com.jpmorgan.fix;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ParserTest {
    private final Parser parser = new Parser();

    @ParameterizedTest
    @MethodSource("validByteArraySource")
    void testParseValidByteArray(byte[] message, Supplier<Map<Float, List<Byte>>> expected) {
        // when
        Map<Float, List<Byte>> actual = assertDoesNotThrow(() -> parser.parse(message));
        // then
        assertEquals(expected.get(), actual);
    }

    @Disabled
    @ParameterizedTest
    @MethodSource("invalidByteArraySource")
    void testParseInvalidByteArray(byte[] message) {
        // when,then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> parser.parse(message));
        assertEquals("Invalid FIX message. The Parser accepts only one and only one complete FIX message", exception.getMessage());
    }

    private static Stream<Arguments> validByteArraySource() {
        return Stream.of(
                // Valid scenario 1: A flatten FIX message having no repeating groups
                // Logon (A): 8=FIX.4.49=10235=A49=BuySide56=SellSide34=152=20190605-11:40:30.39298=0108=30141=Y553=Username554=Password10=104
                Arguments.of(new byte[]{0x38, 0x3d, 0x46, 0x49, 0x58, 0x2e, 0x34, 0x2e, 0x34, 0x01,
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
                                0x31, 0x30, 0x3d, 0x31, 0x30, 0x34, 0x01},
                        (Supplier<Map<Float, List<Byte>>>) () -> {
                            Map<Float, List<Byte>> expected = new HashMap<>(Map.of(
                                    8.0f, List.of((byte) 0x46, (byte) 0x49, (byte) 0x58, (byte) 0x2e, (byte) 0x34, (byte) 0x2e, (byte) 0x34),
                                    9.0f, List.of((byte) 0x31, (byte) 0x30, (byte) 0x32),
                                    35.0f, List.of((byte) 0x41),
                                    49.0f, List.of((byte) 0x42, (byte) 0x75, (byte) 0x79, (byte) 0x53, (byte) 0x69, (byte) 0x64, (byte) 0x65),
                                    56.0f, List.of((byte) 0x53, (byte) 0x65, (byte) 0x6c, (byte) 0x6c, (byte) 0x53, (byte) 0x69, (byte) 0x64, (byte) 0x65),
                                    34.0f, List.of((byte) 0x31),
                                    52.0f, List.of((byte) 0x32, (byte) 0x30, (byte) 0x31, (byte) 0x39, (byte) 0x30, (byte) 0x36, (byte) 0x30, (byte) 0x35, (byte) 0x2d, (byte) 0x31, (byte) 0x31, (byte) 0x3a, (byte) 0x34, (byte) 0x30, (byte) 0x3a, (byte) 0x33, (byte) 0x30, (byte) 0x2e, (byte) 0x33, (byte) 0x39, (byte) 0x32),
                                    98.0f, List.of((byte) 0x30),
                                    108.0f, List.of((byte) 0x33, (byte) 0x30),
                                    141.0f, List.of((byte) 0x59)
                            ));
                            expected.putAll(Map.of(
                                    553.0f, List.of((byte) 0x55, (byte) 0x73, (byte) 0x65, (byte) 0x72, (byte) 0x6e, (byte) 0x61, (byte) 0x6d, (byte) 0x65),
                                    554.0f, List.of((byte) 0x50, (byte) 0x61, (byte) 0x73, (byte) 0x73, (byte) 0x77, (byte) 0x6f, (byte) 0x72, (byte) 0x64),
                                    10.0f, List.of((byte) 0x31, (byte) 0x30, (byte) 0x34)
                            ));
                            return expected;
                        }
                )
        );
    }

    private static Stream<byte[]> invalidByteArraySource() {
        return Stream.of(
                // Invalid scenario 1: empty byte array
                new byte[0],
                // Invalid scenario 2: missing FIX fields
                // Logon (A): 8=FIX.4.49=1235=A108=3010=036
                new byte[]{0x38, 0x3d, 0x46, 0x49, 0x58, 0x2e, 0x34, 0x2e, 0x34, 0x01,
                        0x39, 0x3d, 0x31, 0x32, 0x01,
                        0x33, 0x35, 0x3d, 0x41, 0x01,
                        0x31, 0x30, 0x38, 0x3d, 0x33, 0x30, 0x01,
                        0x31, 0x30, 0x3d, 0x30, 0x33, 0x36, 0x01}
        );
    }
}