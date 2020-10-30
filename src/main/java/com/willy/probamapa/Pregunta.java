package com.willy.probamapa;
import java.io.Serializable;
public class Pregunta implements Serializable {
    String question,type, code,nom_lloc,url_imatge_pre,url_imatge_post;
    int id;
    Float lat,longi;
    boolean superat;

    public Pregunta(int id, String question, String type, String code, Float lat, Float longi, String nom_lloc, String url_imatge_pre, String url_imatge_post, boolean superat) {
        this.id = id;
        this.question = question;
        this.type = type;
        this.code = code;
        this.lat = lat;
        this.longi = longi;
        this.nom_lloc = nom_lloc;
        this.url_imatge_pre = url_imatge_pre;
        this.url_imatge_post = url_imatge_post;
        this.superat = superat;

    }
    public Pregunta(int id, String question, String type, String code, Float lat, Float longi, String url_imatge_pre, String url_imatge_post,String nom_lloc) {
        this.id = id;
        this.question = question;
        this.type = type;
        this.code = code;
        this.lat = lat;
        this.longi = longi;
        this.nom_lloc = nom_lloc;
        this.url_imatge_pre = url_imatge_pre;
        this.url_imatge_post = url_imatge_post;
        this.superat = false;
    }
}

