package network_game;

public class Card {
    final int number;

    Card(int n) {
        number = n;
    }

    static Card fromString(String s) {
        return new Card(Integer.parseInt(s));
    }

    @Override
    public String toString() {
        return String.valueOf(number);
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Card && ((Card) o).number == number;
    }

    @Override
    public int hashCode() {
        return number;
    }
}
