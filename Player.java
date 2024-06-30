import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class Player extends MoneyContainer {
    public static final int maxPlayers = 7;
    public static final Comparator<Player> sortById = Comparator.comparing(Player::getId);
    public static final int startingBalance = 100;

    public static Player dealer;
    public static Player[] players;

    /**
     * Sets the number of players, and randomly chooses a dealer.
     * If {@code numPlayers == 0}, sets {@link Player#dealer} to {@code null}.
     */
    public static void setPlayerCount(int numPlayers) throws NegativeArraySizeException {
        players = new Player[numPlayers];
        if (numPlayers == 0) {
            dealer = null;
        } else {
            players[0] = new Player(0);
            for (int i = 1; i < numPlayers; i++) {
                players[i - 1].next = players[i] = new Player(i);
            }
            players[numPlayers - 1].next = players[0];
            dealer = players[Main.random.nextInt(numPlayers)];
        }
    }

    private Action action;
    private List<Card> hand;
    private final int id;
    private Player next;

    private Player(int id) {
        super(startingBalance);
        setAction(null);
        setHand(null);
        this.id = id;
    }

    public void act(Action action) {
        int i;
        switch (action.getType()) {
            case CHECK:
                break;
            case FOLD:
                for (i = 0; i < Pot.pots.size() && Pot.pots.get(i).getEligiblePlayers().remove(this); i++) {
                }
                if (i == Pot.pots.size()) {
                    if (Pot.pots.get(i - 1).getEligiblePlayers().size() == 1) {
                        Pot.pots.get(i - 1).moveMoney(Pot.pots.remove(i - 1).getEligiblePlayers());
                    }
                } else if (Pot.pots.get(i).getEligiblePlayers().equals(Pot.pots.get(i - 1).getEligiblePlayers())) {
                    Pot.pots.remove(i).moveMoney(Arrays.asList(Pot.pots.get(i - 1)));
                }
                hand = null;
                break;
            case BET:
                i = 0;
                do {
                    if (Pot.pots.size() == i) {
                        Pot.createNextPot();
                    }
                    if (Pot.pots.get(i).getMaxInvestment() >= action.getAmount()) {
                        moveMoney(Arrays.asList(Pot.pots.get(i)), i == 0 ? action.getAmount()
                                : action.getAmount() - Pot.pots.get(i - 1).getMaxInvestment());
                        break;
                    } else {
                        moveMoney(Arrays.asList(Pot.pots.get(i)), i == 0 ? Pot.pots.get(i).getMaxInvestment()
                                : Pot.pots.get(i).getMaxInvestment() - Pot.pots.get(i - 1).getMaxInvestment());
                        i++;
                    }
                } while (true);
                break;
            default:
                for (i = 0; Pot.pots.get(i).getMaxInvestment() <= getRoundInvestment(); i++) {
                }
                if (Pot.pots.get(i).getMaxInvestment() >= action.getAmount()) {
                    moveMoney(Arrays.asList(Pot.pots.get(i)), action.getAmount() - getRoundInvestment());
                } else {
                    moveMoney(Arrays.asList(Pot.pots.get(i)),
                            Pot.pots.get(i).getMaxInvestment() - getRoundInvestment());
                    do {
                        i++;
                        if (Pot.pots.size() == i) {
                            Pot.createNextPot();
                        }
                        if (Pot.pots.get(i).getMaxInvestment() >= action.getAmount()) {
                            moveMoney(Arrays.asList(Pot.pots.get(i)), i == 0 ? action.getAmount()
                                    : action.getAmount() - Pot.pots.get(i - 1).getMaxInvestment());
                            break;
                        } else {
                            moveMoney(Arrays.asList(Pot.pots.get(i)), i == 0 ? Pot.pots.get(i).getMaxInvestment()
                                    : Pot.pots.get(i).getMaxInvestment() - Pot.pots.get(i - 1).getMaxInvestment());
                        }
                    } while (true);
                }
        }
        setAction(action);
    }

    public Action getAction() {
        return action;
    }

    /**
     * @return the amount of money that this {@link Player} can wager this round
     */
    public int getAvailableMoney() {
        return getBalance() + getRoundInvestment();
    }

    public List<Card> getHand() {
        return hand;
    }

    public int getId() {
        return id;
    }

    public int getRoundInvestment() {
        return getAction() == null ? 0 : getAction().getAmount();
    }

    public List<Card> getUpCards() {
        if (getHand() == null) {
            return null;
        } else {
            int s;
            switch (Main.round) {
                case 4:
                    s = 3;
                    break;
                case 5:
                    s = 0;
                    break;
                default:
                    s = 2;
            }
            return getHand().subList(s, getHand().size());
        }
    }

    /**
     * @return whether this {@link Player} could take an action on their next turn
     */
    public boolean isActive() {
        return getBalance() != 0 && getHand() != null;
    }

    /**
     * @return whether this {@link Player} still has either money or cards
     */
    public boolean isAlive() {
        return getBalance() != 0 || getHand() != null;
    }

    /**
     * @return the next {@link Player} by turn order
     */
    public Player next() {
        return next;
    }

    public void setAction(Action newAction) {
        action = newAction;
    }

    public void setHand(List<Card> newHand) {
        hand = newHand;
    }

    public TextCanvas toCanvas() {
        TextCanvas result;
        if (Main.round == 5) {
            result = new TextCanvas(String.format(
                    "\u250C\u2500\u2500\u2510              \u250C\u2500\u2500\u2500\u2500\u2510\n\u2502P%d\u2502              \u2502$%03d\u2502\n\u251C\u2500\u2500\u2534\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2534\u2500\u2500\u2500\u2500\u2524\n\u2502                      \u2502\n\u2502                      \u2502\n\u2502                      \u2502\n\u2502                      \u2502\n\u2514\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2518",
                    getId() + 1, getBalance()));
            if (getHand() != null) {
                for (int i = 0; i < getHand().size(); i++) {
                    result.draw(3 * i + 2, 4, getHand().get(i).toCanvas());
                }
            }
        } else {
            result = new TextCanvas(String.format(
                    "\u250C\u2500\u2500\u2510              \u250C\u2500\u2500\u2500\u2500\u2510\n\u2502P%d\u2502              \u2502$%03d\u2502\n\u251C\u2500\u2500\u2534\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2534\u2500\u2500\u2500\u2500\u2524\n\u2502                      \u2502\n\u251C\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2524\n\u2502                      \u2502\n\u2502                      \u2502\n\u2502                      \u2502\n\u2502                      \u2502\n\u2514\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2518",
                    getId() + 1, getBalance()));
            if (getAction() != null) {
                result.draw((22 - getAction().toString().length() >> 1) + 1, 3, new TextCanvas(getAction().toString()));
            }
            if (getHand() != null) {
                result.draw(5, 6, Card.cardBack);
                result.draw(8, 6, Card.cardBack);
                if (Main.round == 4) {
                    result.draw(2, 6, Card.cardBack);
                    for (int i = 3; i < getHand().size(); i++) {
                        result.draw(3 * i + 2, 6, getHand().get(i).toCanvas());
                    }
                } else {
                    for (int i = 2; i < getHand().size(); i++) {
                        result.draw(3 * i + 5, 6, getHand().get(i).toCanvas());
                    }
                }

            }
        }
        if (this == dealer) {
            result.draw(16, 0, new TextCanvas("\u250C\u2500\u252C\n\u2502D\u2502\n\u2534\u2500\u2534"));
        }
        return result;
    }
}
