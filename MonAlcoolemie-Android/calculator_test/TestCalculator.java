import java.util.*; import nc.xavier.monalcoolemie.BacCalculator;
public class TestCalculator { public static void main(String[] a){
 var one=List.of(new BacCalculator.Drink(6,40,1200,1201));
 var r=BacCalculator.calculate(one,82,58,true,0.13);
 if(r.peak()<0.69||r.peak()>0.72) throw new AssertionError("pic "+r.peak());
 if(r.zeroMinute()<=1201) throw new AssertionError("zero");
 boolean rejected=false;try{BacCalculator.calculate(one,10,58,true,0.13);}catch(IllegalArgumentException e){rejected=true;}if(!rejected)throw new AssertionError("weight guard");
 var cross=List.of(new BacCalculator.Drink(10,12,1430,30)); BacCalculator.calculate(cross,82,58,true,0.13);
 System.out.println("OK: contrôles calculateur validés");
}}
