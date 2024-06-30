public enum ActionType {
    ALL_IN, RAISE, BET, CALL, CHECK, FOLD;

    public String toString() {
        return this == ALL_IN ? "ALL-IN" : name();
    }
}
