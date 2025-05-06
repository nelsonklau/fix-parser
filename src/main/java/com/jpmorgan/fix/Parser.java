package com.jpmorgan.fix;

import java.util.*;

public final class Parser {

    private final Stack<Float> currentGroup = new Stack<>();

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
        Deque<Byte> buf = new LinkedList<>();
        LinkedList<Integer> optionalDefs = new LinkedList<>();
        Set<Integer> defs = Set.of();
        float tag = 0, memberIndex = 0;
        for (byte b : message) {
            if (b == EQUALS) {
                // handle root level tags
                tag = parseTag(buf, memberIndex);
                if (!currentGroup.isEmpty()) {
                    int groupTag = currentGroup.peek().intValue();
                    if (!GROUP_MEMBER_DEFINITIONS.get(groupTag).contains((int) tag) &&
                            !OPTIONAL_GROUP_MEMBER_DEFINITIONS.get(groupTag).contains((int) tag)) {
                        tag = (int) tag;
                        defs = Set.of();
                        optionalDefs.clear();
                        memberIndex = 0;
                        currentGroup.pop();
                    }
                }
                // handle current tag being a group, i.e. NumInGroup field type
                if (GROUP_TAGS.contains((int) tag)) {
                    currentGroup.push(tag);
                    Set<Integer> d = GROUP_MEMBER_DEFINITIONS.get((int) tag);
                    defs = new HashSet<>(d.size(), 1);
                    defs.addAll(d);
                } else if (defs.contains((int) tag)) defs.remove((int) tag);        // handle current mandatory group member tags
                if (!currentGroup.isEmpty() && !optionalDefs.contains((int) tag)) { // handle current optional group member tags
                    int groupTag = currentGroup.peek().intValue();
                    if (OPTIONAL_GROUP_MEMBER_DEFINITIONS.get(groupTag).contains((int) tag))
                        optionalDefs.add((int) tag);
                } else {
                    // handle next group member tags
                    if (!currentGroup.isEmpty() && currentGroup.peek() != tag &&
                            defs.isEmpty() && (optionalDefs.isEmpty() || // when all mandatory and optional tags in current group are cleared
                            optionalDefs.contains((int) tag))) {         // when current tag is optional
                        int groupTag = currentGroup.peek().intValue();
                        memberIndex += 0.1f;
                        tag = (float) Math.floor((tag + 0.1f) * 100) / 100;
                        Set<Integer> d = GROUP_MEMBER_DEFINITIONS.get(groupTag);
                        defs = new HashSet<>(d.size(), 1);
                        defs.addAll(d);
                        optionalDefs.clear();
                        optionalDefs.add((int) tag);
                    }
                }
                continue;
            }
            if (b == SOH) {
                Byte b1;
                ArrayList<Byte> value = new ArrayList<>(buf.size());
                while ((b1 = buf.poll()) != null) value.add(b1);
                result.put(tag, value);
                continue;
            }
            buf.add(b);
        }
        return result;
    }

    // Delimiters to split FIX message into tag-value fields

    private final static byte SOH = 0x01;
    private final static byte EQUALS = 0x3d;

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

    private float parseTag(Deque<Byte> buf, float memberIndex) {
        Byte b;
        int exponent = 0;
        float tag = 0;
        while ((b = buf.pollLast()) != null)
            tag += DIGITS.get(b) * (float) Math.pow(10, exponent++);
        return tag + memberIndex;
    }

    // Utilities to manage groups in metaspace

    private final static Map<Integer, Set<Integer>> GROUP_MEMBER_DEFINITIONS = new HashMap<>(57, 1);
    private final static Map<Integer, Set<Integer>> OPTIONAL_GROUP_MEMBER_DEFINITIONS = new HashMap<>(57, 1);
    private final static Set<Integer> GROUP_TAGS = Set.of(
                534,  78, 870, 420, 398, 576, 938, 936, 518, 382,
                862,  85, 864, 124, 627, 146, 555, 428, 199, 558,
                670, 604, 683,  33,  73, 268, 267, 816, 136, 384,
                539, 756, 948, 806, 952, 804, 453, 211, 753, 702,
                711, 802, 295, 735, 296, 510, 473, 215, 454, 778,
                781, 801, 552, 232, 580, 897, 386, 768, 887, 457
    );

    static {
        Set.of(
                 33,  78,  85, 124, 136, 146, 199, 215, 232, 267,
                268, 295, 296, 382, 384, 386, 398, 420, 428, 453,
                454, 457, 473, 510, 518, 534, 539, 552, 555, 558,
                576, 580, 604, 627, 670, 683, 702, 711, 735, 753,
                756, 778, 781, 801, 802, 804, 806, 816, 862, 864,
                870, 887, 936, 938, 948, 952
        ).forEach(tag -> GROUP_MEMBER_DEFINITIONS.put(tag, Set.of()));
        GROUP_MEMBER_DEFINITIONS.put(73, Set.of(11, 67, 54));

        OPTIONAL_GROUP_MEMBER_DEFINITIONS.put(33, Set.of(58, 354, 355));
        OPTIONAL_GROUP_MEMBER_DEFINITIONS.put(73, Set.of(
                526, 583, 160, 229,  75,   1, 660, 581, 589, 590,
                 70, 591,  63,  64, 544, 635,  21,  18, 110, 111,
                100,  81, 140, 401, 114,  60, 854,  40, 423,  44,
                 99,  15, 376, 377,  23, 117,  59, 168, 432, 126,
                427, 528, 529, 582, 121, 120, 775,  58, 354, 355,
                193, 192, 640,  77, 204, 210, 847, 848, 849, 494,
                 55,  65,  48,  22, 460, 461, 167, 762, 220, 541,
                201, 224, 225, 239, 226, 227, 228, 255, 543, 470,
                471, 472, 240, 202, 947, 206, 231, 223, 207, 106,
                348, 349, 107, 350, 351, 691, 667, 875, 876, 873,
                874,  38, 152, 516, 468, 469, 218, 221, 222, 662,
                663, 699, 761, 235, 236, 701, 696, 697, 698,  12,
                 13, 479, 497, 211, 835, 836, 837, 838, 840, 388,
                389, 841, 842, 843, 844, 846,  11,  37, 198,  66,
                799, 800,  14,  39, 636, 151,  84,   6, 103));
        OPTIONAL_GROUP_MEMBER_DEFINITIONS.put(78, Set.of(
                 79, 661, 573, 366, 467,  80,  81, 208, 209, 776,
                161, 360, 361, 153, 154, 119, 737, 120, 736, 155,
                156, 742, 741, 780));
        OPTIONAL_GROUP_MEMBER_DEFINITIONS.put(85, Set.of(165, 787));
        OPTIONAL_GROUP_MEMBER_DEFINITIONS.put(124, Set.of(
                32, 17, 527, 31, 669, 29));
        OPTIONAL_GROUP_MEMBER_DEFINITIONS.put(136, Set.of(137, 138, 139, 891));
        OPTIONAL_GROUP_MEMBER_DEFINITIONS.put(146, Set.of(
                 55,  65,  48,  22, 460, 461, 167, 762, 220, 541,
                201, 224, 225, 239, 226, 227, 228, 255, 543, 470,
                471, 472, 240, 202, 947, 206, 231, 223, 207, 106,
                348, 349, 107, 350, 351, 691, 667, 875, 876, 873,
                874, 913, 914, 915, 918, 788, 916, 917, 919, 898,
                140, 303, 537, 336, 625, 229,  54, 854,  63,  64,
                193, 192,  15,   1, 660, 581, 692,  40,  62, 126,
                 60, 218, 221, 222, 662, 663, 699, 761, 423,  44,
                640, 235, 236, 701, 696, 697, 698, 827, 668, 869,
                58, 354, 355, 561, 562));
        OPTIONAL_GROUP_MEMBER_DEFINITIONS.put(199, Set.of(104));
        OPTIONAL_GROUP_MEMBER_DEFINITIONS.put(215, Set.of(216, 217));
        OPTIONAL_GROUP_MEMBER_DEFINITIONS.put(232, Set.of(233, 234));
        OPTIONAL_GROUP_MEMBER_DEFINITIONS.put(267, Set.of(269));
        OPTIONAL_GROUP_MEMBER_DEFINITIONS.put(268, Set.of(
                 55,  65,  48,  22, 460, 461, 167, 762, 220, 541,
                201, 224, 225, 239, 226, 227, 228, 255, 543, 470,
                471, 472, 240, 202, 947, 206, 231, 223, 207, 106,
                348, 349, 107, 350, 351, 691, 667, 875, 876, 873,
                874, 269, 270,  15, 271, 272, 273, 274, 275, 336,
                625, 276, 277, 282, 283, 284, 286,  59, 432, 126,
                110,  18, 287,  37, 299, 288, 289, 346, 290, 546,
                811,  58, 354, 355, 279, 285, 278, 280, 291, 292));
        OPTIONAL_GROUP_MEMBER_DEFINITIONS.put(295, Set.of(
                 55,  65,  48,  22, 460, 461, 167, 762, 220, 541,
                201, 224, 225, 239, 226, 227, 228, 255, 543, 470,
                471, 472, 240, 202, 947, 206, 231, 223, 207, 106,
                348, 349, 107, 350, 351, 691, 667, 875, 876, 873,
                874, 913, 914, 915, 916, 917, 918, 919, 788, 898,
                299, 132, 133, 134, 135,  62, 188, 190, 189, 191,
                631, 632, 633, 634,  60, 336, 625,  64,  40, 193,
                192, 642, 643, 15, 368));
        OPTIONAL_GROUP_MEMBER_DEFINITIONS.put(296, Set.of(
                311, 312, 309, 305, 462, 463, 310, 763, 313, 542,
                315, 241, 242, 243, 244, 245, 246, 256, 595, 592,
                593, 594, 247, 941, 317, 436, 435, 308, 306, 362,
                363, 307, 364, 365, 877, 878, 318, 879, 810, 882,
                883, 884, 885, 886, 302, 367, 304, 893));
        OPTIONAL_GROUP_MEMBER_DEFINITIONS.put(382, Set.of(
                375, 337, 437, 438, 655));
        OPTIONAL_GROUP_MEMBER_DEFINITIONS.put(384, Set.of(372, 385));
        OPTIONAL_GROUP_MEMBER_DEFINITIONS.put(386, Set.of(336, 625));
        OPTIONAL_GROUP_MEMBER_DEFINITIONS.put(398, Set.of(
                399, 400, 401, 404, 441, 402, 403, 405, 406, 407,
                408));
        OPTIONAL_GROUP_MEMBER_DEFINITIONS.put(420, Set.of(
                 66, 421,  54,  44, 423, 406, 336, 625,  58, 430,
                 63,  64,   1, 660, 354, 355));
        OPTIONAL_GROUP_MEMBER_DEFINITIONS.put(428, Set.of(
                 55,  65,  48,  22, 460, 461, 167, 762, 220, 541,
                201, 224, 225, 239, 226, 227, 228, 255, 543, 470,
                471, 472, 240, 202, 947, 206, 231, 223, 207, 106,
                348, 349, 107, 350, 351, 691, 667, 875, 876, 873,
                874));
        OPTIONAL_GROUP_MEMBER_DEFINITIONS.put(453, Set.of(448, 447, 452));
        OPTIONAL_GROUP_MEMBER_DEFINITIONS.put(454, Set.of(455, 456));
        OPTIONAL_GROUP_MEMBER_DEFINITIONS.put(457, Set.of(458, 459));
        OPTIONAL_GROUP_MEMBER_DEFINITIONS.put(473, Set.of(
                509, 511, 474, 482, 539, 524, 525, 538, 522, 486,
                475));
        OPTIONAL_GROUP_MEMBER_DEFINITIONS.put(510, Set.of(
                477, 512, 478, 498, 499, 500, 501, 502));
        OPTIONAL_GROUP_MEMBER_DEFINITIONS.put(518, Set.of(519, 520, 521));
        OPTIONAL_GROUP_MEMBER_DEFINITIONS.put(534, Set.of(41, 535, 536));
        OPTIONAL_GROUP_MEMBER_DEFINITIONS.put(539, Set.of(524, 525, 538));
        OPTIONAL_GROUP_MEMBER_DEFINITIONS.put(552, Set.of(
                 54,  41,  11, 526, 583, 586, 229,  75,  38, 152,
                516, 468, 469, 376,  58, 354, 355,   1, 660, 581,
                589, 591,  70, 854, 528, 529, 582, 121, 120, 775,
                 77, 203, 544, 635, 377, 659,  12,  13, 479, 497,
                 37, 198,  66,  81, 575, 576, 577, 578, 579, 821,
                 15,  40,  18, 483, 336, 625, 943, 381, 157, 230,
                158, 159, 738, 920, 921, 922, 238, 237, 118, 119,
                155, 156, 752, 825, 826));
        OPTIONAL_GROUP_MEMBER_DEFINITIONS.put(555, Set.of(
                687, 690, 564, 565, 654, 566, 587, 588, 637, 600,
                601, 602, 603, 607, 608, 609, 764, 610, 611, 248,
                249, 250, 251, 252, 253, 257, 599, 596, 597, 598,
                254, 612, 942, 613, 614, 615, 616, 617, 618, 619,
                620, 621, 622, 623, 624, 556, 740, 739, 955, 956,
                682, 686, 681, 684, 676, 677, 678, 679, 680));
        OPTIONAL_GROUP_MEMBER_DEFINITIONS.put(558, Set.of(167, 762, 460, 461));
        OPTIONAL_GROUP_MEMBER_DEFINITIONS.put(576, Set.of(577));
        OPTIONAL_GROUP_MEMBER_DEFINITIONS.put(580, Set.of(75, 60));
        OPTIONAL_GROUP_MEMBER_DEFINITIONS.put(604, Set.of(605, 606));
        OPTIONAL_GROUP_MEMBER_DEFINITIONS.put(627, Set.of(628, 629, 630));
        OPTIONAL_GROUP_MEMBER_DEFINITIONS.put(670, Set.of(
                671, 672, 673, 674, 675));
        OPTIONAL_GROUP_MEMBER_DEFINITIONS.put(683, Set.of(688, 689));
        OPTIONAL_GROUP_MEMBER_DEFINITIONS.put(702, Set.of(703, 704, 705, 706));
        OPTIONAL_GROUP_MEMBER_DEFINITIONS.put(711, Set.of(
                311, 312, 309, 305, 462, 463, 310, 763, 313, 542,
                315, 241, 242, 243, 244, 245, 246, 256, 595, 592,
                593, 594, 247, 941, 317, 436, 435, 308, 306, 362,
                363, 307, 364, 365, 877, 878, 318, 879, 810, 882,
                883, 884, 885, 886, 732, 733));
        OPTIONAL_GROUP_MEMBER_DEFINITIONS.put(735, Set.of(695));
        OPTIONAL_GROUP_MEMBER_DEFINITIONS.put(753, Set.of(707, 708));
        OPTIONAL_GROUP_MEMBER_DEFINITIONS.put(756, Set.of(757, 758, 759));
        OPTIONAL_GROUP_MEMBER_DEFINITIONS.put(778, Set.of(
                162, 163, 214,  54, 460, 167, 168, 461, 126, 779,
                172, 169, 170, 171, 492, 476, 488, 489, 503, 490,
                491, 504, 505));
        OPTIONAL_GROUP_MEMBER_DEFINITIONS.put(781, Set.of(782, 783, 784));
        OPTIONAL_GROUP_MEMBER_DEFINITIONS.put(801, Set.of(785, 786));
        OPTIONAL_GROUP_MEMBER_DEFINITIONS.put(802, Set.of(523, 803));
        OPTIONAL_GROUP_MEMBER_DEFINITIONS.put(804, Set.of(545, 805));
        OPTIONAL_GROUP_MEMBER_DEFINITIONS.put(806, Set.of(760, 807));
        OPTIONAL_GROUP_MEMBER_DEFINITIONS.put(816, Set.of(817));
        OPTIONAL_GROUP_MEMBER_DEFINITIONS.put(862, Set.of(528, 529, 863));
        OPTIONAL_GROUP_MEMBER_DEFINITIONS.put(864, Set.of(865, 866, 867, 868));
        OPTIONAL_GROUP_MEMBER_DEFINITIONS.put(870, Set.of(871, 872));
        OPTIONAL_GROUP_MEMBER_DEFINITIONS.put(887, Set.of(888, 889));
        OPTIONAL_GROUP_MEMBER_DEFINITIONS.put(936, Set.of(
                930, 931, 283, 284, 928, 929));
        OPTIONAL_GROUP_MEMBER_DEFINITIONS.put(938, Set.of(896));
        OPTIONAL_GROUP_MEMBER_DEFINITIONS.put(948, Set.of(949, 950, 951));
        OPTIONAL_GROUP_MEMBER_DEFINITIONS.put(952, Set.of(953, 954));
    }
}
