/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.foi.nwtis.antpofuk.podaci;

import java.util.Date;

/**
 *
 * @author Antonija Pofuk
 */
public class Dnevnik {
    private int id;
    private int idKorime;    
    private String sadrzaj;
    private Date stored;
    private String vrsta;
    private String ip_adresa;
    private String url;
    

    public Dnevnik() {
    }

    public Dnevnik(int id, int idKorime, Date stored, String url, String vrsta, String ip_adresa) {
        this.id = id;
        this.idKorime = idKorime;
        this.stored = stored;
        this.url = url;
        this.vrsta = vrsta;
        this.ip_adresa = ip_adresa;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getIdKorime() {
        return idKorime;
    }

    public void setIdKorime(int idKorime) {
        this.idKorime = idKorime;
    }

    public String getSadrzaj() {
        return sadrzaj;
    }

    public void setSadrzaj(String sadrzaj) {
        this.sadrzaj = sadrzaj;
    }

    public Date getStored() {
        return stored;
    }

    public void setStored(Date stored) {
        this.stored = stored;
    }

    public String getVrsta() {
        return vrsta;
    }

    public void setVrsta(String vrsta) {
        this.vrsta = vrsta;
    }

    public String getIp_adresa() {
        return ip_adresa;
    }

    public void setIp_adresa(String ip_adresa) {
        this.ip_adresa = ip_adresa;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

      
}
