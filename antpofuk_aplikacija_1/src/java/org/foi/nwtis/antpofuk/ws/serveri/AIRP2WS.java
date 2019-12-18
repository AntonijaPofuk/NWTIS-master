package org.foi.nwtis.antpofuk.ws.serveri;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.jws.WebService;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.servlet.ServletContext;
import org.foi.nwtis.antpofuk.konfiguracije.Konfiguracija;
import org.foi.nwtis.antpofuk.konfiguracije.bp.BP_Konfiguracija;
import org.foi.nwtis.antpofuk.rest.serveri.AIRP2REST;
import org.foi.nwtis.antpofuk.web.podaci.Aerodrom;
import org.foi.nwtis.antpofuk.web.podaci.Korisnik;
import org.foi.nwtis.antpofuk.web.slusaci.SlusacAplikacije;
import org.foi.nwtis.antpofuk.web.zrna.ObradaAerodroma;
import org.foi.nwtis.rest.klijenti.OWMKlijent;
import org.foi.nwtis.rest.podaci.AvionLeti;
import org.foi.nwtis.rest.podaci.Lokacija;
import org.foi.nwtis.rest.podaci.MeteoPodaci;

/**
 * Web service klasa sa metodama za obradu zahtjeva
 *
 * @author Antonija Pofuk
 */
@WebService(serviceName = "AIRP2WS")
public class AIRP2WS {

    ServletContext sc;
    BP_Konfiguracija bp_konf;
    String server;
    String baza;
    String korisnik;
    String lozinka;
    String driver;
    Konfiguracija konf;
    boolean postoji = false;
    String apikey;
    String username;
    String password;
    int inicijalniPocetakIntervala;
    int pocetakIntervala;
    int krajIntervala;
    int trajanjeIntervala;
    int trajanjeCiklusaDretve;
    MeteoPodaci meteoPodaci;
    String icao;
    String token;
    Aerodrom aerodrom;
    Korisnik korisnikK;

    Boolean ima = false;
    Lokacija lokacija;

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

    /**
     * Web servis operacija koja vraca sve korisnike pohranjene u bazi podataka
     *
     * @return korisnici
     */
    @WebMethod(operationName = "dajSveKorisnike")
    public List<Korisnik> dajSveKorisnike(@WebParam(name = "korime") String korime, @WebParam(name = "loz") String loz) {
        List<Korisnik> korisnici = new ArrayList<>();
        ucitajKonfiguraciju();
        System.out.println("podaci za autentifikaciju su: " + korime + loz);
        if (autentficiraj(korime, loz)) {
            try (Connection con = DriverManager.getConnection(baza, korisnik, lozinka);) {
                String upit = "SELECT * FROM KORISNICI";
                Statement stmt = con.createStatement();
                ResultSet rs = stmt.executeQuery(upit);
                while (rs.next()) {
                    korisnici.add(new Korisnik(rs.getInt("ID"), rs.getString("KORIME"), rs.getString("LOZINKA"),
                            rs.getString("PREZIME"), rs.getString("IME")));
                }
            } catch (SQLException ex) {
                System.out.println("GRESKA: " + ex);
            }
            upisiDnevnik(korime, "/antpofuk_aplikacija_1", "SOAP", "127.0.0.1");
            return korisnici;
        }
        System.out.println("Nije prosla autentifikacija!");
        return null;
    }

    /**
     * Web servis operacija koja vraca korisnika na temelju id-a
     *
     * @param id je UNIQUE korisničko ime
     * @return korisnik
     */
    @WebMethod(operationName = "dajKorisnika")
    public Korisnik dajKorisnika(@WebParam(name = "korime") String korime, @WebParam(name = "loz") String loz,
            @WebParam(name = "id") String id) {
        ucitajKonfiguraciju();
        if (autentficiraj(korime, loz)) {
            try {
                Class.forName(driver);
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(ObradaAerodroma.class.getName()).log(Level.SEVERE, null, ex);
            }
            try (Connection con = DriverManager.getConnection(baza, korisnik, lozinka);
                    Statement s = con.createStatement();
                    ResultSet rs = s.executeQuery(("SELECT * FROM KORISNICI"
                            + " WHERE korime = '" + id + "'"));) {
                while (rs.next()) {
                    korisnikK = new Korisnik(rs.getInt("ID"), rs.getString("KORIME"),
                            rs.getString("LOZINKA"), rs.getString("IME"), rs.getString("PREZIME"));
                }
            } catch (SQLException ex) {
                System.out.println("GRESKA: " + ex);
            }
            upisiDnevnik(korime, "/antpofuk_aplikacija_1", "SOAP", "127.0.0.1");
            return korisnikK;
        } else {
            System.out.println("Nije prosla autentifikacija!");
            return null;

        }
    }

