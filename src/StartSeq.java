import static apgas.Constructs.places;


import apgas.Place;

class StartSeq {

	public static void main(String[] args) {
		if (args.length < 2) {
			System.out.println("Aufruf: Start n seed");
			System.exit(1);
		}
		final int n = 120;//Integer.parseInt(args[0]);
		long seed = 123;//Long.parseLong(args[1]);
        final int p = places().size();
        final int t = 6; //Integer.parseInt(System.getProperty(Configuration.APGAS_THREADS));

        Place placeA[] = new Place[p];
        for(Place place : places()) {
        	placeA[place.id] = place;
        }
        
		long start = System.currentTimeMillis();
		
		final Board board = new Board(n, seed);
//		board.printBoard();
		
		int colPrefix[][][] = new int[n][n][10];

		//spalten praefixsummen
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
		
		int sum[][][] = new int[((n*n)+n)/2][n][10];
		//spaltenweise summen fuer zeilen
		int y = 0;
		for(int from = 0; from < n; from++) {
			for(int to = from; to < n; to++) {
				if(from != to && from > 0) {
					for(int i = 0; i < n; i++) {
						for(int k = 0; k < 10; k++) {
							sum[y][i][k] = colPrefix[to][i][k] - colPrefix[from-1][i][k];
						}
					}
				} else {
					for(int i = 0; i < n; i++) {
						for(int k = 0; k < 10; k++) {
							sum[y][i][k] = colPrefix[to][i][k];
						}
					}
				}
				y++;
			}
		}
		
		//berechnung sum
		long sumValues[][] = new long[((n*n)+n)/2][((n*n)+n)/2];
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
					long value = 0;
					for(int k = 0; k < 5; k++) {
						long a = sum2[k];
						if(sum2[k+5] > 0) {
							a = 0;
						}
						
						value += (a*( (a+1)*((2*a)+1) ) )/6;
					}
					sumValues[i][y] = value;
					y++;
				}
			}
		}
		
		long max = 0;
		int i1 = 0;
		int j1 = 0;
		for(int i = 0; i < ((n*n)+n)/2; i++) {
			for(int j = 0; j < ((n*n)+n)/2; j++) {
				if(sumValues[i][j] > max) {
					max = sumValues[i][j];
					i1 = i;
					j1 = j;
				}
			}
		}
		int[] a = coordinates(i1, j1, n);
		System.out.println("i1 = " + a[0] + "\tj1 = " + a[2] + "\ti2 = " + a[1] + "\tj2 = " + a[3]);
		System.out.println("Wert: " + max);
		
		long end = System.currentTimeMillis();
		System.out.println(end-start + "ms");
	}

	public static int[] coordinates(int i, int j, int n) {
		int a[] = new int[4];
		int count = 0;
		for(int k = 0; k < n; k++) {
			for(int l = k; l < n; l++) {
				if(count == i) {
					a[0] = k;
					a[1] = l;
				}
				if(count == j) {
					a[2] = k;
					a[3] = l;
				}
				count++;
			}
		}
		return a;
	}
}