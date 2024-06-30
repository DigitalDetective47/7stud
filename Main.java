import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.InputMismatchException;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeSet;

public class Main {
    public static final int[] antes = { 1, 2, 3, 4, 5, 10, 15, 20, 25, 30, 40, 50, 75, 100, 125, 150, 175, 200, 250,
            300, 350, 400, 500, 600, 700 };
    public static final Scanner input = new Scanner(System.in);
    public static final Random random = new Random();

    public static int round; // 0-4 are betting, 5 is showdown

    public static void main(String[] args) throws InterruptedException, IOException {
        Player.setPlayerCount(0);
        System.out.print("How many players are playing? (Type an integer from 2 to " + Player.maxPlayers + ") ");
        try {
            Player.setPlayerCount(input.nextInt());
        } catch (IllegalArgumentException | InputMismatchException e) {
        }
        input.nextLine();
        while (Player.players.length < 2 || Player.players.length > Player.maxPlayers) {
            System.err.print("Type an integer from 2 to " + Player.maxPlayers + ". ");
            try {
                Player.setPlayerCount(input.nextInt());
            } catch (IllegalArgumentException | InputMismatchException e) {
            }
            input.nextLine();
        }
        int ante = antes[0];
        int anteIndex = 0;
        boolean continueGame;
        int handsSinceLastAnteIncrease = 0;
        int residual = 0;
        do {
            List<Card> deck = Card.deck();
            boolean foundPlayer;
            Pot.createNextPot();
            Pot.pots.get(0).addMoney(residual);
            for (Player p : Player.players) {
                if (p.getBalance() != 0) {
                    p.setHand(new ArrayList<>(7));
                    p.getHand().addAll(Arrays.asList(deck.remove(deck.size() - 1), deck.remove(deck.size() - 1)));
                    p.act(new Action(ActionType.BET, Math.min(ante, p.getBalance())));
                    if (Pot.pots.get(Pot.pots.size() - 1).getEligiblePlayers().size() == 1) {
                        Pot.pots.remove(Pot.pots.size() - 1).moveMoney(Arrays.asList(p));
                    }
                    p.setAction(null);
                }
            }
            for (round = 0; round < 5 && !Pot.pots.isEmpty(); round++) {
                if (round == 4) {
                    for (Player p : Player.players) {
                        if (p.getHand() != null) {
                            p.getHand().add(0, deck.remove(deck.size() - 1));
                        }
                    }
                } else {
                    for (Player p : Player.players) {
                        if (p.getHand() != null) {
                            p.getHand().add(deck.remove(deck.size() - 1));
                        }
                    }
                }

                foundPlayer = false;
                continueGame = false;
                for (Player p : Player.players) {
                    if (p.isActive()) {
                        if (foundPlayer) {
                            continueGame = true;
                            break;
                        } else {
                            foundPlayer = true;
                        }
                    }
                }
                if (continueGame) {
                    Player turn = Player.dealer.next();
                    while (turn.getHand() == null) {
                        turn = turn.next();
                    }
                    HandRank b = new HandRank(turn.getUpCards());
                    Player scan = turn;
                    while (scan != Player.dealer) {
                        scan = scan.next();
                        final HandRank c = new HandRank(scan.getUpCards());
                        if (scan.getHand() != null && b.compareTo(c) < 0) {
                            b = c;
                            turn = scan;
                        }
                    }
                    while (!turn.isActive()) {
                        turn = turn.next();
                    }
                    Player lastRaise = turn;
                    int minRaise = ante;
                    do {
                        cls();

                        final EnumSet<ActionType> allowedActions = EnumSet.of(ActionType.FOLD);
                        int maxRaise = 0;
                        int richest = 0;
                        for (Player p : Player.players) {
                            if (p.getHand() != null) {
                                final int a = p.getAvailableMoney();
                                if (a > richest) {
                                    maxRaise = richest;
                                    richest = a;
                                } else if (a > maxRaise) {
                                    maxRaise = a;
                                }
                            }
                        }
                        if (turn.getAvailableMoney() < maxRaise) {
                            maxRaise = turn.getAvailableMoney();
                        }
                        if (minRaise < maxRaise) {
                            if (turn == lastRaise || lastRaise.getRoundInvestment() == 0) {
                                allowedActions.add(ActionType.BET);
                                allowedActions.add(ActionType.CHECK);
                            } else {
                                allowedActions.add(ActionType.RAISE);
                                allowedActions.add(ActionType.CALL);
                            }
                        } else {
                            allowedActions.add(ActionType.ALL_IN);
                            if (turn == lastRaise || lastRaise.getRoundInvestment() == 0) {
                                allowedActions.add(ActionType.CHECK);
                            } else if (maxRaise > lastRaise.getRoundInvestment()) {
                                allowedActions.add(ActionType.CALL);
                            }
                        }

                        final TextCanvas actionBar = new TextCanvas("\u250C\n\u2524\n\u2514");
                        final FormattedCharacter horBar = new FormattedCharacter('\u2500', null, null);
                        for (ActionType entry : allowedActions) {
                            final int oldWidth = actionBar.getWidth();
                            actionBar.setWidth(actionBar.getWidth() + entry.toString().length() + 1);
                            for (int x = oldWidth; x < actionBar.getWidth() - 1; x++) {
                                actionBar.set(x, 0, horBar);
                                actionBar.set(x, 2, horBar);
                            }
                            actionBar.draw(oldWidth, 1, new TextCanvas(entry.toString()));
                            actionBar.draw(actionBar.getWidth() - 1, 0, new TextCanvas("\u252C\n\u2502\n\u2534"));
                        }
                        actionBar.draw(actionBar.getWidth() - 1, 0, new TextCanvas("\u2510\n\u251C\n\u2518"));
                        String cmd;

                        System.out.println("Player " + (turn.getId() + 1) + ", it's your turn!");
                        input.nextLine();
                        cls();

                        scan = turn.next();
                        while (!scan.isAlive()) {
                            scan = scan.next();
                        }
                        TextCanvas rowDisplay = scan.toCanvas();
                        do {
                            scan = scan.next();
                        } while (!scan.isAlive());
                        while (scan != turn) {
                            if (rowDisplay.getWidth() == 74) {
                                System.out.println(rowDisplay);
                                System.out.println();
                                rowDisplay = scan.toCanvas();
                            } else {
                                rowDisplay.setWidth(rowDisplay.getWidth() + 25);
                                rowDisplay.draw(rowDisplay.getWidth() - 24, 0, scan.toCanvas());
                            }
                            do {
                                scan = scan.next();
                            } while (!scan.isAlive());
                        }
                        System.out.println(rowDisplay);
                        if (Pot.pots.size() == 1) {
                            System.out.println(String.format(
                                    "\n\u250C\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2510\n\u2502POT: $%03d\u2502\n\u2514\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2518\n",
                                    Pot.pots.get(0).getBalance()));
                        } else {
                            rowDisplay = new TextCanvas((Pot.pots.get(1).getEligiblePlayers().size() << 2) + 19,
                                    (Pot.pots.size() << 1)
                                            + (Pot.pots.get(Pot.pots.size() - 1).getEligiblePlayers().contains(turn) ? 3
                                                    : 5));
                            rowDisplay.draw(0, 2, new TextCanvas(
                                    "\u251C\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\n\u2502SIDE POT 1 (\n\u251C\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500"));
                            Iterator<Player> e = Pot.pots.get(1).getEligiblePlayers().iterator();
                            for (int x = 12; e.hasNext(); x += 4) {
                                rowDisplay.draw(x, 3,
                                        new TextCanvas(String.format(
                                                "\u2500\u2500\u2500\u2500\nP%d, \n\u2500\u2500\u2500\u2500",
                                                e.next().getId())));
                            }
                            rowDisplay.draw(rowDisplay.getWidth() - 7, 3,
                                    new TextCanvas(String.format(
                                            "\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2510\n): $%03d\u2502\n\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2518",
                                            Pot.pots.get(1).getBalance())));
                            rowDisplay.draw(0, 0, new TextCanvas(String.format(
                                    "\u250C\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2510\n\u2502MAIN POT: $%03d\u2502",
                                    Pot.pots.get(0).getBalance())));
                            rowDisplay.set(15, 2, new FormattedCharacter('\u2534', null, null));
                            final FormattedCharacter tee = new FormattedCharacter('\u252C', null, null);
                            for (int i = 2; i < Pot.pots.size(); i++) {
                                rowDisplay.draw(0, (i << 1) + 2, new TextCanvas(String.format(
                                        "\u2502SIDE POT %d (\n\u251C\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500",
                                        i)));
                                e = Pot.pots.get(i).getEligiblePlayers().iterator();
                                int x;
                                for (x = 12; e.hasNext(); x += 4) {
                                    rowDisplay.draw(x, (i << 1) + 3,
                                            new TextCanvas(String.format(
                                                    "P%d, \n\u2500\u2500\u2500\u2500",
                                                    e.next().getId())));
                                }
                                rowDisplay.draw(x - 2, (i << 1) + 3,
                                        new TextCanvas(String.format(
                                                "): $%03d\u2502\n\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2518",
                                                Pot.pots.get(i).getBalance())));
                                rowDisplay.set(x + 5, (i << 1) + 2, tee);
                            }
                            int sum;
                            if (!Pot.pots.get(Pot.pots.size() - 1).getEligiblePlayers().contains(turn)) {
                                rowDisplay.set(17, rowDisplay.getHeight() - 5, tee);
                                sum = 0;
                                for (Pot p : Pot.pots) {
                                    if (p.getEligiblePlayers().contains(turn)) {
                                        sum += p.getBalance();
                                    } else {
                                        break;
                                    }
                                }
                                rowDisplay.draw(0, rowDisplay.getHeight() - 4, new TextCanvas(String.format(
                                        "\u2502YOUR TOTAL: $%03d\u2502\n\u251C\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2518",
                                        sum)));
                            }
                            rowDisplay.set(12, rowDisplay.getHeight() - 3, tee);
                            sum = 0;
                            for (Pot p : Pot.pots) {
                                sum += p.getBalance();
                            }
                            rowDisplay.draw(0, rowDisplay.getHeight() - 4, new TextCanvas(String.format(
                                    "\u2502TOTAL: $%03d\u2502\n\u251C\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2518",
                                    sum)));
                            System.out.println(rowDisplay);
                        }
                        rowDisplay = turn.toCanvas();
                        rowDisplay.setHeight(11);
                        rowDisplay.draw(3, 0, new TextCanvas(
                                "\u252C\u2500\u2500\u2500\u2510\n\u2502YOU\u2502\n\u2534\u2500\u2500\u2500\u2534"));
                        if (round == 4) {
                            for (int i = 0; i < 3; i++) {
                                rowDisplay.draw(3 * i + 2, 6, turn.getHand().get(i).toCanvas());
                                rowDisplay.set(3 * i + 3, 7, new FormattedCharacter(' ', null, Color.RED));
                            }
                        } else {
                            for (int i = 0; i < 2; i++) {
                                rowDisplay.draw(3 * i + 5, 6, turn.getHand().get(i).toCanvas());
                                rowDisplay.set(3*i+6, 7, new FormattedCharacter(' ', null, Color.RED));
                            }
                        }
                        rowDisplay.draw((24 - actionBar.getWidth()) >> 1, 8, actionBar);
                        System.out.println(rowDisplay);

                        do {
                            cmd = input.nextLine().toUpperCase();
                            if (cmd.equals("FOLD")) {
                                turn.act(new Action(ActionType.FOLD, 0));
                                break;
                            } else if (allowedActions.contains(ActionType.CALL) && cmd.equals("CALL")) {
                                turn.act(new Action(ActionType.CALL, lastRaise.getRoundInvestment()));
                                break;
                            } else if (allowedActions.contains(ActionType.CHECK) && cmd.equals("CHECK")) {
                                turn.act(new Action(ActionType.CHECK, 0));
                                break;
                            } else if (allowedActions.contains(ActionType.ALL_IN) && cmd.equals("ALL-IN")) {
                                System.out.println('a');
                                if (maxRaise > lastRaise.getRoundInvestment()) {
                                    if (maxRaise > minRaise) {
                                        minRaise = (maxRaise << 1) - lastRaise.getRoundInvestment();
                                    } else {
                                        minRaise += turn.getBalance();
                                    }
                                    lastRaise = turn;
                                }
                                turn.act(new Action(ActionType.ALL_IN, maxRaise));
                                break;
                            } else if (allowedActions.contains(ActionType.BET) && cmd.startsWith("BET ")) {
                                try {
                                    int raiseAmount = Integer.parseInt(cmd, 4, cmd.length(), 10);
                                    ActionType t;
                                    if (raiseAmount < minRaise) {
                                        throw new IllegalArgumentException();
                                    } else if (raiseAmount >= maxRaise) {
                                        raiseAmount = maxRaise;
                                        t = ActionType.ALL_IN;
                                    } else {
                                        t = ActionType.BET;
                                    }
                                    lastRaise = turn;
                                    minRaise = raiseAmount << 1;
                                    turn.act(new Action(t, raiseAmount));
                                    break;
                                } catch (IndexOutOfBoundsException | NumberFormatException e) {
                                    System.err.println(e);
                                    System.err.println("Unrecognized command.");
                                } catch (IllegalArgumentException e) {
                                    System.err.println("You must bet at least $" + minRaise + ".");
                                }
                            } else if (allowedActions.contains(ActionType.RAISE) && cmd.startsWith("RAISE ")) {
                                try {
                                    int raiseAmount = Integer.parseInt(cmd, 6, cmd.length(), 10);
                                    ActionType t;
                                    if (raiseAmount < minRaise) {
                                        throw new IllegalArgumentException();
                                    } else if (raiseAmount >= maxRaise) {
                                        raiseAmount = maxRaise;
                                        t = ActionType.ALL_IN;
                                    } else {
                                        t = ActionType.RAISE;
                                    }
                                    minRaise = (raiseAmount << 1) - lastRaise.getRoundInvestment();
                                    lastRaise = turn;
                                    turn.act(new Action(t, raiseAmount));
                                    break;
                                } catch (IndexOutOfBoundsException | NumberFormatException e) {
                                    System.err.println("Unrecognized command.");
                                } catch (IllegalArgumentException e) {
                                    System.err.println("You must raise to at least $" + minRaise + ".");
                                }
                            } else {
                                System.err.println("Unrecognized command.");
                            }
                        } while (true);
                        do {
                            turn = turn.next();
                        } while (!(turn.isActive() || turn == lastRaise));
                    } while (turn != lastRaise && !Pot.pots.isEmpty());
                    for (Pot p : Pot.pots) {
                        p.setMaxInvestment(Math.max(p.getMaxInvestment() - lastRaise.getRoundInvestment(), 0));
                    }
                    for (Player p : Player.players) {
                        p.setAction(null);
                    }
                }
            }
            cls();

            handsSinceLastAnteIncrease++;
            if (anteIndex != antes.length
                    && handsSinceLastAnteIncrease > 20 * Math.pow(ante, Math.log10(2)) / Player.players.length) {
                anteIndex++;
                ante = antes[anteIndex];
                handsSinceLastAnteIncrease = 0;
            }

            if (!Pot.pots.isEmpty()) {
                System.out.println("Everybody look, it's time for a showdown!");
                input.nextLine();
                cls();
                HandRank best = HandRank.worst;
                final Set<Player> winners = new TreeSet<>(Player.sortById);
                TextCanvas rowDisplay = null;
                for (Player p : Player.players) {
                    if (p.getHand() != null) {
                        final HandRank h = new HandRank(p.getHand());
                        final int c = h.compareTo(best);
                        if (c > 0) {
                            best = h;
                            winners.clear();
                            winners.add(p);
                        } else if (c == 0) {
                            winners.add(p);
                        }
                    }
                }
                for (Player p : Player.players) {
                    if (p.isAlive()) {
                        if (rowDisplay == null) {
                            rowDisplay = p.toCanvas();
                        } else if (rowDisplay.getWidth() == 74) {
                            System.out.println(rowDisplay);
                            System.out.println();
                            rowDisplay = p.toCanvas();
                        } else {
                            rowDisplay.setWidth(rowDisplay.getWidth() + 25);
                            rowDisplay.draw(rowDisplay.getWidth() - 24, 0, p.toCanvas());
                        }
                        if (winners.contains(p)) {
                            rowDisplay.draw(rowDisplay.getWidth() - 21, 0, new TextCanvas(
                                    "\u252C\u2500\u2510\n\u2502\33[33m\u2605\33[0m\u2502\n\u2534\u2500\u2534"));
                        }
                    }
                }
                System.out.println(rowDisplay);
                if (Pot.pots.size() == 1) {
                    System.out.println(String.format(
                            "\n\u250C\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2510\n\u2502POT: $%03d\u2502\n\u2514\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2518",
                            Pot.pots.get(0).getBalance()));
                    input.nextLine();
                    cls();
                    Pot.pots.get(0).moveMoney(winners);
                    residual = Pot.pots.remove(0).removeMoney();
                } else {
                    rowDisplay = new TextCanvas((Pot.pots.get(1).getEligiblePlayers().size() << 2) + 19,
                            (Pot.pots.size() << 1) + 1);
                    rowDisplay.draw(0, 2, new TextCanvas(
                            "\u251C\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\n\u2502SIDE POT 1 (\n\u251C\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500"));
                    Iterator<Player> e = Pot.pots.get(1).getEligiblePlayers().iterator();
                    for (int x = 12; e.hasNext(); x += 4) {
                        rowDisplay.draw(x, 3,
                                new TextCanvas(String.format(
                                        "\u2500\u2500\u2500\u2500\nP%d, \n\u2500\u2500\u2500\u2500",
                                        e.next().getId())));
                    }
                    rowDisplay.draw(rowDisplay.getWidth() - 7, 3,
                            new TextCanvas(String.format(
                                    "\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2510\n): $%03d\u2502\n\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2518",
                                    Pot.pots.get(1).getBalance())));
                    rowDisplay.draw(0, 0, new TextCanvas(String.format(
                            "\u250C\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2510\n\u2502MAIN POT: $%03d\u2502",
                            Pot.pots.get(0).getBalance())));
                    rowDisplay.set(15, 2, new FormattedCharacter('\u2534', null, null));
                    final FormattedCharacter tee = new FormattedCharacter('\u252C', null, null);
                    for (int i = 2; i < Pot.pots.size(); i++) {
                        rowDisplay.draw(0, (i << 1) + 2, new TextCanvas(String.format(
                                "\u2502SIDE POT %d (\n\u251C\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500",
                                i)));
                        e = Pot.pots.get(i).getEligiblePlayers().iterator();
                        int x;
                        for (x = 12; e.hasNext(); x += 4) {
                            rowDisplay.draw(x, (i << 1) + 3,
                                    new TextCanvas(String.format(
                                            "P%d, \n\u2500\u2500\u2500\u2500",
                                            e.next().getId())));
                        }
                        rowDisplay.draw(x - 2, (i << 1) + 3,
                                new TextCanvas(String.format(
                                        "): $%03d\u2502\n\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2518",
                                        Pot.pots.get(i).getBalance())));
                        rowDisplay.set(x + 5, (i << 1) + 2, tee);
                    }
                    rowDisplay.set(0, rowDisplay.getHeight() - 1, new FormattedCharacter('\u251C', null, null));
                    System.out.println(rowDisplay);
                    input.nextLine();
                    cls();
                    Pot.pots.get(0).moveMoney(winners);
                    residual = Pot.pots.remove(0).removeMoney();
                    for (int firstPotId = 1; !Pot.pots.isEmpty(); firstPotId++) {
                        Pot.pots.get(0).addMoney(residual);
                        for (Player p : Player.players) {
                            if (!Pot.pots.get(0).getEligiblePlayers().contains(p)) {
                                p.setHand(null);
                            }
                        }
                        best = HandRank.worst;
                        winners.clear();
                        rowDisplay = null;
                        for (Player p : Player.players) {
                            if (p.getHand() != null) {
                                final HandRank h = new HandRank(p.getHand());
                                final int c = h.compareTo(best);
                                if (c > 0) {
                                    best = h;
                                    winners.clear();
                                    winners.add(p);
                                } else if (c == 0) {
                                    winners.add(p);
                                }
                            }
                        }
                        for (Player p : Player.players) {
                            if (p.isAlive()) {
                                if (rowDisplay == null) {
                                    rowDisplay = p.toCanvas();
                                } else if (rowDisplay.getWidth() == 74) {
                                    System.out.println(rowDisplay);
                                    System.out.println();
                                    rowDisplay = p.toCanvas();
                                } else {
                                    rowDisplay.setWidth(rowDisplay.getWidth() + 25);
                                    rowDisplay.draw(rowDisplay.getWidth() - 24, 0, p.toCanvas());
                                }
                                if (winners.contains(p)) {
                                    rowDisplay.draw(rowDisplay.getWidth() - 21, 0, new TextCanvas(
                                            "\u252C\u2500\u2510\n\u2502\33[33m\u2605\33[0m\u2502\n\u2534\u2500\u2534"));
                                }
                            }
                        }
                        System.out.println(rowDisplay);
                        rowDisplay = new TextCanvas((Pot.pots.get(0).getEligiblePlayers().size() << 2) + 19,
                                (Pot.pots.size() << 1) + 1);
                        for (int i = 0; i < Pot.pots.size(); i++) {
                            rowDisplay.draw(0, (i << 1) + 2, new TextCanvas(String.format(
                                    "\u2502SIDE POT %d (\n\u251C\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500",
                                    firstPotId + i)));
                            e = Pot.pots.get(i).getEligiblePlayers().iterator();
                            int x;
                            for (x = 12; e.hasNext(); x += 4) {
                                rowDisplay.draw(x, (i << 1) + 3,
                                        new TextCanvas(String.format(
                                                "P%d, \n\u2500\u2500\u2500\u2500",
                                                e.next().getId())));
                            }
                            rowDisplay.draw(x - 2, (i << 1) + 3,
                                    new TextCanvas(String.format(
                                            "): $%03d\u2502\n\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2518",
                                            Pot.pots.get(i).getBalance())));
                            rowDisplay.set(x + 5, (i << 1) + 2, tee);
                        }
                        rowDisplay.set(0, rowDisplay.getHeight() - 1, new FormattedCharacter('\u251C', null, null));
                        System.out.println(rowDisplay);
                        input.nextLine();
                        cls();
                        Pot.pots.get(0).moveMoney(winners);
                        residual = Pot.pots.remove(0).removeMoney();
                    }
                }
            }

            continueGame = false;
            foundPlayer = false;
            for (Player p : Player.players) {
                p.setHand(null);
                if (p.getBalance() != 0) {
                    if (foundPlayer) {
                        continueGame = true;
                    } else {
                        foundPlayer = true;
                    }
                }
            }
            if (continueGame) {
                System.out.println("ROUND SUMMARY:\n");
                for (Player p : Player.players) {
                    System.out.println(p.getBalance() == 0 ? String.format("P%d - \33[31mBANKRUPT\33[0m", p.getId() + 1)
                            : String.format("P%d- $%03d", p.getId() + 1, p.getBalance()));
                }
                System.out.println(String.format("\nAnte next hand is $%d", ante));
                input.nextLine();
            } else {
                break;
            }
        } while (true);
        input.close();
        for (Player p : Player.players) {
            if (p.getBalance() != 0) {
                System.out.println(String.format("PLAYER %d WINS", p.getId() + 1));
                break;
            }
        }
    }

    public static void cls() throws InterruptedException, IOException {
        if (System.getProperty("os.name").contains("Windows")) {
            new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
        } else {
            Runtime.getRuntime().exec("clear");
        }
    }
}