    /**
     * Metoda koja ucitava potrebne konfiguracijske parametre
     *
     * @author Antonija Pofuk
     */
    public void ucitajKonfiguraciju() {
        sc = SlusacAplikacije.getServletContext();
        bp_konf = (BP_Konfiguracija) sc.getAttribute("BP_Konfig");
        server = bp_konf.getServerDatabase();
        baza = server + bp_konf.getUserDatabase();
        korisnik = bp_konf.getUserUsername();
        lozinka = bp_konf.getUserPassword();
        driver = bp_konf.getDriverDatabase();
        konf = (Konfiguracija) sc.getAttribute("Konfiguracija");
        apikey = konf.dajPostavku("apikey");
        token = konf.dajPostavku("token");
        username = konf.dajPostavku("username");
        password = konf.dajPostavku("password");
    }

    /**
     * Metoda provjerava postoji li aerodrom pod tim ICAO-kodom
     *
     * @author Antonija Pofuk return Boolean
     * @return true ako postoji false ako ne postoji aerodrom s tim ICAO kodom
     */
    private Boolean provjeriJelPostojiId(String icao) {
        try {
            Class.forName(driver);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(AIRP2WS.class.getName()).log(Level.SEVERE, null, ex);
        }
        try (Connection con = DriverManager.getConnection(baza, korisnik, lozinka);) {
            String upit = "SELECT * FROM MYAIRPORTS WHERE IDENT = " + icao;
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery(upit);
            while (rs.next()) {
                ima = true;
                System.out.println("ID postoji");
            }
        } catch (SQLException ex) {
            System.out.println("GRESKA: " + ex);
            return false;
        }
        return ima;
    }

    /**
     * Web servis koji vraca podatke za avione koji su poletjeli u određenom
     * intervalu sa određenog aerodroma
     *
     * @param icao
     * @param odVremena
     * @param doVremena
     * @return List aviona
     */
    @WebMethod(operationName = "dajAvionePoletjeleSAerodroma")
    public List<AvionLeti> dajAvioneSAerodroma(@WebParam(name = "icao") String icao,
            @WebParam(name = "odVremena") int odVremena, @WebParam(name = "doVremena") int doVremena,
            @WebParam(name = "korisnickoIme") String korisnickoIme, @WebParam(name = "loz") String loz) {
        ucitajKonfiguraciju();
        String upit = "SELECT * FROM AIRPLANES WHERE ESTDEPARTUREAIRPORT != 'NULL' AND ESTDEPARTUREAIRPORT = '" + icao
                + "' AND LASTSEEN >= " + odVremena + " AND LASTSEEN <= " + doVremena;
        List<AvionLeti> departures = new ArrayList<>();

        if (autentficiraj(korisnickoIme, loz)) {
            try {
                Class.forName(driver);
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(ObradaAerodroma.class.getName()).log(Level.SEVERE, null, ex);
            }
            try (Connection con = DriverManager.getConnection(baza, korisnik, lozinka);
                    Statement s = con.createStatement();
                    ResultSet rs = s.executeQuery(upit)) {
                while (rs.next()) {
                    AvionLeti avionLeti = new AvionLeti();
                    avionLeti.setIcao24(rs.getString("ICAO24"));
                    avionLeti.setFirstSeen(rs.getInt("FIRSTSEEN"));
                    avionLeti.setEstDepartureAirport(rs.getString("EstDepartureAirport"));
                    avionLeti.setLastSeen(rs.getInt("LASTSEEN"));
                    avionLeti.setEstArrivalAirport(rs.getString("EstArrivalAirport"));
                    avionLeti.setCallsign(rs.getString("Callsign"));
                    avionLeti.setEstDepartureAirportHorizDistance(rs.getInt("EstDepartureAirportHorizDistance"));
                    avionLeti.setEstDepartureAirportVertDistance(rs.getInt("EstDepartureAirportVertDistance"));
                    avionLeti.setEstArrivalAirportHorizDistance(rs.getInt("EstArrivalAirportHorizDistance"));
                    avionLeti.setEstArrivalAirportVertDistance(rs.getInt("EstArrivalAirportVertDistance"));
                    avionLeti.setDepartureAirportCandidatesCount(rs.getInt("DepartureAirportCandidatesCount"));
                    avionLeti.setArrivalAirportCandidatesCount(rs.getInt("ArrivalAirportCandidatesCount"));
                    departures.add(avionLeti);
                }
            } catch (SQLException ex) {
                System.out.println("GRESKA: " + ex);
            }
            upisiDnevnik(korisnickoIme, "/antpofuk_aplikacija_1", "SOAP", "127.0.0.1");
            return departures;
        } else {
            System.out.println("Nije prosla autentifikacija!");
            return null;
        }
    }

