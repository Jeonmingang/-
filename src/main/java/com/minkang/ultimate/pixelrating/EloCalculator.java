
package com.minkang.ultimate.pixelrating;

public class EloCalculator {
    public enum Outcome { WIN, LOSS, DRAW }
    public static class Result { public final int newA; public final int newB; public Result(int a,int b){newA=a;newB=b;} }
    public static Result calculate(UltimatePixelmonRatingPlugin p, int a, int b, Outcome outA){
        double kA=k(p,a), kB=k(p,b);
        double eA=exp(a,b);
        double sA;
        if (outA==Outcome.WIN) sA=1.0;
        else if (outA==Outcome.LOSS) sA=0.0;
        else sA=p.getConfig().getDouble("rating.draw-score",0.5);
        int na=(int)Math.round(a + kA*(sA - eA));
        int nb=(int)Math.round(b + kB*((1.0 - sA) - exp(b,a)));
        return new Result(na,nb);
    }
    private static double exp(int A,int B){ return 1.0/(1.0+Math.pow(10.0,(B-A)/400.0)); }
    private static double k(UltimatePixelmonRatingPlugin p,int r){
        int def=p.getConfig().getInt("rating.k-factor.default",32);
        int hi=p.getConfig().getInt("rating.k-factor.high-bracket-k",24);
        int lo=p.getConfig().getInt("rating.k-factor.low-bracket-k",40);
        int hiT=p.getConfig().getInt("rating.k-factor.high-threshold",1800);
        int loT=p.getConfig().getInt("rating.k-factor.low-threshold",1000);
        if (r<=loT) return lo;
        if (r>=hiT) return hi;
        return def;
    }
}
