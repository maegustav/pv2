import static apgas.Constructs.async;
import static apgas.Constructs.asyncAt;
import static apgas.Constructs.at;
import static apgas.Constructs.finish;
import static apgas.Constructs.here;
import static apgas.Constructs.places;

import java.util.concurrent.atomic.AtomicInteger;

import apgas.Place;
import apgas.util.GlobalRef;

class Start {

	public static void main(String[] args) {
		if (args.length < 2) {
			System.out.println("Aufruf: Start n seed");
			System.exit(1);
		}
		final int n = 24;//Integer.parseInt(args[0]);
		long seed = 123;//Long.parseLong(args[1]);
        final int p = places().size();
        final int t = 6; //Integer.parseInt(System.getProperty(Configuration.APGAS_THREADS));

        if (n % p != 0 || n < p * t) {
            System.out.println("n muss Vielfaches von p und groesser als pt sein!");
            System.exit(1);
        }
        
        Place placeA[] = new Place[p];
        for(Place place : places()) {
        	placeA[place.id] = place;
        }
        
		long start = System.currentTimeMillis();
		
		final Board board = new Board(n, seed);
//		board.printBoard();
		
		final GlobalRef<Board> gBoard = new GlobalRef<Board>(places(), () -> {
			return new Board(n,seed);
		});

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
		
		final GlobalRef<int[][][]> globalColPrefix = new GlobalRef<int[][][]>(places(), () -> {
			int[][][] myA = new int[n/p][n][10];
			final int indicesPerActivity = n / (p * t);
			finish( () -> {
				for (int k = 0; k < t; ++k) {
					final int fk = k;
					async(() -> {
						final int ind = (((n/p)%t >= fk) ? (fk) : ((n/p)%t));
//						for(int j = fk * indicesPerActivity; j < (fk + 1) * indicesPerActivity; j++) {
						for (int j = (fk * indicesPerActivity) + ind; j < ((fk + 1) * indicesPerActivity) + ind + (((n/p)%t > fk) ? (1) : (0)) ;  ++j) {
							for(int i = 0; i < n; i++) {
								for(Gift gift : gBoard.get().board[j + (here().id * (n/p))][i].contains) {
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
                	int[][][] myA = globalColPrefix.get();
                       while(place.id != at(gC.home(), () -> gC.get().get())) {}
                       for(int i = 0; i < n/p; i++) {
                    	   for(int j = 0; j < n; j++) {
                    		   for(int k = 0; k < 10; k++) {
//                    			   System.out.print("k " + k +": " + myA[j][i][k] + "  ");
                    		   }
//                    		   System.out.println();
                    	   }
                       }
                       at(gC.home(), () -> gC.get().addAndGet(1));
                });
            }
        }); 
		long start1 = System.currentTimeMillis();
		
		final GlobalRef<int[][][]> globalSum = new GlobalRef<int[][][]>(places(), () -> {
			int[][][] mySum = new int[(((n*n)+n)/2)][n/p][10];
			final int indicesPerActivity = (n/p)/t;
			finish( () -> {
				for (int k = 0; k < t; ++k) {
					final int fk = k;
					async(() -> {
						final int ind = (((n/p)%t >= fk) ? (fk) : ((n/p)%t));
//						for(int j = fk * indicesPerActivity; j < (fk + 1) * indicesPerActivity; j++) {
						for (int i = (fk * indicesPerActivity) + ind; i < ((fk + 1) * indicesPerActivity) + ind + (((n/p)%t > fk) ? (1) : (0)) ;  ++i) {
							int y = 0;
							for(int from = 0; from < n; from++) {
								for(int to = from; to < n; to++) {
									final int fto = to%(n/p);
									final int fi = i;
									if(from != to && from > 0) {
										for(int z = 0; z < 10; z++) {
											final int fz = z;
											final int ffrom = (from-1)%(n/p);
											mySum[y][i][z] = at(placeA[(to/(n/p))], () -> globalColPrefix.get()[fto][fi][fz] ) - 
													at(placeA[(from-1)/(n/p)], () -> globalColPrefix.get()[ffrom][fi][fz] );
										}
									} else {
										for(int z = 0; z < 10; z++) {
											final int fz = z;
											mySum[y][i][z] = colPrefix[fto][fi][fz];
										}
									}
									y++;
								}
							}
						} 
					});
				}
			});
            return mySum;
         });
		
		long end1 = System.currentTimeMillis();
		System.out.println(end1-start1 + "ms");
                

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
		
		final long[][] values = new long[((n*n)+n)/2][((n*n)+n)/2];
		finish( () -> {
			final int indices = (((n*n)+n)/2);
			final int indicesPerActivity = indices/t;
			for (int h = 0; h < t; ++h) {
				final int fh = h;
				async(() -> {
					final int ind = ((indices%t >= fh) ? (fh) : (indices%t));
					for (int i = (fh * indicesPerActivity) + ind;
							i < ((fh + 1) * indicesPerActivity) + ind + ((indices%t > fh) ? (1) : (0)) ; ++i) {
						int x = 0;
						final int fi = i;
						for(int from = 0; from < n; from++) {
							for(int to = from; to < n; to++) {
								int sum2[] = new int[10];
								for(int j = from; j <= to; j++) {
									final int fj = j%(n/p);
									int sum3[] = at(placeA[(j/(n/p))], () -> {
										int placeSum[] = new int[10];
										for(int k = 0; k < 10; k++) {
											final int fk = k;
											placeSum[k] += globalSum.get()[fi][fj][fk];
										}
										return placeSum;
									});
									for(int k = 0; k < 10; k++) {
										sum2[k] += sum3[k];
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
								values[i][x] = value;
								x++;
							}
						}
					}
					return;
				});
			}
		});
		

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