import java.util.Random;

class Board {

    Cell[][] board;

    public Board(int n, long seed) {
        board = new Cell[n][n];
        Random random = new Random(seed);
        for (int i = 0; i < n; ++i) {
            for (int j = 0; j < n; ++j) {
                board[i][j] = new Cell(random);
            }
        }
    }

    void printBoard() {
        for (Cell[] row : board) {
            for (Cell cell : row) {
                System.out.print(cell);
            }
            System.out.println();
        }
    }
}