
package com.minkang.ultimate.pixelrating;

public class EloCalculator {
    public enum Outcome { WIN, LOSS, DRAW }
    public static class Result { public final int newA; public final int newB; public Result(int a,int b){newA=a;newB=b;} }

    public static Result calculate(UltimatePixelmonRatingPlugin p, int a, int b, Outcome outA){
        double kA=k(p,a), kB=k(p,b);
        double expFactor = p.getConfig().getDouble("rating.diff-exp", 1.00);
        double eA = 1.0/(1.0+Math.pow(10.0,(b-a)/(400.0*expFactor)));
        double sA;
        if (outA==Outcome.WIN) sA=1.0;
        else if (outA==Outcome.LOSS) sA=0.0;
        else sA=p.getConfig().getDouble("rating.draw-score",0.5);
        int na=(int)Math.round(a + kA*(sA - eA));
        int nb=(int)Math.round(b + kB*((1.0 - sA) - (1.0/(1.0+Math.pow(10.0,(a-b)/(400.0*expFactor))))));

        int maxDelta = p.getConfig().getInt("rating.max-delta", 0);
        int minDelta = p.getConfig().getInt("rating.min-delta", 0);
        if (maxDelta>0){
            na = clampDelta(a, na, maxDelta);
            nb = clampDelta(b, nb, maxDelta);
        }
        if (minDelta>0){
            if (na>a && na-a<minDelta) na=a+minDelta;
            if (nb>b && nb-b<minDelta) nb=b+minDelta;
        }
        return new Result(na,nb);
    }

    private static int clampDelta(int old, int now, int max){
        if (now - old > max) return old + max;
        if (old - now > max) return old - max;
        return now;
    }

    private static double k(UltimatePixelmonRatingPlugin p,int r){
        int def=p.getConfig().getInt("rating.k-factor.default",32);
        int hi=p.getConfig().getInt("rating.k-factor.high-bracket-k",24);
        int lo=p.getConfig().getInt("rating.k-factor.low-bracket-k",40);
        int hiT=p.getConfig().getInt("rating.k-factor.high-threshold",1800);
        int loT=p.getConfig().getInt("rating.k-factor.low-threshold",1000);
        double k = (r<=loT)? lo : (r>=hiT? hi : def);
        int provN = p.getConfig().getInt("rating.provisional-games", 0);
        double provMul = p.getConfig().getDouble("rating.provisional-multiplier", 1.0);
        // If we had access to per-player games, we'd adjust here; kept simple for compatibility.
        return k * provMul;
    }
}
