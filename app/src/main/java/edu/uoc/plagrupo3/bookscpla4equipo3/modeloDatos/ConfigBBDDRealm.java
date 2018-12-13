package edu.uoc.plagrupo3.bookscpla4equipo3.modeloDatos;

import android.app.Application;

import io.realm.Realm;
import io.realm.RealmConfiguration;

public class ConfigBBDDRealm extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        Realm.init(this);
        RealmConfiguration realmConfiguration = new RealmConfiguration.Builder()
                .name(Realm.DEFAULT_REALM_NAME)
                .schemaVersion(0)
                .deleteRealmIfMigrationNeeded()
                .build();
        Realm.setDefaultConfiguration(realmConfiguration);
    }
}
