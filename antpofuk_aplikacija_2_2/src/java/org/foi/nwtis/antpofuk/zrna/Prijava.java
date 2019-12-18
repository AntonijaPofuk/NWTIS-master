package org.foi.nwtis.antpofuk.zrna;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.json.Json;
import javax.json.JsonObjectBuilder;
import javax.json.JsonValue;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import org.foi.nwtis.antpofuk.konfiguracije.Konfiguracija;
import org.foi.nwtis.antpofuk.konfiguracije.bp.BP_Konfiguracija;
import org.foi.nwtis.antpofuk.podaci.Korisnik;
import org.foi.nwtis.antpofuk.rest.klijent.RESTKorisnik;
import org.primefaces.context.RequestContext;

/**
 * Klasa za upravljanje prikazom prijave, registracije, pregleda korisnika
 *
 * @author Antonija Pofuk
 */
@Named(value = "prijava")
@SessionScoped
public class Prijava implements Serializable {

    private String korime;
    private String loz;
    private boolean error_login = false;

    ServletContext sc;
    BP_Konfiguracija bp_konf;
    String server;
    String baza;
    String korisnik;
    String lozinka;
    String driver;
    Konfiguracija konf;
    private String jezik = "hr";
    private List<Korisnik> lista = new ArrayList<>();
    private int limit = 0;
    private String ime;
    private String prezime;
    private List<Korisnik> korisnici;
    private String kor_imeR;
    private String prezimeR;
    private String lozR;
    private String imeR;
    private String ponovljenaLozinkaR;
    boolean lozinkeNePodudaraju = true;
    HttpSession session = (HttpSession) FacesContext.getCurrentInstance().getExternalContext().getSession(false);
    String prijavljen;
    String lozinkaSesija;
    private String imeA;

    public String getImeA() {
        return imeA;
    }

    public void setImeA(String imeA) {
        this.imeA = imeA;
    }

    public String getKorimeA() {
        return korimeA;
    }

    public void setKorimeA(String korimeA) {
        this.korimeA = korimeA;
    }

    public String getPrezimeA() {
        return prezimeA;
    }

    public void setPrezimeA(String prezimeA) {
        this.prezimeA = prezimeA;
    }

    public String getLozA() {
        return lozA;
    }

    public void setLozA(String lozA) {
        this.lozA = lozA;
    }
    private String korimeA;
    private String prezimeA;
    private String lozA;

    public String getPrijavljen() {
        return prijavljen;
    }

    public void setPrijavljen(String prijavljen) {
        this.prijavljen = prijavljen;
    }

    public String getKor_imeR() {
        return kor_imeR;
    }

    public void setKor_imeR(String kor_imeR) {
        this.kor_imeR = kor_imeR;
    }

    public String getPrezimeR() {
        return prezimeR;
    }

    public void setPrezimeR(String prezimeR) {
        this.prezimeR = prezimeR;
    }

    public String getLozR() {
        return lozR;
    }

    public void setLozR(String lozR) {
        this.lozR = lozR;
    }

    public String getImeR() {
        return imeR;
    }

    public void setImeR(String imeR) {
        this.imeR = imeR;
    }

    public String getPonovljenaLozinkaR() {
        return ponovljenaLozinkaR;
    }

    public void setPonovljenaLozinkaR(String ponovljenaLozinkaR) {
        this.ponovljenaLozinkaR = ponovljenaLozinkaR;
    }

    public Prijava() {
        dajPodatke();
        dajSesiju();
    }

    public void dajSesiju() {
        prijavljen = (String) session.getAttribute("korime");
        lozinkaSesija = (String) session.getAttribute("lozinka");
        System.out.println("Sesija za: " + prijavljen);
    }

    public String getJezik() {
        return jezik;
    }

    public void setJezik(String jezik) {
        this.jezik = jezik;
    }

    public Object odaberiJezik() {
        return "jezik";
    }

    public String login() {
        dajPodatke();
        
        if (this.korime != null && this.loz != null && !this.loz.isEmpty() && !this.korime.isEmpty()) {
            System.out.println(korime + loz);
            error_login = false;
            RESTKorisnik autentikacija = new RESTKorisnik();
            String odgovor = autentikacija.getJsonId("0", korime, loz);
            System.out.println(odgovor);
            if (odgovor.contains("OK")) {
                System.out.println("Prijavljen korisnik:" + odgovor);
                staviUSesiju(korime, loz);
                this.korime = "";
                this.loz = "";
                dajSesiju();
                prikaziPoruku("Prijavili ste se pod korisničkim imenom: " + prijavljen);
                dajPodatke();              
            } else {
                error_login = true;
                this.korime = "";
                this.loz = "";
            }
        } else {
            error_login = true;
            this.korime = "";
            this.loz = "";
        }
        if (error_login == true) {
            prikaziPoruku("Prijava nije uspjela, provjerite podatke!");
            return "error";
        } else {
            return "prijava";
        }
    }

    public void staviUSesiju(String user, String pass) {
        session.setAttribute("korime", user);
        session.setAttribute("lozinka", pass);
    }

