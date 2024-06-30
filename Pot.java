import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class Pot extends MoneyContainer {
    public static final List<Pot> pots = new ArrayList<>(Player.maxPlayers - 1);

    public static void createNextPot() {
        int cap = Player.players.length * Player.startingBalance;
        final TreeSet<Player> eligiblePlayers = new TreeSet<>(Player.sortById);
        final int minCap = pots.isEmpty() ? 0 : pots.get(pots.size() - 1).getMaxInvestment();
        for (Player p : Player.players) {
            if (p.getBalance() > minCap) {
                eligiblePlayers.add(p);
                if (p.getBalance() < cap) {
                    cap = p.getBalance();
                }
            }
        }
        pots.add(new Pot(eligiblePlayers, cap));
    }

    private Set<Player> eligiblePlayers;
    private int maxInvestment;

    private Pot(Set<Player> eligiblePlayers, int maxInvestment) {
        super(0);
        setEligiblePlayers(eligiblePlayers);
        setMaxInvestment(maxInvestment);
    }

    public Set<Player> getEligiblePlayers() {
        return eligiblePlayers;
    }

    public int getMaxInvestment() {
        return maxInvestment;
    }

    public void setEligiblePlayers(Set<Player> newEligiblePlayers) {
        eligiblePlayers = newEligiblePlayers;
    }

    public void setMaxInvestment(int newMaxInvestment) {
        maxInvestment = newMaxInvestment;
    }
}
