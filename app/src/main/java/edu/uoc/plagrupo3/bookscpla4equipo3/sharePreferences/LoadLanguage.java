package edu.uoc.plagrupo3.bookscpla4equipo3.sharePreferences;

import android.content.Context;
import android.content.res.Configuration;

import java.util.Locale;

public class LoadLanguage {
    private Context mcontext;
    private ChangeLanguage idioma;

  public LoadLanguage(Context context){
     mcontext=context;}



    public void Change(){
        idioma = new ChangeLanguage(mcontext);

        Locale locale = new Locale(idioma.getLangCode());
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.locale = locale;
        mcontext.getResources().updateConfiguration(config,mcontext.getResources().getDisplayMetrics());

    }


}
