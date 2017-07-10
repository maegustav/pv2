package samples;

import apgas.*;
import static apgas.Constructs.*;
import apgas.util.GlobalRef;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

final class Count3 {
    public static void main(String[] args) {
        final int n = Integer.parseInt(args[0]);
        final int p = places().size();
        final int t = 2;//Integer.parseInt(System.getProperty(Configuration.APGAS_THREADS));
        if (n % p != 0 || n < p * t) {
            System.out.println("n muss Vielfaches von p und groesser als pt sein!");
            System.exit(1);
        }
        // Initialisierung
        long start = System.currentTimeMillis();
        final GlobalRef<Integer[]> gA = new GlobalRef<Integer[]>( places(), () -> {
               Integer[] myA = new Integer[n/p];
               finish( () -> {
                       int indicesPerActivity = n / (p * t);
                       for (int k = 0; k < t; ++k) {
                           final int fk = k;
                           async(() -> {
                                   Random random   = new Random(t * here().id + fk);
                                   for (int i = fk * indicesPerActivity; i < (fk + 1) * indicesPerActivity;  ++i) {
                                       myA[i] = random .nextInt(10);
                                   }
                           });
                       }
               });
               return myA;
            });
        long end = System.currentTimeMillis();
        System.out.println("Time Init in ms: " + (end - start));
        // Berechnung
        start = System.currentTimeMillis();
        AtomicInteger sum = new AtomicInteger(0);
        final GlobalRef<AtomicInteger> gSum = new GlobalRef<AtomicInteger>(sum);
        finish(() -> {       
            for (final Place place: places()) {
                asyncAt(place, () -> {
                       AtomicInteger placeSum = new AtomicInteger(0);
                       Integer[] myA = gA.get();
                       int indicesPerActivity = n / (p * t);
                       finish(() -> {
                           for (int k = 0; k < t; ++k) {
                               final int fk = k;
                               async(() -> {
                                   int activitySum = 0;
                                   for (int i = fk * indicesPerActivity; i < (fk + 1) * indicesPerActivity;  ++i) {
                                       if (myA[i] == 3) { activitySum++; }
                                   }
                                   placeSum.addAndGet(activitySum);
                               });
                           }
                       });
                       final int finalPlaceSum = placeSum.get();
                       at(gSum.home(), () -> gSum.get().addAndGet(finalPlaceSum));
                });
            }
        });    
        end = System.currentTimeMillis();
        System.out.println("Time Computation in ms: " + (end - start));
        System.out.println("Number of 3's: " + sum.get());
    }
}