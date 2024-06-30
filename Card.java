import java.beans.ConstructorProperties;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Card {
    public static TextCanvas cardBack = new TextCanvas("\33[37m\33[41m\u2663\u2660\n\u2665\u2666\33[0m");

    /**
     * @return a new shuffled deck of {@link Card}s
     */
    public static List<Card> deck() {
        ArrayList<Card> result = new ArrayList<>(52);
        for (int rank = 2; rank <= 14; rank++) {
            for (Suit suit : Suit.values()) {
                result.add(Main.random.nextInt(result.size() + 1), new Card(rank, suit));
            }
        }
        return result;
    }

    private final int rank; // Aces are 14, Jacks are 11, Queens are 12, Kings are 13
    private final Suit suit;

    /**
     * @param rank aces are 14, jacks are 11, queens are 12, and kings are 13
     * @throws IllegalArgumentException if {@code rank < 1 || rank > 14}
     */
    @ConstructorProperties({ "rank, suit" })
    public Card(int rank, Suit suit) throws IllegalArgumentException, NullPointerException {
        if (rank < 2) {
            throw new IllegalArgumentException("rank < 2");
        } else if (rank > 14) {
            throw new IllegalArgumentException("rank > 14");
        } else {
            this.rank = rank;
            this.suit = Objects.requireNonNull(suit, "suit == null");
        }
    }

    public boolean equals(Object other) {
        if (other == null || other.getClass() != Card.class) {
            return false;
        }
        Card o = (Card) other;
        return getRank() == o.getRank() && getSuit() == o.getSuit();
    }

    public int getRank() {
        return rank;
    }

    public Suit getSuit() {
        return suit;
    }

    public int hashCode() {
        return getRank() << 2 | getSuit().ordinal();
    }

    public TextCanvas toCanvas() {
        final StringBuilder s = new StringBuilder(15).append("\33[47m\33[3")
                .append(getSuit().ordinal() >= 2 ? '1' : '0').append('m');
        switch (getRank()) {
            case 10:
                s.append("10");
                break;
            case 11:
                s.append("J ");
                break;
            case 12:
                s.append("Q ");
                break;
            case 13:
                s.append("K ");
                break;
            case 14:
                s.append("A ");
                break;
            default:
                s.append(getRank()).append(' ');
        }
        return new TextCanvas(s.append('\n').append(getSuit()).append(' '));
    }

    public String toString() {
        String s;
        switch (getRank()) {
            case 11:
                s = "J";
                break;
            case 12:
                s = "Q";
                break;
            case 13:
                s = "K";
                break;
            case 14:
                s = "A";
                break;
            default:
                s = Integer.toString(getRank());
        }
        return s + getSuit().toString();
    }
}
