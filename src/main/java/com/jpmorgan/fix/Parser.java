package com.jpmorgan.fix;

import java.util.*;
import java.util.stream.Stream;

public final class Parser {

    private final static byte SOH = 0x01;
    private final static byte EQUALS = 0x3d;

    private final Stack<Float> currentGroup = new Stack<>();
    private final Map<Integer, Integer> groupOccurrences = new HashMap<>(60, 1);

    public Parser() {
        Stream.of(
                534, 78, 870, 420, 398, 576, 938, 936, 518, 382,
                862, 85, 864, 124, 627, 146, 555, 428, 199, 558,
                670, 604, 683, 33, 73, 268, 267, 816, 136, 384,
                539, 756, 948, 806, 952, 804, 453, 211, 753, 702,
                711, 802, 295, 735, 296, 510, 473, 215, 454, 778,
                781, 801, 552, 232, 580, 897, 386, 768, 887, 457
        ).forEach(group -> groupOccurrences.put(group, 0));
    }

    /**
     * Parse the input byte array into a map storing parsed tag-value pairs.
     * <p>
     * The input byte array should contain one and only one complete FIX message.
     *
     * @param message A single line byte array represented FIX message
     * @return A map storing tag-value pairs
     */
    public Map<Float, List<Byte>> parse(byte[] message) {
        int initialCapacity = 0;
        for (byte b : message) if (b == SOH) initialCapacity++;
        HashMap<Float, List<Byte>> result = new HashMap<>(initialCapacity, 1);
        LinkedList<Byte> buffer = new LinkedList<>();
        List<Integer> groupMemberSnapshot = List.of(); // empty list stored in metaspace so it is re-usable
        float tag = 0, indexWithinGroup = 0;
        for (byte b : message) {
            if (b == EQUALS) {
                tag = parseTag(buffer, indexWithinGroup);
                if (!currentGroup.isEmpty()) {
                    int groupTag = currentGroup.peek().intValue();
                    if (!GROUP_MEMBER_DEFINITIONS.get(groupTag).contains((int) tag)) {
                        tag = (int) tag;
                        groupMemberSnapshot = List.of();
                        indexWithinGroup = 0;
                        currentGroup.pop();
                    }
                }
                if (groupOccurrences.containsKey((int) tag)) {
                    currentGroup.push(tag);
                    groupMemberSnapshot = new ArrayList<>(GROUP_MEMBER_DEFINITIONS.get((int) tag));
                } else if (groupMemberSnapshot.contains((int) tag)) groupMemberSnapshot.remove((Integer) (int) tag);
                if (!currentGroup.isEmpty() && groupMemberSnapshot.isEmpty()) {
                    // TODO: handle missing 305.1, 311.1, 309.1
                    indexWithinGroup += 0.1f;
                    int groupTag = currentGroup.peek().intValue();
                    groupMemberSnapshot = new ArrayList<>(GROUP_MEMBER_DEFINITIONS.get(groupTag));
                }
                continue;
            }
            if (b == SOH) {
                Byte b1;
                ArrayList<Byte> value = new ArrayList<>(buffer.size());
                while ((b1 = buffer.poll()) != null) value.add(b1);
                result.put(tag, value);
                groupOccurrences.computeIfPresent((int) tag, (k, v) -> parseToInt(value));
                continue;
            }
            buffer.add(b);
        }
        return result;
    }

    // Utilities to manage groups

    private final static Map<Integer, List<Integer>> GROUP_MEMBER_DEFINITIONS = new HashMap<>(60, 1);

    static {
        GROUP_MEMBER_DEFINITIONS.put(454, List.of(455, 456));
        GROUP_MEMBER_DEFINITIONS.put(711, List.of(311, 312, 309, 305, 462, 463, 310, 763, 313, 542, 315, 241, 242, 243, 244, 245, 246, 256, 595, 592, 593, 594, 247, 941, 317, 436, 435, 308, 306, 362, 363, 307, 364, 365, 877, 878, 318, 879, 810, 882, 883, 884, 885, 886));
    }

    // Utilities to convert byte array to numeric types

    private final static Map<Byte, Float> DIGITS = Map.of(
            (byte) 0x30, 0.0f,
            (byte) 0x31, 1.0f,
            (byte) 0x32, 2.0f,
            (byte) 0x33, 3.0f,
            (byte) 0x34, 4.0f,
            (byte) 0x35, 5.0f,
            (byte) 0x36, 6.0f,
            (byte) 0x37, 7.0f,
            (byte) 0x38, 8.0f,
            (byte) 0x39, 9.0f
    );

    private int parseToInt(List<Byte> value) {
        int result = 0;
        for (int i = value.size() - 1, exponent = 0; i >= 0; i--)
            result += DIGITS.get(value.get(i)).intValue() * (int) Math.pow(10, exponent++);
        return result;
    }

    private float parseTag(Deque<Byte> buffer, float indexWithinGroup) {
        Byte b;
        int exponent = 0;
        float tag = 0;
        while ((b = buffer.pollLast()) != null)
            tag += DIGITS.get(b) * (float) Math.pow(10, exponent++);
        return tag + indexWithinGroup;
    }
}
