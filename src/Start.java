class Start {

    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Aufruf: Start n seed");
            System.exit(1);
        }
        int n = Integer.parseInt(args[0]);
        long seed = Long.parseLong(args[1]);

        Board board = new Board(n, seed);
        board.printBoard();
    }
}