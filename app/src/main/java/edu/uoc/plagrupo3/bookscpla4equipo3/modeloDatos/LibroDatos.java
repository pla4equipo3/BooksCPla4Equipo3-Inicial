package edu.uoc.plagrupo3.bookscpla4equipo3.modeloDatos;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;

import com.google.firebase.database.DataSnapshot;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import io.realm.Realm;
import io.realm.RealmResults;

public class LibroDatos {

    public static List<Libro> listalibros=new ArrayList<>();

    //Definimos la variable de conexión
    public static Realm conexion;

    /*public static RealmConfiguration config = new RealmConfiguration.Builder().name("milibros.realm").build();
        Por si necesitamos cambiar la configuración de la base de datos
    */
    //Función para devolver la lista
    public static List<Libro> getBooks(){

        return   conexion.copyFromRealm(LibroDatos.conexion.where(Libro.class).findAll());
    }

    //función que devuelve un nuevo id para la base datos, ya que realm no tiene autoincremento

    public static int calculateIndex()
    {
        conexion= Realm.getDefaultInstance();
        Number currentId = conexion.where(Libro.class).max("id");
        int nextId;
        if (currentId==null){
            nextId=0;
        }
        else {
            nextId= currentId.intValue()+1;
        }
        return nextId;
    }


    //Comprueba si existe un libro por titulo

    public static  boolean exists(Libro libro){
        List<Libro> l =conexion.where(Libro.class).equalTo("titulo", libro.getTitulo()).findAll();
        // RealmResults<Libro> l = conexion.where(Libro.class).equalTo("titulo", libro.getTitulo()).findAll();
        //Devuelve resultado en función de si encuentra el titulo o no
        if (l.size() > 0)
            return true;
        else
            return false;
    }

    //Comprueba si existe un libro por id

    public static  boolean existsById(Libro libro){
        List<Libro> l = conexion.where(Libro.class).equalTo("id", libro.getId()).findAll();

        //
        // RealmResults<Libro> l = conexion.where(Libro.class).equalTo("id", libro.getId()).findAll();
        //Devuelve resultado en función de si encuentra el titulo o no
        if (l.size() > 0)
            return true;
        else
            return false;
    }
    public static void updateId(){
        conexion = Realm.getDefaultInstance();
        conexion.beginTransaction();
        RealmResults<Libro> listaFavoritos = conexion.where(Libro.class).equalTo("favorito",true).findAll().sort("id");
        for (int i=0;i<listaFavoritos.size();i++) {
            listaFavoritos.setInt("id",i);
        }
        conexion.commitTransaction();
    }

    //eliminar registros base datos
    public static void deleteDatabase(){
        conexion = Realm.getDefaultInstance();
        conexion.beginTransaction();
        RealmResults<Libro> lista =conexion.where(Libro.class).findAll();
        lista.deleteAllFromRealm();
        conexion.commitTransaction();
    }
    //Eliminamos el libro
    public static  void deleteBook(int  bookpos){
        RealmResults<Libro> l;
        if (bookpos < LibroDatos.listalibros.size()) {
            conexion.beginTransaction();

            l = conexion.where(Libro.class).equalTo("id", LibroDatos.listalibros.get(bookpos).getId()).findAll();
            l.deleteAllFromRealm();
            conexion.commitTransaction();
        }
    }

    //Listar Favoritos

    public static List<Libro> getAllFavourite(){
        conexion = Realm.getDefaultInstance();
        List<Libro> l = conexion.where(Libro.class).equalTo("favorito",true).findAll();
        //RealmResults<Libro> listaFavoritos = conexion.where(Libro.class).equalTo("favorito",true).findAll();

        return l;
    }



    //Añadir Favorito
    public static void addFavourite( int id){
        conexion= Realm.getDefaultInstance();
        conexion.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                Libro libro = realm.where(Libro.class).equalTo("id",id).findFirst();
                libro.setFavorito(true);
                realm.insertOrUpdate(libro);

            }
        });
        /* otra manera de hacerlo
        conexion.beginTransaction();
        Libro libro = conexion.where(Libro.class).equalTo("id",id).findFirst();
        libro.setFavorito(true);
        conexion.insertOrUpdate(libro);
        conexion.commitTransaction();
        */
    }



    //Quitar de favoritos

    public static void deleteFavourite( int id){
        conexion= Realm.getDefaultInstance();
        conexion.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                Libro libro = realm.where(Libro.class).equalTo("id",id).findFirst();
                libro.setFavorito(false);
                realm.insertOrUpdate(libro);

            }
        });
        /*
        conexion= Realm.getDefaultInstance();
        conexion.beginTransaction();
        Libro libro = conexion.where(Libro.class).equalTo("id",id).findFirst();
        libro.setFavorito(false);
        conexion.insertOrUpdate(libro);
        conexion.commitTransaction();
        */
    }

    //Clase asincrona para cargar la imagen, recibe el imageview en que la cargará a partir de la URL
    public static class cargaImagendeURL extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;
        public cargaImagendeURL(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap bmp = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                bmp = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return bmp;
        }
        protected void onPostExecute(Bitmap result) {
            bmImage.setImageBitmap(result);
        }
    }
}