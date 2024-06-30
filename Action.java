import java.beans.ConstructorProperties;
import java.util.Objects;

/**
 * An action that a {@link Player} can take.
 */
public class Action {
    private final int amount;
    private final ActionType type;

    /**
     * @throws IllegalArgumentException if {@code amount < 0}
     */
    @ConstructorProperties({ "type", "amount" })
    public Action(ActionType type, int amount) throws IllegalArgumentException, NullPointerException {
        this.type = Objects.requireNonNull(type, "type == null");
        if (amount < 0) {
            throw new IllegalArgumentException("amount < 0");
        } else {
            this.amount = amount;
        }
    }

    public boolean equals(Object other) {
        if (other == null || other.getClass() != Action.class) {
            return false;
        }
        Action o = (Action) other;
        return getAmount() == o.getAmount() && getType() == o.getType();
    }

    public int getAmount() {
        return amount;
    }

    public ActionType getType() {
        return type;
    }

    public int hashCode() {
        return getAmount() << 3 | getType().ordinal();
    }

    public String toString() {
        return getType() == ActionType.RAISE || getType() == ActionType.BET ? getType().toString() + " $" + getAmount()
                : getType().toString();
    }
}
