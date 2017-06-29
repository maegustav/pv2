import static apgas.Constructs.async;
import static apgas.Constructs.asyncAt;
import static apgas.Constructs.at;
import static apgas.Constructs.finish;
import static apgas.Constructs.here;
import static apgas.Constructs.places;

import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import apgas.Configuration;
import apgas.Place;
import apgas.util.GlobalRef;

class Start {

	public static void main(String[] args) {
		if (args.length < 2) {
			System.out.println("Aufruf: Start n seed");
			System.exit(1);
		}
		int n = 80;//Integer.parseInt(args[0]);
		long seed = 123;//Long.parseLong(args[1]);
        final int p = places().size();
        final int t = 6; //Integer.parseInt(System.getProperty(Configuration.APGAS_THREADS));

		long start = System.currentTimeMillis();
		
		Board board = new Board(n, seed);
//		board.printBoard();

		int colPrefix[][][] = new int[n][n][10];
		int sum[][][] = new int[((n*n)+n)/2][n][10];

		
		
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
		
		final GlobalRef<Integer[][][]> gA = new GlobalRef<Integer[][][]>( places(), () -> {
            Integer[][][] myA = new Integer[n/p][n][10];
            finish( () -> {
                    int indicesPerActivity = n / (p * t);
                    for (int k = 0; k < t; ++k) {
                        final int fk = k;
                        async(() -> {
                                for (int j = fk * indicesPerActivity; j < (fk + 1) * indicesPerActivity;  ++j) {
                                	for(int i = 0; i < n; i++) {
                        				for(Gift gift : board.board[j + (here().id * indicesPerActivity)][i].contains) {
                        					myA[j][i][gift.ordinal()] += 1;
                        				}
                        				if(j > 0) {
                        					for(int m = 0; m < 10; m++) {
                        						myA[j][i][m] += myA[j-1][i][m];
                        					}
                        				}
                        			}
                                }
                        });
                    }
            });
            return myA;
         });
	    
		AtomicInteger c = new AtomicInteger(0);
		final GlobalRef<AtomicInteger> gC = new GlobalRef<AtomicInteger>(c);
		finish(() -> {   
            for (final Place place: places()) {
                asyncAt(place, () -> {
                       Integer[][][] myA = gA.get();
                       
                       int indicesPerActivity = n / (p * t);
                       while(place.id != at(gC.home(), () -> gC.get().get())) {
                       }
                       for(int i = 0; i < n/p; i++) {
                    	   for(int j = 0; j < n; j++) {
                    		   for(int k = 0; k < 10; k++) {
                    			   System.out.print("k " + k +": " + myA[j][i][k] + "  ");
                    		   }
                    	   }
                       }
                       at(gC.home(), () -> gC.get().addAndGet(1));
                });
            }
        }); 
		
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
		int j1 = 0;
		for(int i = 0; i < ((n*n)+n)/2; i++) {
			for(int j = 0; j < ((n*n)+n)/2; j++) {
				if(values[i][j] > max) {
					max = values[i][j];
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