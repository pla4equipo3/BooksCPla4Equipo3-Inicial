package edu.uoc.plagrupo3.bookscpla4equipo3.modeloDatos;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Libro extends RealmObject {
    @PrimaryKey
    private int id;
    private String titulo;
    private String autor;
    private String fechapub;
    private String descripcion;
    private String urlimage;
    private String latitud;
    private String longitud;
    private String disponible;
    private Boolean favorito;
    private String keylibro;
    private String usuarioactivo;

    public Libro(){}
    //Constructor de la clase con todos los parámetros.

    public Libro(int identificador, String titulo, String autor, String fechapub,
                 String descripcion, String URL,String latitud, String longitud, String disponible,Boolean favorito,String keylibro,String usuarioactivo) {
        this.id = identificador;
        this.titulo = titulo;
        this.autor = autor;
        this.fechapub= fechapub;
        this.descripcion = descripcion;
        this.urlimage = URL;
        this.latitud= latitud;
        this.longitud = longitud;
        this.disponible = disponible;
        this.favorito = favorito;
        this.keylibro = keylibro;
        this.usuarioactivo = usuarioactivo;
    }

    //Get y sets de las propiedades.
    public int getId() {
        return this.id;
    }
    public void setId(int identificador) {
        this.id = identificador;
    }
    public String getTitulo() {
        return this.titulo;
    }
    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }
    public String getAutor() {
        return this.autor;
    }
    public void setAutor(String autor) {
        this.autor = autor;
    }
    public String getFechapub() {
        return this.fechapub;
    }
    public void setpublicationdate(String dataPublicacion) {
        this.fechapub = dataPublicacion;
    }
    public String getDescripcion() {
        return this.descripcion;
    }
    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
    public String getUrlimage() {
        return this.urlimage;
    }
    public void setUrlimage(String URL) {
        this.urlimage = URL;
    }
    public String getLongitud() {
        return this.longitud;
    }
    public void setLongitud(String longitud) {
        this.longitud = longitud;
    }
    public String getLatitud() {
        return this.latitud;
    }
    public void setLatitud(String latitud) {
        this.latitud = latitud;
    }
    public void setDisponible(String disponible){this.disponible= disponible; }
    public String getDisponible(){return this.disponible;}
    public void setFavorito(Boolean favorito){this.favorito=favorito;}
    public Boolean getFavorito(){return this.favorito;}
    public String getKeylibro() { return keylibro; }
    public void setKeylibro(String keylibro) {this.keylibro = keylibro; }
    public String getUsuarioactivo() { return usuarioactivo;}
    public void setUsuarioactivo(String usuarioactivo) { this.usuarioactivo = usuarioactivo;}

    /*
   public boolean estaDisponible() {
        return this.disponible;
    }
    public void setDisponible(boolean disp) {
       this.disponible = disp;
    }
    public String getTextoDisp() {
        String mensaje = "No disponible";
        if (this.disponible) mensaje = "Disponible";
        return mensaje;
    }
*/
}
