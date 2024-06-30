import java.util.Collection;
import java.util.Objects;

public class MoneyContainer {
    private int balance;

    /**
     * @throws IllegalArgumentException if {@code balance < 0}
     */
    public MoneyContainer(int balance) throws IllegalArgumentException {
        if (balance < 0) {
            throw new IllegalArgumentException("balance < 0");
        } else {
            setBalance(balance);
        }
    }

    /**
     * @throws IllegalArgumentException if {@code amount < 0}
     */
    public void addMoney(int amount) throws IllegalArgumentException {
        if (amount < 0) {
            throw new IllegalArgumentException("amount < 0");
        } else {
            balance += amount;
        }
    }

    public int getBalance() {
        return balance;
    }

    /**
     * Equally distribute the contents of this {@link MoneyContainer} between
     * {@code dests}.
     */
    public void moveMoney(Collection<? extends MoneyContainer> dests) throws NullPointerException {
        moveMoney(Objects.requireNonNull(dests, "dests == null"), getBalance());
    }

    /**
     * Give up to {@code each} money to each element of {@code dests}.
     * 
     * @param ensureEqual if {@code true}, if this {@link MoneyContainer} does not
     *                    contain enough money to give {@code each} to each element
     *                    of {@code dests}, the amount that can be given wil be
     *                    split equally.
     *                    if {@code false}, if this {@link MoneyContainer} does not
     *                    contain enough money to give {@code each} to each element
     *                    of {@code dests}, the elements of {@code dest} will be
     *                    filled in the order that they are yielded by
     *                    {@code dest.iterator()}.
     * @throws IllegalArgumentException if {@code each < 0}
     */
    public void moveMoney(Collection<? extends MoneyContainer> dests, int each, boolean ensureEqual)
            throws IllegalArgumentException, NullPointerException {
        if (each < 0) {
            throw new IllegalArgumentException("each < 0");
        } else if (!Objects.requireNonNull(dests, "dests == null").isEmpty()) {
            if (ensureEqual) {
                moveMoney(dests, Math.min(each, getBalance() / dests.size()), false);
            } else {
                for (MoneyContainer dest : dests) {
                    dest.addMoney(removeMoney(each));
                }
            }
        }
    }

    /**
     * Equally distribute up to {@code total} money between {@code dests}.
     * 
     * @throws IllegalArgumentException if {@code total < 0}
     */
    public void moveMoney(Collection<? extends MoneyContainer> dests, int total)
            throws IllegalArgumentException, NullPointerException {
        if (total < 0) {
            throw new IllegalArgumentException("total < 0");
        } else if (!Objects.requireNonNull(dests, "dests == null").isEmpty()) {
            moveMoney(dests, total / dests.size(), true);
        }
    }

    /**
     * Remove all money from this {@link MoneyContainer}.
     * 
     * @return the amount of money removed
     */
    public int removeMoney() {
        final int result = getBalance();
        setBalance(0);
        return result;
    }

    /**
     * Remove the maximum possible amount of money less than or equal to
     * {@code amount}.
     * 
     * @param amount the maximum amount of money to remove
     * @return the amount of money removed
     * @throws IllegalArgumentException if {@code amount < 0}
     */
    public int removeMoney(int amount) throws IllegalArgumentException {
        if (amount < 0) {
            throw new IllegalArgumentException("amount < 0");
        } else if (amount >= getBalance()) {
            return removeMoney();
        } else {
            balance -= amount;
            return amount;
        }
    }

    /**
     * @throws IllegalArgumentException if {@code newBalance < 0}
     */
    public void setBalance(int newBalance) throws IllegalArgumentException {
        if (newBalance < 0) {
            throw new IllegalArgumentException("newBalance < 0");
        } else {
            balance = newBalance;
        }
    }
}
