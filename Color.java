public enum Color {
    BLACK, RED, GREEN, YELLOW, BLUE, MAGENTA, CYAN, WHITE;

    public static final String reset = "\33[0m";

    public String bgCode() {
        return String.format("\33[4%dm", this.ordinal());
    }

    public String fgCode() {
        return String.format("\33[3%dm", this.ordinal());
    }
}
