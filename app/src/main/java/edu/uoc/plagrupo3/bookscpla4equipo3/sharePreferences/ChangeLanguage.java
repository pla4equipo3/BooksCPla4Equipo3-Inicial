package edu.uoc.plagrupo3.bookscpla4equipo3.sharePreferences;

import android.content.Context;
import android.content.SharedPreferences;

import static android.content.Context.MODE_PRIVATE;

public class ChangeLanguage {

    private static final String FILE_NAME = "file_lang"; // nombre archivo preference
    private static final String KEY_LANG = "key_lang"; // preference key
    private Context mContext;

    public ChangeLanguage(Context context){
        mContext=context;
    }

    private SharedPreferences getSettings()
    {
        return mContext.getSharedPreferences(FILE_NAME, MODE_PRIVATE);
    }

    public void saveLanguage(String lang) {
        // guardar el leguaje
        SharedPreferences.Editor editor = getSettings().edit();
        editor.putString(KEY_LANG, lang);
        editor.apply();
        }

    public String getLangCode() {
         String langCode = getSettings().getString(KEY_LANG, "es");
        // devuelve el idioma por defecto en español
         return langCode;
    }
}
