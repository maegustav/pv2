import java.io.Serializable;

// Aufzaehlungstypen sind in Moessenboeck: Sprechen Sie Java
// beschrieben.  Beispielsweise kann man mittels Gift.values() auf ein
// Array mit allen Werten des Typs und mit gift.ordinal() auf die
// Nummer eines konkreten Wertes zugreifen. (Die Werte sind mit 0
// beginnend durchnummeriert).)
enum Gift implements Serializable {
    AFFE("A", true),
    BALL("B", true),
    CLOWN("C", true),
    DONUT("D", true),
    EDELSTEIN("E", true),
    ANTI_AFFE("-A", false),
    ANTI_BALL("-B", false),
    ANTI_CLOWN("-C", false),
    ANTI_DONUT("-D", false),
    ANTI_EDELSTEIN("-E", false);

    private static final long serialVersionUID = 12345L;
    private String abbr;
    private boolean good;

    Gift(String abbr, boolean good) {
        this.abbr = abbr;
        this.good = good;
    }

    @Override
    public String toString() {
        return abbr;
    }

    boolean isGood() {
        return good;
    }
}