    /**
     * Web servis koji vraca podatke za određeni avion koji je poletio u
     * određenom intervalu sa određenog aerodroma
     *
     * @param icao24 avion
     * @param icao aerodrom
     * @param odVremena početak
     * @param doVremena kraj
     * @return Lista aviiona
     */
    @WebMethod(operationName = "dajAvioneUIntervalu")
    public List<AvionLeti> dajAvioneUIntervalu(@WebParam(name = "icao24") String icao24, @WebParam(name = "odVremena") int odVremena, @WebParam(name = "doVremena") int doVremena,
            @WebParam(name = "korisnickoIme") String korisnickoIme, @WebParam(name = "loz") String loz) {
        ucitajKonfiguraciju();
        String upit = "SELECT * FROM AIRPLANES WHERE icao24='" + icao24 + "' AND  FIRSTSEEN BEETWEEN " + odVremena;
        List<AvionLeti> departures = new ArrayList<>();

        if (autentficiraj(korisnickoIme, loz)) {
            try {
                Class.forName(driver);
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(ObradaAerodroma.class.getName()).log(Level.SEVERE, null, ex);
            }
            try (Connection con = DriverManager.getConnection(baza, korisnik, lozinka);
                    Statement s = con.createStatement();
                    ResultSet rs = s.executeQuery(upit)) {
                while (rs.next()) {
                    AvionLeti avionLeti = new AvionLeti();
                    avionLeti.setIcao24(rs.getString("ICAO24"));
                    avionLeti.setFirstSeen(rs.getInt("FIRSTSEEN"));
                    avionLeti.setEstDepartureAirport(rs.getString("EstDepartureAirport"));
                    avionLeti.setLastSeen(rs.getInt("LASTSEEN"));
                    avionLeti.setEstArrivalAirport(rs.getString("EstArrivalAirport"));
                    avionLeti.setCallsign(rs.getString("Callsign"));
                    avionLeti.setEstDepartureAirportHorizDistance(rs.getInt("EstDepartureAirportHorizDistance"));
                    avionLeti.setEstDepartureAirportVertDistance(rs.getInt("EstDepartureAirportVertDistance"));
                    avionLeti.setEstArrivalAirportHorizDistance(rs.getInt("EstArrivalAirportHorizDistance"));
                    avionLeti.setEstArrivalAirportVertDistance(rs.getInt("EstArrivalAirportVertDistance"));
                    avionLeti.setDepartureAirportCandidatesCount(rs.getInt("DepartureAirportCandidatesCount"));
                    avionLeti.setArrivalAirportCandidatesCount(rs.getInt("ArrivalAirportCandidatesCount"));
                    departures.add(avionLeti);
                }
            } catch (SQLException ex) {
                System.out.println("GRESKA: " + ex);
            }
            upisiDnevnik(korisnickoIme, "/antpofuk_aplikacija_1", "SOAP", "127.0.0.1");
            return departures;
        } else {
            System.out.println("Nije prosla autentifikacija!");
            return null;
        }
    }

