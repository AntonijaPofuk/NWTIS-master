package org.foi.nwtis.antpofuk.web.podaci;

/**
 * Klasa za rad s korisničkim podacima
 * @author Antonija Pofuk
 */
public class Korisnik {
    private int id;
    private String korisnickoIme;
    private String lozinka;
    private String prezime;
    private String ime;

    public Korisnik() {
    }

    public Korisnik(int ID, String kor_ime, String pass, String prezime, String ime) {
        this.id = ID;
        this.korisnickoIme = kor_ime;
        this.lozinka = pass;
        this.prezime = prezime;
        this.ime = ime;
    }
        public Korisnik(String kor_ime, String pass, String prezime, String ime) {
      
        this.korisnickoIme = kor_ime;
        this.lozinka = pass;
        this.prezime = prezime;
        this.ime = ime;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getKorisnickoIme() {
        return korisnickoIme;
    }

    public void setKorisnickoIme(String korisnickoIme) {
        this.korisnickoIme = korisnickoIme;
    }

    public String getLozinka() {
        return lozinka;
    }

    public void setLozinka(String lozinka) {
        this.lozinka = lozinka;
    }

    public String getPrezime() {
        return prezime;
    }

    public void setPrezime(String prezime) {
        this.prezime = prezime;
    }

    public String getIme() {
        return ime;
    }

    public void setIme(String ime) {
        this.ime = ime;
    }


    
}