    public void odjaviSe() {
        session.removeAttribute("korime");
        session.removeAttribute("lozinka");
        this.prijavljen = null;
        prikaziPoruku("Odjavili ste se!");
    }

    public void dajPodatke() {
        dajSesiju();
        Gson gson = new Gson();
        RESTKorisnik klijentKor = new RESTKorisnik();
        String jsonStr = klijentKor.getJson(prijavljen, lozinkaSesija);
        JsonObject jsonObject = new JsonParser().parse(jsonStr).getAsJsonObject();
        String json = (jsonObject.get("odgovor")).getAsString();
        JsonArray jsonA = new JsonParser().parse(json).getAsJsonArray();
        for (JsonElement jsonElement : jsonA) {
            Korisnik k = gson.fromJson(jsonElement, Korisnik.class);
            lista.add(k);
            String imeKori = k.getIme();
            System.out.println(imeKori);
        }
    }

    /**
     * Metoda koja služi za registraciju korisnika
     *
     * @return "error" ako registracija nije uspjela odnosno "registracija" ako
     * je uspjela
     *
     */
    public String registriraj() {
        if (!kor_imeR.isEmpty() && !lozR.isEmpty() && !prezimeR.isEmpty() && !imeR.isEmpty()
                && !ponovljenaLozinkaR.isEmpty()) {
            if (lozR.equals(ponovljenaLozinkaR)) {
                lozinkeNePodudaraju = false;
                if (lozinkeNePodudaraju) {
                    prikaziPoruku("Lozinke se ne podudaraju!");
                }
                System.out.println("upisuje se" + kor_imeR + lozR + imeR + prezimeR);
                RESTKorisnik rest = new RESTKorisnik();
                JsonObjectBuilder json = Json.createObjectBuilder();
                json.add("korisnickoIme", kor_imeR);
                json.add("lozinka", lozR);
                json.add("prezime", prezimeR);
                json.add("ime", imeR);
                String odgovor = rest.postJsonKorisnik(new Korisnik(kor_imeR, lozR, prezimeR, imeR));
                System.out.println(odgovor);
                this.kor_imeR = "";
                this.imeR = "";
                this.prezimeR = "";
                this.lozR = "";
                prikaziPoruku("Registrirali ste se uspješno!");
                return "registriran";
            }
        }
        prikaziPoruku("Niste se uspjeli registrirati. Provjerite podatke!");
        return null;
    }

    public void dajKorisnika() {
        RESTKorisnik rest = new RESTKorisnik();
        JsonObjectBuilder json = Json.createObjectBuilder();
        json.add("korisnickoIme", korimeA);
        json.add("lozinka", lozA);
        json.add("prezime", prezimeA);
        json.add("ime", imeA);
        String odgovor = rest.getJsonId(prijavljen, prijavljen, lozinkaSesija);
        System.out.println(odgovor);
        this.korimeA = "";
        this.imeA = "";
        this.prezimeA = "";
        this.lozA = "";
       if (odgovor.contains("OK")) {
            prikaziPoruku("Dohvaceni su podaci!");
        } else {
            prikaziPoruku("Podaci nisu dohvaceni!");
        }
    }

    public void azurirajKorisnika() {
        System.out.println("upisuje se" + korimeA + lozA + imeA + prezimeA);
        RESTKorisnik rest = new RESTKorisnik();
        JsonObjectBuilder json = Json.createObjectBuilder();
        json.add("korisnickoIme", korimeA);
        json.add("lozinka", lozA);
        json.add("prezime", prezimeA);
        json.add("ime", imeA);
        String odgovor = rest.putJson(new Korisnik(korimeA, lozA, prezimeA, imeA), korimeA);
        System.out.println(odgovor);
        this.kor_imeR = "";
        this.imeR = "";
        this.prezimeR = "";
        this.lozR = "";
        if (odgovor.contains("OK")) {
            prikaziPoruku("Azurirali ste se uspješno!");
        } else {
            prikaziPoruku("Azuriranje nije uspjelo! Provjerite podatke");
        }
    }

    public String getLoz() {
        return loz;
    }

    public void setLoz(String loz) {
        this.loz = loz;
    }

    public boolean isError_login() {
        return error_login;
    }

    public void setError_login(boolean error_login) {
        this.error_login = error_login;
    }

    public String pregledPoruka() {
        return "pregledPoruka";
    }

    public String pregledDnevnika() {
        return "pregledDnevnika";
    }

    public String promjenaJezika() {
        return "promjenaJezika";
    }

    public String registracija() {
        return "registracija";
    }

    public String getKorime() {
        return korime;
    }

    public void setKorime(String korime) {
        this.korime = korime;
    }

    public String getIme() {
        return ime;
    }

    public void setIme(String ime) {
        this.ime = ime;
    }

    public String getPrezime() {
        return prezime;
    }

    public void setPrezime(String prezime) {
        this.prezime = prezime;
    }

    public List<Korisnik> getLista() {
        return lista;
    }

    public void setLista(List<Korisnik> lista) {
        this.lista = lista;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public void prikaziPoruku(String String) {
        RequestContext.getCurrentInstance().showMessageInDialog(new FacesMessage(FacesMessage.SEVERITY_INFO, "Upozorenje", String));
    }

}
