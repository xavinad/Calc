package nc.xavier.monalcoolemie;

import java.util.*;

public final class BacCalculator {
    public record Drink(double cl, double abv, int startMin, int endMin) {}
    public record Result(double peak, int zeroMinute, boolean beyond48h) {}
    private BacCalculator() {}

    public static Result calculate(List<Drink> drinks, double weight, int age, boolean male, double elimination) {
        if (drinks == null || drinks.isEmpty()) throw new IllegalArgumentException("Aucun verre");
        if (weight < 30 || weight > 300 || age < 18 || age > 120 || elimination < 0.08 || elimination > 0.20)
            throw new IllegalArgumentException("Profil hors limites");
        int base = nightStart(drinks.stream().mapToInt(Drink::startMin).toArray());
        double r = Math.max(0.50, (male ? 0.70 : 0.60) - Math.max(0, age - 20) * 0.001);
        class P { int s,e,d; double bac; P(int s,int e,int d,double bac){this.s=s;this.e=e;this.d=d;this.bac=bac;} }
        List<P> parsed = new ArrayList<>();
        for (Drink x : drinks) {
            if (x.cl <= 0 || x.cl > 200 || x.abv <= 0 || x.abv > 100) throw new IllegalArgumentException("Verre hors limites");
            int s=x.startMin, e=x.endMin;
            if(s<base)s+=1440; if(e<s)e+=1440;
            int d=Math.max(e-s,1);
            if(d>24*60) throw new IllegalArgumentException("Durée supérieure à 24 h");
            double grams=x.cl*10.0*(x.abv/100.0)*0.8;
            parsed.add(new P(s,e,d,grams/(weight*r)));
        }
        parsed.sort(Comparator.comparingInt(p->p.s));
        int first=parsed.get(0).s; double bac=0, peak=0;
        for(int t=first;t<=first+48*60;t++){
            for(P p:parsed){ if(t>p.s && t<=p.e) bac+=p.bac/p.d; else if(t==p.s && p.d==1) bac+=p.bac; }
            peak=Math.max(peak,bac); if(bac>0) bac=Math.max(0,bac-elimination/60.0);
            boolean done=true; for(P p:parsed) if(t<p.e){done=false;break;}
            if(done && bac<=1e-9) return new Result(peak,t,false);
        }
        return new Result(peak,-1,true);
    }

    static int nightStart(int[] times){
        if(times.length==1)return times[0]; Arrays.sort(times); int max=-1,start=times[0];
        for(int i=0;i<times.length;i++){int next=times[(i+1)%times.length]; int gap=(next-times[i]+1440)%1440; if(gap>max){max=gap;start=next;}}
        return start;
    }
}
