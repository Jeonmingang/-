package com.minkang.ultimate.pixelrating;
public class EloCalculator {
    public enum Outcome { WIN, LOSS, DRAW }
    public static class Result { public final int newA,newB; public Result(int a,int b){ newA=a; newB=b; } }
    public static Result calculate(UltimatePixelmonRatingPlugin p, int a, int b, Outcome outA){
        double kA=k(p,a), kB=k(p,b); double eA=exp(a,b); double sA=(outA==Outcome.WIN)?1.0:(outA==Outcome.LOSS?0.0:p.getConfig().getDouble("rating.draw-score",0.5));
        int na=(int)Math.round(a + kA*(sA - eA)), nb=(int)Math.round(b + kB*((1.0-sA) - exp(b,a))); return new Result(na, nb);
    }
    private static double exp(int rA,int rB){ return 1.0/(1.0+Math.pow(10.0,(rB-rA)/400.0)); }
    private static double k(UltimatePixelmonRatingPlugin p,int r){
        int def=p.getConfig().getInt("rating.k-factor.default",32), hi=p.getConfig().getInt("rating.k-factor.high-bracket-k",24), lo=p.getConfig().getInt("rating.k-factor.low-bracket-k",40);
        int hiT=p.getConfig().getInt("rating.k-factor.high-threshold",1800), loT=p.getConfig().getInt("rating.k-factor.low-threshold",1000);
        if (r<=loT) return lo; if (r>=hiT) return hi; return def;
    }
}