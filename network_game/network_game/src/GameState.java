package network_game.src;

import java.util.*;

public class GameState {

    private final Map<String, Deque<Card>> hands = new HashMap<>();
    private final Deque<Card> deck = new ArrayDeque<>();
    private Card center;
    private String winner;

    public GameState(List<String> players) {
        for (int i = 1; i <= 50; i++)
            deck.add(new Card(i));
        Collections.shuffle((List<?>) deck);

        for (String p : players) {
            Deque<Card> h = new ArrayDeque<>();
            for (int i = 0; i < 5; i++)
                h.add(deck.pop());
            hands.put(p, h);
        }

        center = deck.pop();
        System.out.println("[GAME] start center=" + center);
    }

    public boolean playCard(String name, Card c) {
        Deque<Card> h = hands.get(name);
        if (h == null || !h.contains(c)) return false;
        if (c.number <= center.number) return false;

        h.remove(c);
        center = c;

        if (h.isEmpty())
            winner = name;

        return true;
    }

    public boolean isFinished() {
        return winner != null;
    }

    public String getWinner() {
        return winner;
    }

    public Card getCenterTop() {
        return center;
    }

    public String getHandString(String name) {
        StringBuilder sb = new StringBuilder();
        for (Card c : hands.get(name))
            sb.append(c).append(",");
        return sb.toString();
    }
}
