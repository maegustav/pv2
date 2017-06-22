class Start {

	public static void main(String[] args) {
		if (args.length < 2) {
			System.out.println("Aufruf: Start n seed");
			System.exit(1);
		}
		int n = 200;//Integer.parseInt(args[0]);
		long seed = 123;//Long.parseLong(args[1]);

		Board board = new Board(n, seed);
//		board.printBoard();

		int colPrefix[][][] = new int[n][n][10];
		int sum[][][] = new int[((n*n)+n)/2][n][10];

		//spalten präfixsummen
		for(int j = 0; j < n; j++) {
			for(int i = 0; i < n; i++) {
				for(Gift gift : board.board[j][i].contains) {
					colPrefix[j][i][gift.ordinal()]++;
				}
				if(j > 0) {
					for(int k = 0; k < 10; k++) {
						colPrefix[j][i][k] += colPrefix[j-1][i][k];
					}
				}
			}
		}
		
//		for(int i = 0; i < n; i++) {
//			for(int j = 0; j < n; j++) {
//				System.out.print("( ");
//				for(int k = 0; k < 10; k++) {
//					System.out.print("k" + k + ": " + colPrefix[i][j][k] + " ");
//				}
//				System.out.print(")\t");
//			}
//			System.out.println();
//		}
		
		//spaltenweise summen für zeilen
		int y = 0;
		for(int from = 0; from < n; from++) {
			for(int to = from; to < n; to++) {
//				System.out.print(from + ", " + to + ": ");
				if(from != to && from > 0) {
					for(int i = 0; i < n; i++) {
//						System.out.print("( ");
						for(int k = 0; k < 10; k++) {
							sum[y][i][k] = colPrefix[to][i][k] - colPrefix[from-1][i][k];
//							System.out.print("L" + k + ": " + sum[y][i][k] + " ");
						}
//						System.out.print(")\t");
					}
				} else {
					for(int i = 0; i < n; i++) {
//						System.out.print("( ");
						for(int k = 0; k < 10; k++) {
							sum[y][i][k] = colPrefix[to][i][k];
//							System.out.print("k" + k + ": " + sum[y][i][k] + " ");
						}
//						System.out.print(")\t");
					}
				}
				y++;
//				System.out.println();
			}
		}

//		System.out.println("sum");
//		for(int i = 0; i < ((n*n)+n)/2; i++) {
//			System.out.print("from: " + i/n + " to: " + i%n + ": ");
//			for(int j = 0; j < n; j++) {
//				System.out.print("( ");
//				for(int k = 0; k < 10; k++) {
//					System.out.print("k" + k + ": " + sum[i][j][k] + " ");
//				}
//				System.out.print(")\t");
//			}
//			System.out.println();
//		}

		//berechnung sum
		long values[][] = new long[((n*n)+n)/2][((n*n)+n)/2];
		for(int i = 0; i < ((n*n)+n)/2; i++) {
			y = 0;
			for(int from = 0; from < n; from++) {
				for(int to = from; to < n; to++) {
					int sum2[] = new int[10];
					for(int j = from; j <= to; j++) {
						for(int k = 0; k < 10; k++) {
							sum2[k] += sum[i][j][k];
						}
					}
					
					for(int k = 0; k < 5; k++) {
						long a = sum2[k];
						if(sum2[k+5] > 0) {
							a = 0;
						}
						values[i][y] += (a*( (a+1)*((2*a)+1) ) )/6;
					}
					y++;
				}
			}
		}
		
		long max = 0;
		int i1 = 0;
		int i2 = 0;
		int j1 = 0;
		int j2 = 0;
		int count = 0;
		for(int i = 0; i < ((n*n)+n)/2; i++) {
			for(int j = 0; j < ((n*n)+n)/2; j++) {
//				System.out.println(count + ": " + values[i][j]);
				count++;
				if(values[i][j] > max) {
					max = values[i][j];
				}
			}
		}
		System.out.println(max);
	}
}