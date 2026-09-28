package nc.xavier.monalcoolemie;

import android.app.*; import android.os.*; import android.graphics.Color; import android.graphics.Typeface;
import android.content.*; import android.text.InputType; import android.view.*; import android.widget.*;
import java.util.*;

public class MainActivity extends Activity {
    final List<BacCalculator.Drink> drinks=new ArrayList<>(); LinearLayout list; TextView result; EditText weight,age,elim,vol,abv,start,end; Spinner sex;
    int blue=Color.rgb(168,199,250), bg=Color.rgb(18,18,18), surface=Color.rgb(30,30,30);
    @Override public void onCreate(Bundle b){super.onCreate(b); showMandatoryWarning();}
    void showMandatoryWarning(){
        new AlertDialog.Builder(this).setTitle("Avertissement indispensable")
          .setMessage("Cette application fournit uniquement une estimation théorique. Elle ne mesure pas l’alcoolémie et ne donne jamais l’autorisation de conduire. En cas de doute, ne conduisez pas. Utilisez un éthylotest homologué et prévoyez un autre transport.")
          .setCancelable(false).setPositiveButton("J’ai compris",(d,w)->build()).show();
    }
    TextView text(String s,int sp){TextView v=new TextView(this);v.setText(s);v.setTextColor(Color.LTGRAY);v.setTextSize(sp);v.setPadding(4,6,4,6);return v;}
    EditText field(String value,int type){EditText e=new EditText(this);e.setText(value);e.setTextColor(Color.WHITE);e.setHintTextColor(Color.GRAY);e.setInputType(type);e.setBackgroundColor(Color.rgb(42,42,42));e.setPadding(18,12,18,12);return e;}
    LinearLayout card(){LinearLayout c=new LinearLayout(this);c.setOrientation(LinearLayout.VERTICAL);c.setPadding(24,20,24,20);c.setBackgroundColor(surface);LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2);p.setMargins(0,0,0,20);c.setLayoutParams(p);return c;}
    void addField(LinearLayout c,String label,View v){c.addView(text(label,13));c.addView(v,new LinearLayout.LayoutParams(-1,56));}
    Button button(String s){Button b=new Button(this);b.setText(s);b.setTextColor(Color.rgb(0,51,85));b.setBackgroundColor(blue);return b;}
    void build(){
        ScrollView sc=new ScrollView(this);sc.setFillViewport(true); LinearLayout root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setPadding(24,24,24,220);root.setBackgroundColor(bg);sc.addView(root);
        TextView title=text("Mon Alcoolémie",26);title.setTextColor(blue);title.setTypeface(null,Typeface.BOLD);title.setGravity(Gravity.CENTER);root.addView(title);
        TextView warn=text("⚠ ESTIMATION UNIQUEMENT • NE PERMET PAS DE DÉCIDER DE CONDUIRE",14);warn.setTextColor(Color.rgb(255,200,120));warn.setGravity(Gravity.CENTER);warn.setPadding(8,18,8,18);root.addView(warn);
        LinearLayout profile=card(); profile.addView(text("Profil de calcul",19));
        weight=field("82",2|8192); age=field("58",2); elim=field("0.13",2|8192); sex=new Spinner(this);sex.setAdapter(new ArrayAdapter<>(this,android.R.layout.simple_spinner_dropdown_item,new String[]{"Homme","Femme"}));
        addField(profile,"Poids (30 à 300 kg)",weight);addField(profile,"Âge (18 à 120 ans)",age);addField(profile,"Sexe",sex);addField(profile,"Élimination estimée (0,08 à 0,20 g/L/h)",elim);root.addView(profile);
        LinearLayout form=card();form.addView(text("Nouveau verre",19));vol=field("6",2|8192);abv=field("40",2|8192);String now=String.format(Locale.FRANCE,"%02d:%02d",Calendar.getInstance().get(11),Calendar.getInstance().get(12));start=field(now,1);end=field(now,1);
        addField(form,"Volume (cl)",vol);addField(form,"Taux (%)",abv);addField(form,"Début (HH:mm)",start);addField(form,"Fin (HH:mm)",end);Button add=button("Ajouter à la liste");add.setOnClickListener(v->addDrink());form.addView(add);root.addView(form);
        list=new LinearLayout(this);list.setOrientation(LinearLayout.VERTICAL);root.addView(list);
        result=text("",18);result.setTextColor(Color.rgb(0,51,85));result.setBackgroundColor(blue);result.setGravity(Gravity.CENTER);result.setPadding(18,20,18,20);result.setVisibility(View.GONE);root.addView(result,new LinearLayout.LayoutParams(-1,-2));setContentView(sc);
    }
    double num(EditText e){return Double.parseDouble(e.getText().toString().trim().replace(',','.'));}
    int time(EditText e){String[] p=e.getText().toString().trim().split(":");if(p.length!=2)throw new Exception();int h=Integer.parseInt(p[0]),m=Integer.parseInt(p[1]);if(h<0||h>23||m<0||m>59)throw new Exception();return h*60+m;}
    void addDrink(){try{double v=num(vol),a=num(abv);if(v<=0||v>200||a<=0||a>100)throw new Exception();drinks.add(new BacCalculator.Drink(v,a,time(start),time(end)));render();}catch(Exception x){toast("Valeurs invalides. Vérifiez volume, taux et heures.");}}
    void render(){list.removeAllViews();for(int i=0;i<drinks.size();i++){final int n=i;BacCalculator.Drink d=drinks.get(i);LinearLayout c=card();c.addView(text(String.format(Locale.FRANCE,"%.1f cl à %.1f %%",d.cl(),d.abv()),18));c.addView(text(String.format(Locale.FRANCE,"De %02d:%02d à %02d:%02d",d.startMin()/60,d.startMin()%60,d.endMin()/60,d.endMin()%60),14));Button rm=button("Supprimer");rm.setOnClickListener(v->{drinks.remove(n);render();});c.addView(rm);list.addView(c);}calculate();}
    void calculate(){if(drinks.isEmpty()){result.setVisibility(View.GONE);return;}try{BacCalculator.Result r=BacCalculator.calculate(drinks,num(weight),(int)num(age),sex.getSelectedItemPosition()==0,num(elim));String z=r.beyond48h()?"au-delà de 48 h":String.format(Locale.FRANCE,"%02dh%02d",(r.zeroMinute()/60)%24,r.zeroMinute()%60);result.setText(String.format(Locale.FRANCE,"Pic théorique : %.2f g/L\nRetour mathématique à 0 : %s\n\n⚠ Ce résultat n’autorise jamais à conduire. Attendez davantage et contrôlez avec un éthylotest homologué.",r.peak(),z));result.setVisibility(View.VISIBLE);}catch(Exception x){result.setVisibility(View.GONE);toast(x.getMessage()==null?"Profil invalide":x.getMessage());}}
    void toast(String s){Toast.makeText(this,s,Toast.LENGTH_LONG).show();}
}
