package org.foi.nwtis.antpofuk.zrna;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Named;
import javax.json.Json;
import javax.json.JsonObjectBuilder;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import org.foi.nwtis.antpofuk.konfiguracije.Konfiguracija;
import org.foi.nwtis.antpofuk.konfiguracije.bp.BP_Konfiguracija;
import org.foi.nwtis.antpofuk.podaci.Aerodrom;
import org.foi.nwtis.antpofuk.rest.klijent.RESTAerodromi;
import org.foi.nwtis.antpofuk.soap.klijent.AerodromiClient;
import org.foi.nwtis.antpofuk.soap.klijent.MeteoPodaci;
import org.primefaces.context.RequestContext;

/**
 *
 * @author Antonija Pofuk
 */
@Named(value = "pogled_1")
@SessionScoped
public class Pogled_1 implements Serializable {

    ServletContext sc;
    BP_Konfiguracija bp_konf;
    String server;
    String baza;
    String korisnik;
    String lozinka;
    String driver;
    Konfiguracija konf;
    String jezik = "hr";
    List<Aerodrom> listaAerodromi;
    int limit = 0;
    List<Aerodrom> aerodromi;
    String icaoN;
    HttpSession session = (HttpSession) FacesContext.getCurrentInstance().getExternalContext().getSession(false);
    String prijavljen;
    String lozinkaSesija;
    String icao;
    String naziv;
    String odabraniAerodrom;
    MeteoPodaci meteoPodaci;

    public String getNaziv() {
        return naziv;
    }

    public void setNaziv(String naziv) {
        this.naziv = naziv;
    }

    public String getOdabraniAerodrom() {
        return odabraniAerodrom;
    }

    public void setOdabraniAerodrom(String odabraniAerodrom) {
        this.odabraniAerodrom = odabraniAerodrom;
    }

    public MeteoPodaci getMeteoPodaci() {
        return meteoPodaci;
    }

    public void setMeteoPodaci(MeteoPodaci meteoPodaci) {
        this.meteoPodaci = meteoPodaci;
    }

    public String getIcaoN() {
        return icaoN;
    }

    public void setIcaoN(String icaoN) {
        this.icaoN = icaoN;
    }

    public List<Aerodrom> getAerodromi() {
        return aerodromi;
    }

    public void setAerodromi(List<Aerodrom> aerodromi) {
        this.aerodromi = aerodromi;
    }

    public String getIcao() {
        return icao;
    }

    public void setIcao(String icao) {
        this.icao = icao;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public String getJezik() {
        return jezik;
    }

    public void setJezik(String jezik) {
        this.jezik = jezik;
    }

    public List<Aerodrom> getListaAerodromi() {
        return listaAerodromi;
    }

    public void setListaAerodromi(List<Aerodrom> listaAerodromi) {
        this.listaAerodromi = listaAerodromi;
    }

    public Pogled_1() {
         dajSesiju(); 
        dajAerodrome();
       
    }

    public void dajSesiju() {
        prijavljen = (String) session.getAttribute("korime");
        lozinkaSesija = (String) session.getAttribute("lozinka");
        System.out.println("Sesija za: " + prijavljen);
    }

    public void dajAerodrome() {
        listaAerodromi = new ArrayList<>();
        dajSesiju();
        Gson gson = new Gson();
        RESTAerodromi rest = new RESTAerodromi();
        String jsonStr = rest.getJson(prijavljen, lozinkaSesija);
        JsonObject jsonObject = new JsonParser().parse(jsonStr).getAsJsonObject();
        String json = (jsonObject.get("odgovor")).getAsString();
        JsonArray jsonA = new JsonParser().parse(json).getAsJsonArray();
        for (JsonElement jsonElement : jsonA) {
            Aerodrom a = gson.fromJson(jsonElement, Aerodrom.class);
            listaAerodromi.add(a);
        }
    }

    public String dodajAerodrom() {
        dajSesiju();
        Gson gson = new Gson();
        RESTAerodromi rest = new RESTAerodromi();
        if (!icaoN.isEmpty()) {
            System.out.println(icaoN);
            System.out.println("upisuje se" + icaoN);
            JsonObjectBuilder json = Json.createObjectBuilder();
            json.add("icao", icaoN);
            String odgovor = rest.postJson(json.build().toString(), prijavljen, lozinkaSesija);
            System.out.println("Dodan:" + odgovor);
            this.icaoN = "";
            prikaziPoruku("Dodali ste aerodrom" + icaoN);
            dajAerodrome();
            return "dodan";
        } else {
            prikaziPoruku("Molimo unesite ICAO aerodroma!");
            return null;
        }

    }

    public void dajVazeceMeteo() {
        System.out.println("Aerodrom je " + odabraniAerodrom);
        meteoPodaci = AerodromiClient.dajVazeceMeteoPodatke("LDZA", prijavljen, lozinkaSesija);
        System.out.println(meteoPodaci.getTemperatureMax());
    }

    public void prikaziPoruku(String String) {
        RequestContext.getCurrentInstance().showMessageInDialog(new FacesMessage(FacesMessage.SEVERITY_INFO, "Upozorenje", String));
    }
}
