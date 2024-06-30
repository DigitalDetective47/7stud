import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class HandRank implements Comparable<HandRank> {
    public static final HandRank best = new HandRank(HandType.STRAIGHTFLUSH, new int[] { 14 });
    public static final HandRank worst = new HandRank(HandType.HIGHCARD, new int[] { 7, 5, 4, 3, 2 });

    private final HandType type;
    private final int[] kickers;

    public HandRank(HandType type, int[] kickers) {
        this.kickers = kickers;
        this.type = type;
    }

    /**
     * Rank a hand.
     * 
     * @throws IllegalArgumentException if the hand is empty
     */
    public HandRank(List<? extends Card> hand) throws IllegalArgumentException, NullPointerException {
        int[] v;
        switch (Objects.requireNonNull(hand, "hand == null").size()) {
            case 0:
                throw new IllegalArgumentException("hand.size() == 0");
            case 1:
                kickers = new int[] { hand.get(0).getRank() };
                type = HandType.HIGHCARD;
                break;
            case 2:
                if (hand.get(0).getRank() == hand.get(1).getRank()) {
                    kickers = new int[] { hand.get(0).getRank() };
                    type = HandType.ONEPAIR;
                } else {
                    kickers = hand.get(0).getRank() < hand.get(1).getRank()
                            ? new int[] { hand.get(1).getRank(), hand.get(0).getRank() }
                            : new int[] { hand.get(0).getRank(), hand.get(1).getRank() };
                    type = HandType.HIGHCARD;
                }
                break;
            case 3:
                v = new int[3];
                for (int i = 0; i < 3; i++) {
                    v[i] = hand.get(i).getRank();
                }
                Arrays.sort(v);
                if (v[0] == v[2]) {
                    kickers = new int[] { v[0] };
                    type = HandType.THREEOFAKIND;
                } else if (v[0] == v[1]) {
                    kickers = new int[] { v[0], v[2] };
                    type = HandType.ONEPAIR;
                } else if (v[1] == v[2]) {
                    kickers = new int[] { v[1], v[0] };
                    type = HandType.ONEPAIR;
                } else {
                    kickers = new int[3];
                    for (int i = 0; i < 3; i++) {
                        kickers[i] = v[2 - i];
                    }
                    type = HandType.HIGHCARD;
                }
                break;
            case 4:
                v = new int[4];
                for (int i = 0; i < 4; i++) {
                    v[i] = hand.get(i).getRank();
                }
                Arrays.sort(v);
                if (v[0] == v[3]) {
                    kickers = new int[] { v[0] };
                    type = HandType.FOUROFAKIND;
                } else if (v[0] == v[2]) {
                    kickers = new int[] { v[0], v[3] };
                    kickers[0] = v[0];
                    kickers[1] = v[3];
                    type = HandType.THREEOFAKIND;
                } else if (v[1] == v[3]) {
                    kickers = new int[] { v[1], v[0] };
                    type = HandType.THREEOFAKIND;
                } else if (v[2] == v[3]) {
                    if (v[0] == v[1]) {
                        kickers = new int[] { v[2], v[0] };
                        type = HandType.TWOPAIR;
                    } else {
                        kickers = new int[] { v[2], v[1], v[0] };
                        type = HandType.ONEPAIR;
                    }
                } else if (v[0] == v[1]) {
                    kickers = new int[] { v[0], v[3], v[2] };
                    type = HandType.ONEPAIR;
                } else if (v[1] == v[2]) {
                    kickers = new int[] { v[1], v[3], v[0] };
                    type = HandType.ONEPAIR;
                } else {
                    kickers = new int[4];
                    for (int i = 0; i < 4; i++) {
                        kickers[i] = v[3 - i];
                    }
                    type = HandType.HIGHCARD;
                }
                break;
            case 5:
                v = new int[5];
                for (int i = 0; i < 5; i++) {
                    v[i] = hand.get(i).getRank();
                }
                Arrays.sort(v);
                boolean flush = true;
                for (Card c : hand) {
                    if (c.getSuit() != hand.get(0).getSuit()) {
                        flush = false;
                    }
                }
                final int[] fiveHighStraight = { 2, 3, 4, 5, 14 };
                if (Arrays.equals(v, fiveHighStraight)) {
                    kickers = new int[] { 5 };
                    type = flush ? HandType.STRAIGHTFLUSH : HandType.STRAIGHT;
                } else {
                    boolean straight = true;
                    for (int i = 1; i < 5; i++) {
                        if (v[i] != v[0] + i) {
                            straight = false;
                        }
                    }
                    if (straight) {
                        kickers = new int[] { v[4] };
                        type = flush ? HandType.STRAIGHTFLUSH : HandType.STRAIGHT;
                    } else if (flush) {
                        kickers = new int[5];
                        for (int i = 0; i < 5; i++) {
                            kickers[i] = v[4 - i];
                        }
                        type = HandType.FLUSH;
                    } else if (v[0] == v[3]) {
                        kickers = new int[] { v[0], v[4] };
                        type = HandType.FOUROFAKIND;
                    } else if (v[1] == v[4]) {
                        kickers = new int[] { v[1], v[0] };
                        type = HandType.FOUROFAKIND;
                    } else if (v[0] == v[2]) {
                        if (v[3] == v[4]) {
                            kickers = new int[] { v[0], v[3] };
                            type = HandType.FULLHOUSE;
                        } else {
                            kickers = new int[] { v[0], v[4], v[3] };
                            type = HandType.THREEOFAKIND;
                        }
                    } else if (v[1] == v[3]) {
                        kickers = new int[] { v[1], v[4], v[0] };
                        type = HandType.THREEOFAKIND;
                    } else if (v[2] == v[4]) {
                        if (v[0] == v[1]) {
                            kickers = new int[] { v[2], v[0] };
                            type = HandType.FULLHOUSE;
                        } else {
                            kickers = new int[] { v[2], v[1], v[0] };
                            type = HandType.THREEOFAKIND;
                        }
                    } else if (v[0] == v[1]) {
                        if (v[2] == v[3]) {
                            kickers = new int[] { v[2], v[0], v[4] };
                            type = HandType.TWOPAIR;
                        } else if (v[3] == v[4]) {
                            kickers = new int[] { v[3], v[0], v[2] };
                            type = HandType.TWOPAIR;
                        } else {
                            kickers = new int[] { v[0], v[4], v[3], v[2] };
                            type = HandType.ONEPAIR;
                        }
                    } else if (v[1] == v[2]) {
                        if (v[3] == v[4]) {
                            kickers = new int[] { v[3], v[2], v[0] };
                            type = HandType.TWOPAIR;
                        } else {
                            kickers = new int[] { v[1], v[4], v[3], v[0] };
                            type = HandType.ONEPAIR;
                        }
                    } else if (v[2] == v[3]) {
                        kickers = new int[] { v[2], v[4], v[1], v[0] };
                        type = HandType.ONEPAIR;
                    } else if (v[3] == v[4]) {
                        kickers = new int[] { v[3], v[2], v[1], v[0] };
                        type = HandType.ONEPAIR;
                    } else {
                        kickers = new int[5];
                        for (int i = 0; i < 5; i++) {
                            kickers[i] = v[4 - i];
                        }
                        type = HandType.HIGHCARD;
                    }
                }
                break;
            default:
                HandRank b = worst;
                for (int i = hand.size() - 1; i >= 4; i--) {
                    for (int j = i - 1; j >= 3; j--) {
                        for (int k = j - 1; k >= 2; k--) {
                            for (int l = k - 1; l >= 1; l--) {
                                for (int m = l - 1; m >= 0; m--) {
                                    final HandRank h = new HandRank(Arrays.asList(hand.get(i), hand.get(j), hand.get(k),
                                            hand.get(l), hand.get(m)));
                                    if (b.compareTo(h) < 0) {
                                        b = h;
                                    }
                                }
                            }
                        }
                    }
                }
                kickers = b.kickers;
                type = b.type;
        }
    }

    public int compareTo(HandRank other) {
        if (type.ordinal() == other.type.ordinal()) {
            for (int i = 0; i < Math.min(kickers.length, other.kickers.length); i++) {
                if (kickers[i] != other.kickers[i]) {
                    return kickers[i] - other.kickers[i];
                }
            }
            return kickers.length - other.kickers.length;
        } else {
            return type.ordinal() - other.type.ordinal();
        }
    }

    public boolean equals(Object other) {
        if (other == null || other.getClass() != HandRank.class) {
            return false;
        }
        HandRank o = (HandRank) other;
        return type == o.type && Arrays.equals(kickers, o.kickers);
    }

    public int hashCode() {
        return Objects.hash(kickers, type);
    }
}
