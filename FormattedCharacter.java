import java.util.Objects;

/**
 * A character with a foreground and background color.
 */
public class FormattedCharacter {
    public static final FormattedCharacter blank = new FormattedCharacter(' ', null, null);

    private final Color bg;
    private final Color fg;
    private final char value;

    public FormattedCharacter(char value, Color fg, Color bg) {
        this.bg = bg;
        this.fg = fg;
        this.value = value;
    }

    public char charValue() {
        return value;
    }

    public boolean equals(Object other) {
        if (other == null || other.getClass() != FormattedCharacter.class) {
            return false;
        }
        FormattedCharacter o = (FormattedCharacter) other;
        return charValue() == o.charValue() && getBgColor() == o.getBgColor() && getFgColor() == o.getFgColor();
    }

    public Color getBgColor() {
        return bg;
    }

    public Color getFgColor() {
        return fg;
    }

    public int hashCode() {
        return Objects.hash(fg, bg, Character.valueOf(value));
    }

    public String toString() {
        boolean plain = true;
        StringBuilder result = new StringBuilder(16);
        if (getBgColor() != null) {
            plain = false;
            result.append(getBgColor().bgCode());
        }
        if (getFgColor() != null) {
            plain = false;
            result.append(getFgColor().fgCode());
        }
        if (plain) {
            return String.valueOf(charValue());
        } else {
            return result.append(charValue()).append(Color.reset).toString();
        }
    }
}
