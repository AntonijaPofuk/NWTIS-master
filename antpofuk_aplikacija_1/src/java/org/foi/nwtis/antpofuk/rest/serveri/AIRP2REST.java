package org.foi.nwtis.antpofuk.rest.serveri;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.servlet.ServletContext;
import javax.ws.rs.Produces;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PUT;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;
import org.foi.nwtis.antpofuk.konfiguracije.Konfiguracija;
import org.foi.nwtis.antpofuk.konfiguracije.bp.BP_Konfiguracija;
import org.foi.nwtis.antpofuk.web.podaci.Aerodrom;
import org.foi.nwtis.antpofuk.web.slusaci.SlusacAplikacije;
import org.foi.nwtis.antpofuk.ws.serveri.AIRP2WS;
import org.foi.nwtis.rest.klijenti.LIQKlijent;
import org.foi.nwtis.rest.podaci.Lokacija;

/**
 * Klasa sluzi za izvršavanje AIRP"REST servisa
 *
 * @author Antonija Pofuk
 */
@Path("aerodromi")
public class AIRP2REST {

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
    Aerodrom aerodrom;
    String token;
    String username;
    String password;
    private int ciklus;
    private int pocetak;
    private int trajanje;

    public AIRP2REST() {
    }

    /**
     * Metoda provjerava postoji li vec korisnik s tim korisnickim imenom i
     * lozinkom, ako postoji vraca se true u suprotnom false
     *
     * @return true ako postoji korisnik u bazi s navedenim podacima
     */
    private boolean autenticiraj(String korisnicko, String loz) {
        ucitajKonfiguraciju();
        boolean postoji = false;
        String upit = "select * from korisnici WHERE korime = '" + korisnicko + "' AND lozinka = '" + loz + "'";
        try {
            Class.forName(driver);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(AIRP2REST.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
        try (Connection con = DriverManager.getConnection(baza, korisnik, lozinka);
                Statement stmt = con.createStatement();
                ResultSet rs = stmt.executeQuery(upit);) {
            if (rs.next()) {
                postoji = true;
            }
            stmt.close();
            con.close();
        } catch (Exception e) {
            System.out.println("GRESKA: " + e.getMessage());
        }
        return postoji;
    }

    /**
     * Metoda provjerava postoji li vec aerodrom s tim ICAO-kodom Ako postoji
     * vraca se true u suprotnom false
     *
     * @return true ako postoji aerodrom s unesenim ICAO kodom
     */
    private boolean provjeraAerodroma(String id) {
        boolean postoji = false;
        try {
            Class.forName(driver);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(AIRP2REST.class.getName()).log(Level.SEVERE, null, ex);
        }
        try (Connection con = DriverManager.getConnection(baza, korisnik, lozinka);) {
            String upit = "SELECT * FROM MYAIRPORTS WHERE IDENT = '" + id + "'";
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery(upit);
            while (rs.next()) {
                postoji = true;
                System.out.println("Aerodrom postoji");
            }
        } catch (SQLException ex) {
            System.out.println("GRESKA: " + ex);
            postoji = false;
        }
        return postoji;
    }

    public void ucitajKonfiguraciju() {
        sc = SlusacAplikacije.getServletContext();
        bp_konf = (BP_Konfiguracija) sc.getAttribute("BP_Konfig");
        server = bp_konf.getServerDatabase();
        baza = server + bp_konf.getUserDatabase();
        korisnik = bp_konf.getUserUsername();
        lozinka = bp_konf.getUserPassword();
        driver = bp_konf.getDriverDatabase();
        konf = (Konfiguracija) sc.getAttribute("Konfiguracija");
        ciklus = Integer.parseInt(konf.dajPostavku("ciklus"));
        token = konf.dajPostavku("token");
    }

    /**
     * Metoda vraca podatke iz tablice MYAIRPORTS
     *
     * @return an instance of java.lang.String
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/korisnik/{korisnik}/lozinka/{lozinka}")
    public String getJson(@PathParam("korisnik") String korime, @PathParam("lozinka") String loz) {
        System.out.println("Podaci za REST su: " + korime + loz);
        if (autenticiraj(korime, loz)) {
            ucitajKonfiguraciju();
            Gson gson = new Gson();
            String odgovor = null;
            List<Aerodrom> aerodromi = new ArrayList<>();
            LIQKlijent lIQKlijent = new LIQKlijent(token);
            try (Connection con = DriverManager.getConnection(baza, korisnik, lozinka);) {
                String upit = "SELECT * FROM MYAIRPORTS";
                Statement stmt = con.createStatement();
                ResultSet rs = stmt.executeQuery(upit);
                while (rs.next()) {
                    aerodromi.add(new Aerodrom(rs.getString("IDENT"), rs.getString("NAME"),
                            rs.getString("ISO_COUNTRY"), null));
//                for (Aerodrom aerodrom : aerodromi) {
//                    Lokacija geoLocation = lIQKlijent.getGeoLocation(aerodrom.getNaziv());
//                    aerodrom.setLokacija(geoLocation);}
                }
                odgovor = gson.toJson(aerodromi);
                upisiDnevnik(korime, "/antpofuk_aplikacija_1", "REST", "127.0.0.1");

                if (odgovor != null) {
                    odgovor = Json.createObjectBuilder()
                            .add("odgovor", odgovor)
                            .add("status", "OK")
                            .build().toString();

                } else {
                    odgovor = Json.createObjectBuilder()
                            .add("odgovor", Json.createArrayBuilder())
                            .add("status", "ERR")
                            .add("poruka", "Nema podataka!")
                            .build().toString();

                }
            } catch (SQLException ex) {
                System.out.println("GRESKA: " + ex);

            }
            return odgovor;
        }
        return null;
    }

    /**
     * Metoda ažurira podatke o odabranom aerodromu
     *
     * @param podaci String
     * @return String
     * @author Antonija Pofuk
     */
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("{id}/korisnik/{korisnik}/lozinka/{lozinka}")
    public String putJson(@PathParam("korisnik") String korime, @PathParam("lozinka") String loz,
            @PathParam("id") String id, String podaci) {
        ucitajKonfiguraciju();
        boolean ima = false;
        String odgovor = "";
        String naziv, adresa;
        if (autenticiraj(korime, loz)) {
            try {
                JsonObject jsonObject = new JsonParser().parse(podaci).getAsJsonObject();
                naziv = jsonObject.get("naziv").getAsString();
                adresa = jsonObject.get("adresa").getAsString();
                System.out.println("Aerodrom postoji i moze se azurirati!");
                ima = azurirajAerodrom(id, naziv, adresa);
            } catch (JsonSyntaxException e) {
                System.out.println("GRESKA: " + e);
            }
            if (ima) {
                odgovor = Json.createObjectBuilder()
                        .add("odgovor", Json.createArrayBuilder())
                        .add("status", "OK")
                        .build()
                        .toString();
            } else {
                odgovor = Json.createObjectBuilder()
                        .add("odgovor", Json.createArrayBuilder())
                        .add("status", "ERR")
                        .add("poruka", "Pogreska kod azuriranja podataka u bazi! Provjerite ICAO.")
                        .build()
                        .toString();
            }
            upisiDnevnik(korime, "/antpofuk_aplikacija_1", "REST", "127.0.0.1");
            return odgovor;
        }
        return null;
    }

    private boolean azurirajAerodrom(String id, String naziv, String adresa) {
        boolean update = false;
        ucitajKonfiguraciju();
        LIQKlijent lIQKlijent = new LIQKlijent(token);
        Lokacija geoLocation = lIQKlijent.getGeoLocation(id);
        String upit = "UPDATE MYAIRPORTS SET NAME ='" + naziv + "', ISO_COUNTRY ='"
                + adresa + "', COORDINATES =" + geoLocation + ", STORED = CURRENT_TIMESTAMP "
                + "  WHERE IDENT = " + id;
        try {
            Class.forName(driver);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(AIRP2REST.class.getName()).log(Level.SEVERE, null, ex);
        }
        try (Connection con = DriverManager.getConnection(baza, korisnik, lozinka);
                Statement stmt = con.createStatement();) {
            stmt.execute(upit);
            update = true;
            stmt.close();
            con.close();
        } catch (Exception e) {
            System.out.println("GRESKA: " + e.getMessage());
            update = false;
        } finally {
            return update;
        }
    }

    /**
     * Čita podatke o aerodromu iz tablice MYAIRPORTS s odgovarajucim ICAO-om
     *
     * @param id
     * @return
     */
    @Path("{id}/korisnik/{korisnik}/lozinka/{lozinka}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public String getJsonId(@PathParam("korisnik") String korime, @PathParam("lozinka") String loz, @PathParam("id") String id) {
        JsonArrayBuilder json = Json.createArrayBuilder();
        ucitajKonfiguraciju();
        String odgovor = null;
        if (autenticiraj(korime, loz)) {
            if (provjeraAerodroma(id)) {
                try {
                    Class.forName(driver);
                } catch (ClassNotFoundException ex) {
                    Logger.getLogger(AIRP2REST.class.getName()).log(Level.SEVERE, null, ex);
                }
                try (Connection con = DriverManager.getConnection(baza, korisnik, lozinka);) {
                    String upit = "SELECT * FROM MYAIRPORTS WHERE IDENT = '" + id + "'";
                    Statement stmt = con.createStatement();
                    ResultSet rs = stmt.executeQuery(upit);
                    if (rs != null) {
                        while (rs.next()) {
                            json.add(Json.createObjectBuilder()
                                    .add("icao", rs.getString("IDENT"))
                                    .add("naziv", rs.getString("NAME"))
                                    .add("iso_country", rs.getString("iso_country"))
                                    .add("latitude", rs.getString("latitude"))
                                    .add("longitude", rs.getString("longitude"))
                            );
                        }
                        odgovor = Json.createObjectBuilder().add("odgovor", json).add("status", "OK").build().toString();
                    }
                } catch (SQLException ex) {
                    System.out.println("GRESKA: " + ex);
                    odgovor = Json.createObjectBuilder().add("odgovor", Json.createArrayBuilder())
                            .add("status", "ERR")
                            .add("poruka", "Pogreška kod dohvaćanja podataka!")
                            .build().toString();
                }
            } else {
                System.out.println("Nema zapisa pod tim id-om u bazi!");
                odgovor = Json.createObjectBuilder().add("odgovor", Json.createArrayBuilder())
                        .add("status", "ERR")
                        .add("poruka", "Nema podataka za taj aerodrom!")
                        .build().toString();
            }
            upisiDnevnik(korime, "/antpofuk_aplikacija_1", "REST", "127.0.0.1");
            return odgovor;
        }
        return null;
    }

    /**
     * GET čita podatke iz tablice AIRPLANES s odgovarajucim ICAO-om Vraca
     * avione za navedeni aeroddrom
     *
     * @param id je aerodrom
     * @return odgovor
     */
    @Path("{id}/avioni/korisnik/{korisnik}/lozinka/{lozinka}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public String getJsonIdAvioni(@PathParam("id") String id, @PathParam("korisnik") String korime, @PathParam("lozinka") String loz) {

        JsonArrayBuilder json = Json.createArrayBuilder();
        ucitajKonfiguraciju();
        String odgovor = null;
        if (autenticiraj(korime, loz)) {
            if (provjeraAerodroma(id)) {
                try {
                    Class.forName(driver);
                } catch (ClassNotFoundException ex) {
                    Logger.getLogger(AIRP2REST.class.getName()).log(Level.SEVERE, null, ex);
                }
                try (Connection con = DriverManager.getConnection(baza, korisnik, lozinka);) {
                    String upit = "SELECT * FROM AIRPLANES WHERE ESTDEPARTUREAIRPORT ='" + id + "'";
                    Statement stmt = con.createStatement();
                    ResultSet rs = stmt.executeQuery(upit);
                    if (rs != null) {
                        while (rs.next()) {
                            json.add(Json.createObjectBuilder()
                                    .add("icao24", rs.getString("icao24"))
                            );
                        }
                        odgovor = Json.createObjectBuilder().add("odgovor", json).add("status", "OK").build().toString();
                    }
                } catch (SQLException ex) {
                    System.out.println("GRESKA: " + ex);
                    odgovor = Json.createObjectBuilder().add("odgovor", Json.createArrayBuilder())
                            .add("status", "ERR")
                            .add("poruka", "Pogreška kod dohvaćanja podataka!")
                            .build().toString();
                }
            } else {
                System.out.println("Nema zapisa pod tim id-om u bazi!");
                odgovor = Json.createObjectBuilder().add("odgovor", Json.createArrayBuilder())
                        .add("status", "ERR")
                        .add("poruka", "Nema podataka za taj aerodrom!")
                        .build().toString();
            }
            upisiDnevnik(korime, "/antpofuk_aplikacija_1", "REST", "127.0.0.1");
            return odgovor;
        }
        return null;
    }

    /**
     * Metoda upisuje podatke o aerodromu u bazu
     *
     * @param icao aerodroma za koji se vracaju podaci
     * @return true ako je aerodrom dodan odnosno false ako nije
     * @author Antonija Pofuk
     */
    private boolean dodajAerodrom(String icao) {
        ucitajKonfiguraciju();
        LIQKlijent lIQKlijent = new LIQKlijent(token);
        String select = "SELECT * FROM AIRPORTS  WHERE IDENT = '" + icao + "'";
        try {
            Class.forName(driver);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(AIRP2WS.class.getName()).log(Level.SEVERE, null, ex);
        }
        try (Connection con = DriverManager.getConnection(baza, korisnik, lozinka);
                Statement stmt = con.createStatement();
                ResultSet rs = stmt.executeQuery(select)) {
            while (rs.next()) {
                aerodrom = new Aerodrom(rs.getString("IDENT"), rs.getString("NAME"),
                        rs.getString("ISO_COUNTRY"), null);
                if (aerodrom != null) {
                    Lokacija geoLocation = lIQKlijent.getGeoLocation(aerodrom.getNaziv());
                    aerodrom.setLokacija(geoLocation);
                }
            }
            String upit = "INSERT INTO MYAIRPORTS  VALUES "
                    + "('" + aerodrom.getIcao() + "','" + aerodrom.getNaziv() + "' ,' " + aerodrom.getDrzava()
                    + " ',' " + aerodrom.getLokacija().getLatitude() + " ',' " + aerodrom.getLokacija().getLongitude() + "', CURRENT_TIMESTAMP)";
            stmt.executeUpdate(upit);
            System.out.println("Dodan novi aerodrom u MYAIRPORTS");
            return true;
        } catch (SQLException ex) {
            System.err.println("SQLException: " + ex.getMessage());
            return false;
        }
    }

    /**
     * Metoda upisuje novi aerodrom
     *
     * @param podaci
     * @author Antonija Pofuk
     * @return String
     */
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/korisnik/{korisnik}/lozinka/{lozinka}")
    public String postJson(@PathParam("korisnik") String korime, @PathParam("lozinka") String loz, String podaci) {
        ucitajKonfiguraciju();
        boolean dodan = false;
        String odgovor = "";
        if (autenticiraj(korime, loz)) {
            try {
                JsonObject jsonObject = new Gson().fromJson(podaci, JsonObject.class);
                String icao;
                icao = jsonObject.get("icao").getAsString();
                if (provjeraAerodroma(icao) == false) {
                    dodajAerodrom(icao);
                    dodan = true;
                } else {
                    dodan = false;
                }
            } catch (JsonSyntaxException e) {
                System.out.println("JSON!Aerodrom nije moguce dodat: " + e);
                dodan = false;
            }
            if (dodan) {
                odgovor = Json.createObjectBuilder()
                        .add("odgovor", Json.createArrayBuilder())
                        .add("status", "OK")
                        .build()
                        .toString();
            } else {
                odgovor = Json.createObjectBuilder()
                        .add("odgovor", Json.createArrayBuilder())
                        .add("status", "ERR")
                        .add("poruka", "Pogreska kod upisa aerodroma podataka u bazu! Provjerite naziv.")
                        .build()
                        .toString();
            }
            upisiDnevnik(korime, "/antpofuk_aplikacija_1", "REST", "127.0.0.1");
            return odgovor;
        }
        return null;
    }

    public boolean obrisiAerodrom(String icao) {
        String upit = "DELETE from MYAIRPORTS WHERE IDENT = '" + icao + "'";
        boolean del = false;
        try {
            Class.forName(driver);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(AIRP2REST.class.getName()).log(Level.SEVERE, null, ex);
        }
        try (Connection con = DriverManager.getConnection(baza, korisnik, lozinka);
                Statement stmt = con.createStatement();) {
            stmt.execute(upit);
            del = true;
            stmt.close();
            con.close();

        } catch (Exception e) {
            System.out.println("GRESKA: " + e.getMessage());

        } finally {
            return del;
        }
    }

    public boolean obrisiAvione(String icao) {
        String upit = "DELETE from AIRPLANES WHERE ESTDEPARTUREAIRPORT = '" + icao + "'";
        boolean del = false;
        try {
            Class.forName(driver);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(AIRP2REST.class.getName()).log(Level.SEVERE, null, ex);
        }
        try (Connection con = DriverManager.getConnection(baza, korisnik, lozinka);
                Statement stmt = con.createStatement();) {
            stmt.execute(upit);
            del = true;
            stmt.close();
            con.close();

        } catch (Exception e) {
            System.out.println("GRESKA: " + e.getMessage());

        } finally {
            return del;
        }
    }

    public boolean obrisiAvionSAerodroma(String icao, String icao24) {
        String upit = "DELETE from AIRPLANES WHERE icao24 = '" + icao24 + "' AND estdepartureairport = '" + icao + "'";
        boolean del = false;
        try {
            Class.forName(driver);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(AIRP2REST.class.getName()).log(Level.SEVERE, null, ex);
        }
        try (Connection con = DriverManager.getConnection(baza, korisnik, lozinka);
                Statement stmt = con.createStatement();) {
            stmt.execute(upit);
            del = true;
            stmt.close();
            con.close();

        } catch (Exception e) {
            System.out.println("GRESKA: " + e.getMessage());

        } finally {
            return del;
        }
    }

    /**
     * Metoda briše aerodrome pod zadanim ICAO kodom
     *
     * @param id
     * @return String
     * @author Antonija Pofuk
     */
    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @Path("{id}/korisnik/{korisnik}/lozinka/{lozinka}")
    public String deleteJson(@PathParam("id") String id, @PathParam("korisnik") String korime, @PathParam("lozinka") String loz) {
        ucitajKonfiguraciju();
        boolean del = false;
        String odgovor = "";
        if (autenticiraj(korime, loz)) {
            try {
                del = obrisiAerodrom(id);

            } catch (NumberFormatException e) {
                del = false;
            }
            if (del) {
                odgovor = Json.createObjectBuilder()
                        .add("odgovor", Json.createArrayBuilder())
                        .add("status", "OK")
                        .build()
                        .toString();
            } else {
                odgovor = Json.createObjectBuilder()
                        .add("odgovor", Json.createArrayBuilder())
                        .add("status", "ERR")
                        .add("poruka", "Pogreska kod brisanja podataka! Provjerite ICAO.")
                        .build()
                        .toString();
            }
            upisiDnevnik(korime, "/antpofuk_aplikacija_1", "REST", "127.0.0.1");
            return odgovor;
        }
        return null;
    }

    /**
     * Metoda briše avione pod zadanim ICAO kodom
     *
     * @param id
     * @return String
     * @author Antonija Pofuk
     */
    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @Path("{id}/avion/korisnik/{korisnik}/lozinka/{lozinka}")
    public String deleteJsonIdAvioni(@PathParam("id") String id, @PathParam("korisnik") String korime, @PathParam("lozinka") String loz) {
        ucitajKonfiguraciju();
        boolean del = false;
        String odgovor = "";
        if (autenticiraj(korime, loz)) {
            try {
                del = obrisiAvione(id);

            } catch (NumberFormatException e) {
                del = false;
            }
            if (del) {
                odgovor = Json.createObjectBuilder()
                        .add("odgovor", Json.createArrayBuilder())
                        .add("status", "OK")
                        .build()
                        .toString();
            } else {
                odgovor = Json.createObjectBuilder()
                        .add("odgovor", Json.createArrayBuilder())
                        .add("status", "ERR")
                        .add("poruka", "Pogreska kod brisanja podataka! Provjerite ICAO.")
                        .build()
                        .toString();
            }
            upisiDnevnik(korime, "/antpofuk_aplikacija_1", "REST", "127.0.0.1");
            return odgovor;
        }
        return null;
    }

    /**
     * Metoda briše avion pod zadanim ICAO kodom aerodroma i ICAO24 kodom aviona
     *
     * @param id je ICAO kod aerodroma
     * @param aid je ICAO24 kod aviona
     * @return String
     * @author Antonija Pofuk
     */
    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @Path("{id}/avion/{aid}/korisnik/{korisnik}/lozinka/{lozinka}")
    public String deleteJsonAvionSAerodroma(@PathParam("id") String icao, @PathParam("aid") String icao24, @PathParam("korisnik") String korime, @PathParam("lozinka") String loz) {
        ucitajKonfiguraciju();
        boolean del = false;
        String odgovor = "";
        if (autenticiraj(korime, loz)) {
            try {
                del = obrisiAvionSAerodroma(icao, icao24);

            } catch (NumberFormatException e) {
                del = false;
            }
            if (del) {
                odgovor = Json.createObjectBuilder()
                        .add("odgovor", Json.createArrayBuilder())
                        .add("status", "OK")
                        .build()
                        .toString();
            } else {
                odgovor = Json.createObjectBuilder()
                        .add("odgovor", Json.createArrayBuilder())
                        .add("status", "ERR")
                        .add("poruka", "Pogreska kod brisanja podataka! Provjerite ICAO.")
                        .build()
                        .toString();
            }
            upisiDnevnik(korime, "/antpofuk_aplikacija_1", "REST", "127.0.0.1");
            return odgovor;
        }
        return null;
    }

    /**
     * Metoda upisuje podatke u talbicu dnevnik
     */
    private void upisiDnevnik(String korime, String url, String vrsta, String ip_adresa) {
        ucitajKonfiguraciju();
        String upit = "INSERT INTO DNEVNIK VALUES (default,'" + korime + "',default,'" + url + "','" + vrsta + "','" + ip_adresa + "')";
        try {
            Class.forName(driver);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(AIRP2REST.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
        try (Connection con = DriverManager.getConnection(baza, korisnik, lozinka);
                Statement stmt = con.createStatement();) {
            stmt.execute(upit);
            stmt.close();
            con.close();
        } catch (Exception e) {
            System.out.println("GRESKA: " + e.getMessage());
        }
    }

}