    /**
     * Web servis koji vraca određeni broj zadnjih aerodroma i njihovih podataka
     *
     * @param n broj aerodroma
     */
    @WebMethod(operationName = "dajZadnjeNPreuzeteAerodromPodatke")
    public List<AvionLeti> dajZadnjeNPodatkeAerodrom(@WebParam(name = "icao") String icao,
            @WebParam(name = "korisnickoIme") String korisnickoIme, @WebParam(name = "lozinka") String loz,
            @WebParam(name = "brojN") int brojN) {
        ucitajKonfiguraciju();
        String upit = "SELECT * FROM AIRPLANES WHERE EstDepartureAirport='" + icao + "' ORDER BY storedOn LIMIT " + brojN;

        List<AvionLeti> departures = new ArrayList<>();

        if (autentficiraj(korisnickoIme, loz)) {
            try {
                Class.forName(driver);
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(ObradaAerodroma.class.getName()).log(Level.SEVERE, null, ex);
            }
            try (Connection con = DriverManager.getConnection(baza, korisnik, lozinka);
                    Statement s = con.createStatement();
                    ResultSet rs = s.executeQuery(upit)) {
                while (rs.next()) {
                    System.out.println("dohvacam avione");
                    AvionLeti avionLeti = new AvionLeti();
                    avionLeti.setIcao24(rs.getString("ICAO24"));
                    avionLeti.setFirstSeen(rs.getInt("FIRSTSEEN"));
                    avionLeti.setEstDepartureAirport(rs.getString("EstDepartureAirport"));
                    avionLeti.setLastSeen(rs.getInt("LASTSEEN"));
                    avionLeti.setEstArrivalAirport(rs.getString("EstArrivalAirport"));
                    avionLeti.setCallsign(rs.getString("Callsign"));
                    avionLeti.setEstDepartureAirportHorizDistance(rs.getInt("EstDepartureAirportHorizDistance"));
                    avionLeti.setEstDepartureAirportVertDistance(rs.getInt("EstDepartureAirportVertDistance"));
                    avionLeti.setEstArrivalAirportHorizDistance(rs.getInt("EstArrivalAirportHorizDistance"));
                    avionLeti.setEstArrivalAirportVertDistance(rs.getInt("EstArrivalAirportVertDistance"));
                    avionLeti.setDepartureAirportCandidatesCount(rs.getInt("DepartureAirportCandidatesCount"));
                    avionLeti.setArrivalAirportCandidatesCount(rs.getInt("ArrivalAirportCandidatesCount"));
                    departures.add(avionLeti);
                }
            } catch (SQLException ex) {
                System.out.println("GRESKA: " + ex);
            }
            upisiDnevnik(korisnickoIme, "/antpofuk_aplikacija_1", "SOAP", "127.0.0.1");
            return departures;
        } else {
            System.out.println("Nije prosla autentifikacija!");
            return null;
        }
    }

    @WebMethod(operationName = "dajZadnjePreuzeteAerodromPodatke")
    public AvionLeti dajZadnjePodatkeAerodrom(@WebParam(name = "icao") String icao,
            @WebParam(name = "korisnickoIme") String korisnickoIme, @WebParam(name = "lozinka") String loz) {
        ucitajKonfiguraciju();
        String upit = "SELECT * FROM AIRPLANES WHERE EstDepartureAirport='" + icao + "' ORDER BY storedOn LIMIT 1";
        AvionLeti avionLeti = new AvionLeti();

        if (autentficiraj(korisnickoIme, loz)) {
            try {
                Class.forName(driver);
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(ObradaAerodroma.class.getName()).log(Level.SEVERE, null, ex);
            }
            try (Connection con = DriverManager.getConnection(baza, korisnik, lozinka);
                    Statement s = con.createStatement();
                    ResultSet rs = s.executeQuery(upit)) {
                while (rs.next()) {
                    System.out.println("dohvacam avione");
                    avionLeti.setIcao24(rs.getString("ICAO24"));
                    avionLeti.setFirstSeen(rs.getInt("FIRSTSEEN"));
                    avionLeti.setEstDepartureAirport(rs.getString("EstDepartureAirport"));
                    avionLeti.setLastSeen(rs.getInt("LASTSEEN"));
                    avionLeti.setEstArrivalAirport(rs.getString("EstArrivalAirport"));
                    avionLeti.setCallsign(rs.getString("Callsign"));
                    avionLeti.setEstDepartureAirportHorizDistance(rs.getInt("EstDepartureAirportHorizDistance"));
                    avionLeti.setEstDepartureAirportVertDistance(rs.getInt("EstDepartureAirportVertDistance"));
                    avionLeti.setEstArrivalAirportHorizDistance(rs.getInt("EstArrivalAirportHorizDistance"));
                    avionLeti.setEstArrivalAirportVertDistance(rs.getInt("EstArrivalAirportVertDistance"));
                    avionLeti.setDepartureAirportCandidatesCount(rs.getInt("DepartureAirportCandidatesCount"));
                    avionLeti.setArrivalAirportCandidatesCount(rs.getInt("ArrivalAirportCandidatesCount"));
                }
            } catch (SQLException ex) {
                System.out.println("GRESKA: " + ex);
            }
            upisiDnevnik(korisnickoIme, "/antpofuk_aplikacija_1", "SOAP", "127.0.0.1");
            return avionLeti;
        } else {
            System.out.println("Nije prosla autentifikacija!");
            return null;
        }
    }

