package com.jpmorgan.fix;

import java.util.Map;

public final class Parser {

    /**
     * Parse the input byte array into a map storing parsed tag-value pairs.
     * <p>
     * The input byte array should contain one and only one complete FIX message. If the input byte array contains
     * an invalid FIX message or more than one complete FIX message, this function treats it as an invalid FIX message,
     * throwing IllegalArgumentException.
     *
     * @param message A single line byte array represented FIX message
     * @param result  A map storing parsed tag-value pairs
     * @throws IllegalArgumentException If the input byte array contains an invalid FIX message or more than one
     *                                  complete FIX message, this function treats it as an invalid FIX message
     * @see PrettyPrinter#print(Map)
     */
    public void parse(byte[] message, Map<Integer, Object> result) throws IllegalArgumentException {
        throw new IllegalArgumentException("Invalid FIX message. The Parser accepts only one and only one complete FIX message");
    }
}
