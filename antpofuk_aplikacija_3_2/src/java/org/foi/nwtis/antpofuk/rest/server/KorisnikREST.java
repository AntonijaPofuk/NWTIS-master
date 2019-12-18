package org.foi.nwtis.antpofuk.rest.server;

import com.google.gson.Gson;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.json.Json;
import javax.json.JsonObject;
import javax.servlet.ServletContext;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;
import org.foi.nwtis.antpofuk.konfiguracije.Konfiguracija;
import org.foi.nwtis.antpofuk.konfiguracije.bp.BP_Konfiguracija;
import org.foi.nwtis.antpofuk.ws.klijent.KorisnikClient;
import org.foi.nwtis.antpofuk.podaci.Korisnik;

/**
 * Klasa sluzi za izvršavanje KorisnikREST servisa
 *
 * @author Antonija Pofuk
 */
@Path("aerodromi")
public class KorisnikREST {

    private UriInfo context;
    private static ServletContext sc;
    BP_Konfiguracija bp_konf;
    String server;
    String baza;
    String korisnik;
    String lozinka;
    String driver;
    Konfiguracija konf;
    String apikey;
    String token;
    String username;
    String password;
    private int ciklus;
    private int pocetak;
    private int trajanje;

    public KorisnikREST() {
    }

    /**
     * Metoda koja se spaja na serversku dretvu preko porta i socketa Salje
     * komande
     *
     * @return odgovor na pokušaj spajanja
     */
    public static String posaljiNaServer(String komanda) {
        String odgovor = "";
        try {
            Socket socket = new Socket("127.0.0.1", 12345);
            InputStream is = socket.getInputStream();
            OutputStream os = socket.getOutputStream();
            StringBuffer buffer = new StringBuffer();
            os.write(komanda.getBytes());
            os.flush();
            socket.shutdownOutput();
            while (true) {
                int znak = is.read();
                if (znak == -1) {
                    break;
                }
                buffer.append((char) znak);
            }
            is.close();
            odgovor = buffer.toString();
        } catch (IOException ex) {
            Logger.getLogger(KorisnikREST.class.getName()).log(Level.SEVERE, null, ex);
        }
        System.err.println("odgovor od servera: " + odgovor);
        return odgovor;
    }

   
    /**
     * Autentifikacija korisnika
     *
     * @param korime
     * @param lozinka
     * @return odgovor
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{aId}/{korime}/{lozinka}")
    public String getJsonId(@PathParam("korime") String korime, @PathParam("lozinka") String loz,
            @PathParam("aId") int autentikacija) {
        Gson gson = new Gson();
        String odgovor = null;
        System.out.println(korime + " " + loz);
        String komanda = "KORISNIK " + korime + "; LOZINKA " + loz + ";";
        String stringOdgovor = posaljiNaServer(komanda);
        boolean uspjesno = stringOdgovor.contains("OK");
        odgovor = gson.toJson(korisnik);
        System.err.println("Uspješno : " + uspjesno);
        if (uspjesno) {
            odgovor = Json.createObjectBuilder()
                    .add("odgovor", "[]")
                    .add("status", "OK")
                    .build().toString();
        } else {
            odgovor = Json.createObjectBuilder()
                    .add("odgovor", Json.createArrayBuilder())
                    .add("status", "ERR")
                    .add("poruka", "Nema podataka!").build().toString();
        }
        return odgovor;
    }

    /**
     * Metoda vraca podatke o svim korisnicima
     *
     * @return lista svih korisnika
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{korime}/{lozinka}")
    public String getJson(@PathParam("korime") String korime, @PathParam("lozinka") String loz) {
        Gson gson = new Gson();
        String odgovor = null;
        System.out.println("Podaci za REST su:" + korime + loz);
        List korisnici = KorisnikClient.dajSveKorisnike(korime,loz);
        odgovor = gson.toJson(korisnici);
        if (odgovor != null) {
            odgovor = Json.createObjectBuilder()
                    .add("odgovor", odgovor)
                    .add("status", "OK")
                    .build().toString();
        } else {
            odgovor = Json.createObjectBuilder()
                    .add("odgovor", Json.createArrayBuilder())
                    .add("status", "ERR")
                    .add("poruka", "Nema podataka!").build().toString();
        }
        return odgovor.toString();
    }

    /**
     * Metoda vraca podatke o jednome korisniku prema UNIQUE vrijednosti korisničkog imena
     *
     * @param id korisnicko ime korisnika
     * @return korisnik
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}")
    public String getJsonKorisnik(@PathParam("id") String id, @PathParam("korime") String korime, @PathParam("lozinka") String loz) {
        Gson gson = new Gson();
    
        String odgovor = null;
        org.foi.nwtis.antpofuk.ws.serveri.Korisnik korisnik = KorisnikClient.dajKorisnika(korime, loz, id);
        odgovor = gson.toJson(korisnik);
        if (odgovor != null) {
            odgovor = Json.createObjectBuilder()
                    .add("odgovor", odgovor)
                    .add("status", "OK")
                    .build().toString();
        } else {
            odgovor = Json.createObjectBuilder()
                    .add("odgovor", Json.createArrayBuilder())
                    .add("status", "ERR")
                    .add("poruka", "Nema podataka!").build().toString();
        }
        return odgovor;
    }

    /**
     * Metoda upisuje podatke o jednome korisniku
     *
     * @param id korisnika
     * @return korisnik
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String postJsonKorisnik(String podaci) {
        Gson gson = new Gson();
        Korisnik korisnik = gson.fromJson(podaci, Korisnik.class);
        boolean dodan = KorisnikClient.dodajKorisnika(korisnik.getKorisnickoIme(), korisnik.getLozinka(), korisnik.getIme(), korisnik.getPrezime());
        String odgovor = "";
    System.out.println("Dodaje se " + korisnik.getKorisnickoIme() + korisnik.getPrezime());

        System.out.println("Dodan je preko resta? " + dodan);
        if (dodan) {
            odgovor = Json.createObjectBuilder()
                    .add("odgovor", odgovor)
                    .add("status", "OK")
                    .build().toString();
        } else {
            odgovor = Json.createObjectBuilder()
                    .add("odgovor", Json.createArrayBuilder())
                    .add("status", "ERR")
                    .add("poruka", "Nije dodan korisnik!").build().toString();
        }
        return odgovor;
    }

    /**
     * Ažuriranje jednog korisnika
     *
     * @param korisnickoIme
     * @param korime
     * @param lozinka
     * @param podaci
     * @return
     */
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("{id}")
    public String putJson(String podaci, @PathParam("korime") String korime, @PathParam("lozinka") String loz) {
        Gson gson = new Gson();
        JsonObject jsonOdgovor;
        Korisnik korisnik = gson.fromJson(podaci, Korisnik.class);
        boolean dodan = KorisnikClient.azurirajKorisnika(korime, loz, korisnik.getKorisnickoIme(), korisnik.getLozinka(), korisnik.getIme(), korisnik.getPrezime());
        String odgovor = "";
        System.out.println("Azuriran je preko resta? " + dodan);
        if (dodan) {
            odgovor = Json.createObjectBuilder()
                    .add("odgovor", odgovor)
                    .add("status", "OK")
                    .build().toString();
        } else {
            odgovor = Json.createObjectBuilder()
                    .add("odgovor", Json.createArrayBuilder())
                    .add("status", "ERR")
                    .add("poruka", "Nije dodan korisnik!").build().toString();
        }
        return odgovor;
    }
}
