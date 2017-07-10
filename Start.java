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
		final int n = 80;//Integer.parseInt(args[0]);
		long seed = 123;//Long.parseLong(args[1]);
		final int p = places().size();
		final int t = 6; //Integer.parseInt(System.getProperty(Configuration.APGAS_THREADS));

		/* if (n % p != 0 || n < p * t) {
            System.out.println("n muss Vielfaches von p und groesser als pt sein!");
            System.exit(1);
        }*/

		Place placeA[] = new Place[p];
		for(Place place : places()) {
			placeA[place.id] = place;
		}

		long start = System.currentTimeMillis();

		final Board board = new Board(n, seed);
		//board.printBoard();

		final GlobalRef<int[][][]> globalPrefix = new GlobalRef<int[][][]>(new int[n][n][10]);
		final GlobalRef<int[][][]> globalFromToSum = new GlobalRef<int[][][]>(new int[((n*n)+n)/2][n][10]);

		final int indicesPerActivity = n / (p * t);

		//ColPrefix calc
		finish( ()->{
			for (Place place : placeA) {
				asyncAt(place,()->{
					for(int i = place.id; i < n; i += p) 
					{
						final int fi = i;
						async(()->{
							int[][][] tempPrefix = new int[n][n][10];
							for(int j = 0; j < n; j++) {
								for(Gift gift : board.board[j][fi].contains) {
									tempPrefix[j][fi][gift.ordinal()]++;
								}
								if(j > 0) {
									for(int k = 0; k < 10; k++) {
										tempPrefix[j][fi][k] += tempPrefix[j-1][fi][k];		
									}
								}
								final int fj = j;
								for(int k = 0; k < 10; k++) {

									final int fk = k;
									asyncAt(globalPrefix.home(), () -> {
										globalPrefix.get()[fj][fi][fk] = tempPrefix[fj][fi][fk];
									});
								}
							}
						});
					}
				});
			}
		});	

		long start1 = System.currentTimeMillis();
		//spaltenweise calc

		finish(() ->{
			int[][][] tempPrefix = at(globalPrefix.home(), () -> {
				return globalPrefix.get();
			});
			for (Place place : placeA) {
				asyncAt(place, ()->{
					for(int j = place.id; j < n; j += p) {
						final int fj = j;
						async(() -> {
							int y = 0;
							for(int from = 0; from < n; from++) {
								for(int to = from; to < n; to++){			
									final int fy = y;
									if(from != to && from > 0) {
										for(int k = 0; k < 10; k++) {
											final int fk = k;
											final int tempValue = tempPrefix[to][fj][fk] - tempPrefix[from-1][fj][fk];
											at(globalFromToSum.home(), () ->{
												globalFromToSum.get()[fy][fj][fk] = tempValue;
											});
										}
									}else if(from != 0) {
										for(int k = 0; k < 10; k++) {
											final int fk = k;
											final int tempValue = tempPrefix[to][fj][fk] - tempPrefix[to - 1][fj][fk];
											at(globalFromToSum.home(), () ->{
												globalFromToSum.get()[fy][fj][fk] = tempValue;
											});
										}
									}else{
										for(int k = 0; k < 10; k++) {
											final int fk = k;
											final int tempValue = tempPrefix[to][fj][fk];
											at(globalFromToSum.home(), () ->{
												globalFromToSum.get()[fy][fj][fk] = tempValue;
											});
										}
									}
									y++;
								}
							}
						});
					}
				});
			}
		});
		/*for(int aijhskgla = 0;aijhskgla < ((n*n)+n)/2; aijhskgla++){
			int coords[] = coordinates(aijhskgla, aijhskgla, n);
			System.out.print(coords[0]+ " " + coords[1] + " ");
			for(int j = 0; j < n ; j++){

				for(int i = 0; i< 10; i++){
					final int fi = i;
					final int faijhskgla = aijhskgla;
					final int fj = j;
					System.out.print(at(globalFromToSum.home(),()-> globalFromToSum.get()[faijhskgla][fj][fi]));
				}
				System.out.print("\t");	
			}
			System.out.println();
		}*/	

		long end1 = System.currentTimeMillis();
		System.out.println(end1-start1 + "ms");

		//berechnung sum
		at(globalFromToSum.home(), () -> {
			long sumValues[][] = new long[((n*n)+n)/2][((n*n)+n)/2];
			finish(() -> {
				for(int i = 0; i < ((n*n)+n)/2; i++) {
					final int fi = i;
					async(() -> {
						int y = 0;
						for(int from = 0; from < n; from++) {
							for(int to = from; to < n; to++) {
								int sum2[] = new int[10];
								for(int j = from; j <= to; j++) {
									for(int k = 0; k < 10; k++) {
										sum2[k] += globalFromToSum.get()[fi][j][k];
									}
								}
								for(int k = 0; k < 5; k++) {
									long a = sum2[k];
									if(sum2[k+5] > 0) {
										a = 0;
									}
									sumValues[fi][y] += (a*( (a+1)*((2*a)+1) ) )/6;
								}
								y++;
							}
						}
					});
				}
			});

			long max = 0;
			int i1 = 0;
			int j1 = 0;
			for(int i = 0; i < ((n*n)+n)/2; i++) {
				for(int j = 0; j < ((n*n)+n)/2; j++) {
					if(sumValues[i][j] > max) {
						synchronized(sumValues) {
							max = sumValues[i][j];
							i1 = i;
							j1 = j;
						}
					}
				}
			}
			int[] a = coordinates(i1, j1, n);

			System.out.println("i1 = " + a[0] + "\tj1 = " + a[2] + "\ti2 = " + a[1] + "\tj2 = " + a[3]);
			System.out.println("Wert: " + max);
		});

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