    @WebMethod(operationName = "dajVazeceMeteoPodatke")
    public MeteoPodaci dajVazeceMeteoPodatke(@WebParam(name = "icao") String icao, @WebParam(name = "korisnickoIme") String korisnickoIme, @WebParam(name = "loz") String loz) {
        MeteoPodaci mp = null;
        String latitude = "";
        String longitude = "";
        ucitajKonfiguraciju();
        if (autentficiraj(korisnickoIme, loz)) {
            try {
                Class.forName(driver);
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(AIRP2WS.class.getName()).log(Level.SEVERE, null, ex);
            }
            try (Connection con = DriverManager.getConnection(baza, korisnik, lozinka);) {
                String upit = "SELECT * FROM MYAIRPORTS WHERE IDENT = '" + icao + "'";
                Statement stmt = con.createStatement();
                ResultSet rs = stmt.executeQuery(upit);
                rs.next();
                latitude = String.valueOf(rs.getFloat("latitude"));
                longitude = String.valueOf(rs.getFloat("longitude"));
                OWMKlijent oWMKlijent = new OWMKlijent(apikey);
                mp = oWMKlijent.getRealTimeWeather(latitude, longitude);
            } catch (SQLException ex) {
                System.out.println("GRESKA: " + ex);
                return null;
            }
            upisiDnevnik(korisnickoIme, "/antpofuk_aplikacija_1", "SOAP", "127.0.0.1");
            return mp;
        } else {
            System.out.println("Nije prosla autentifikacija!");
            return null;
        }
    }

    /**
     * Web servis koji provjerava prijavljenog korisnika
     *
     * @param korisnicko korisnicko ime
     * @param loz lozinka
     * @return true ukoliko je uspješna autentifikacija odnosno false ukoliko
     * nije
     */
    private boolean autentficiraj(String korisnicko, String loz) {
        ucitajKonfiguraciju();
        boolean postoji = false;
        String upit = "select * from `korisnici` WHERE `korime` = '" + korisnicko + "' AND `lozinka` = '" + loz + "'";
        try {
            Class.forName(driver);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(AIRP2WS.class
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
            postoji = false;
        }
        return postoji;
    }

    /**
     * Web servis dodaje korisnika u bazu podataka
     *
     * @param ime
     * @param prezime
     * @param korime
     * @param lozinka
     */
    @WebMethod(operationName = "dodajKorisnika")
    public Boolean dodajKorisnika(@WebParam(name = "ime") String ime, @WebParam(name = "prezime") String prezime,
            @WebParam(name = "korime") String korime, @WebParam(name = "lozinka") String loz) {
        ucitajKonfiguraciju();
        String upit = "INSERT INTO KORISNICI VALUES "
                + "(default,'" + ime + "','" + prezime + "','" + korime + "','" + loz + "', default)";
            try {
                Class.forName(driver);
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(AIRP2WS.class.getName()).log(Level.SEVERE, null, ex);
            }
            try (Connection con = DriverManager.getConnection(baza, korisnik, lozinka);
                    Statement stmt = con.createStatement();) {
                stmt.executeUpdate(upit);
                System.out.println("Dodan novi korisnik");
                upisiDnevnik(korime, "/antpofuk_aplikacija_1", "SOAP", "127.0.0.1");

                return true;
            } catch (SQLException ex) {
                Logger.getLogger(AIRP2WS.class.getName()).log(Level.SEVERE, null, ex);
                System.out.println("Nije dodan novi korisnik, provjerite podatke! " + ex);
                return false;
            }
        }
       
    /**
     * Web servis operacija ažurira podatke za određenog korisnika
     *
     * @return korisnici
     */
    @WebMethod(operationName = "azurirajKorisnika")
    public Boolean azurirajKorisnika(@WebParam(name = "korimeP") String korimeP, @WebParam(name = "lozP") String lozP,@WebParam(name = "korime") String korime, @WebParam(name = "lozinka") String loz,
            @WebParam(name = "ime") String ime, @WebParam(name = "prezime") String prezime) {
        ucitajKonfiguraciju();
        String upit = "UPDATE KORISNICI SET ime = '" + ime + "', prezime = '" + prezime + "', korime ='" + korime + "'"
                + "WHERE korime = '" + korime+"'";
            try {
                Class.forName(driver);
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(AIRP2WS.class.getName()).log(Level.SEVERE, null, ex);
            }
            try (Connection con = DriverManager.getConnection(baza, korisnik, lozinka);
                Statement stmt = con.createStatement();) {
                stmt.executeUpdate(upit);
                System.out.println("Azuriran je korisnik " + ime + prezime);
                upisiDnevnik(korime, "/antpofuk_aplikacija_1", "SOAP", "127.0.0.1");
                return true;
            } catch (SQLException ex) {
                Logger.getLogger(AIRP2WS.class.getName()).log(Level.SEVERE, null, ex);
                System.out.println("Nije azuriran korisnik, provjerite podatke! " + ex);
                return false;
            }
        }
//        System.out.println("Nije prosla autentifikacija!");
//        return null;
//    }
}
