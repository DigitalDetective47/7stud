public enum Suit {
    CLUBS, SPADES, HEARTS, DIAMONDS;

    public String toString() {
        switch (this) {
            case CLUBS:
                return "\u2663";
            case SPADES:
                return "\u2660";
            case HEARTS:
                return "\u2665";
            case DIAMONDS:
                return "\u2666";
            default:
                throw new NullPointerException();
        }
    }
}
