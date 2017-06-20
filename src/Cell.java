import java.util.ArrayList;
import java.util.Random;

class Cell {

    ArrayList<Gift> contains;

    Cell(Random random) {
        contains = new ArrayList<Gift>();
        for (Gift gift : Gift.values()) {
            if (gift.isGood() && random.nextBoolean()) {
                // mit Wahrscheinlichkeit 0.5
                contains.add(gift);
            } else if (!gift.isGood() && random.nextInt(10) == 3) {
                // mit Wahrscheinlichkeit 0.1
                contains.add(gift);
            }
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(" ( ");
        for (Gift gift : contains) {
            sb.append(gift + " ");
        }
        sb.append(") ");
        for (int i = sb.length(); i < 25; ++i) {
            sb.append(" ");
        }
        return sb.toString();
    }
}

