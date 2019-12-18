package org.foi.nwtis.antpofuk.web.zrna;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.servlet.ServletContext;
import org.foi.nwtis.antpofuk.konfiguracije.Konfiguracija;
import org.foi.nwtis.antpofuk.konfiguracije.bp.BP_Konfiguracija;
import org.foi.nwtis.antpofuk.web.podaci.Dnevnik;
import org.foi.nwtis.antpofuk.web.slusaci.SlusacAplikacije;
import org.foi.nwtis.antpofuk.ws.serveri.AIRP2WS;

/**
 *
 * @author Antonija Pofuk
 */
@ManagedBean
@SessionScoped
public class PregledDnevnika implements Serializable {

    private List<Dnevnik> lista = new ArrayList<>();
    private Date odDat;
    private Date doDat;
    private String vrsta;
    private int limit = 0;
    private int ukupno = 0;
    private TimeZone timeZone;

    ServletContext sc;
    String server;
    String baza;
    String korisnik;
    String lozinka;
    String driver;
    private BP_Konfiguracija bp_konf;
    private Konfiguracija konf;
    private Integer ciklus;
    private String token;
    private int trajanje;
    private int pocetak;

    /**
     * Creates a new instance of PregledDnevnika
     */
    public PregledDnevnika() {
        ucitajKonfiguraciju();
        dajPodatke();        
    }

    /**
     * Metoda preuzima potrebne konfiguracijske parametre
     * iz datoteke konfiguracije.
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
        ciklus = Integer.parseInt(konf.dajPostavku("ciklus"));
        pocetak = Integer.parseInt(konf.dajPostavku("pocetak"));
        trajanje = Integer.parseInt(konf.dajPostavku("trajanje"));
        token = konf.dajPostavku("token");
        limit = Integer.parseInt(konf.dajPostavku("tablicaBrojRedaka"));
    }
    
    public String dajPodatke() {
        ucitajKonfiguraciju();
        lista.clear();
        try {
            Class.forName(driver);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(PregledKorisnika.class.getName()).log(Level.SEVERE, null, ex);
        }
        try (Connection con = DriverManager.getConnection(baza, korisnik, lozinka);) {
            String upit = "SELECT * FROM dnevnik";
            Statement stmt2 = con.createStatement();
            ResultSet rs2 = stmt2.executeQuery(upit);
            while (rs2.next()) {
                Dnevnik dnevnik = new Dnevnik();
                dnevnik.setId(rs2.getInt("id"));
                dnevnik.setIdKorime(rs2.getString("korime"));
                dnevnik.setStored(rs2.getTimestamp("STORED"));
                dnevnik.setSadrzaj(rs2.getString("URL"));
                dnevnik.setVrsta(rs2.getString("VRSTA"));
                dnevnik.setIp_adresa(rs2.getString("IP_ADRESA"));
                lista.add(dnevnik);
            }
        } catch (SQLException ex) {
            Logger.getLogger(PregledDnevnika.class.getName()).log(Level.SEVERE, null, ex);
        }
        return "dajPodatke";
    }

    public List<Dnevnik> getLista() {
        return lista;
    }

    public void setLista(List<Dnevnik> lista) {
        this.lista = lista;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public TimeZone getTimeZone() {
        return timeZone;
    }

    public void setTimeZone(TimeZone timeZone) {
        this.timeZone = timeZone;
    }

    public Date getOdDat() {
        return odDat;
    }

    public void setOdDat(Date odDat) {
        this.odDat = odDat;
    }

    public Date getDoDat() {
        return doDat;
    }

    public void setDoDat(Date doDat) {
        this.doDat = doDat;
    }

    public String getVrsta() {
        return vrsta;
    }

    public void setVrsta(String vrsta) {
        this.vrsta = vrsta;
    }

    public String pregledKorisnika() {
        return "pregledKorisnika";
    }
    
    private boolean autenticiraj(String korime, String loz){
       ucitajKonfiguraciju();
       boolean postoji = false;
       String upit = "select * from `korisnici` WHERE `korime` = '"+ korime + "' AND "
               + "`lozinka` = '" + loz + "'";
        try {
            Class.forName(driver);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(AIRP2WS.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
        try (Connection con = DriverManager.getConnection(baza, korisnik, lozinka);
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery(upit);) {
            if(rs.next()){
                postoji = true;
                System.out.println("Korisnik postoji");
            }
                stmt.close();
                con.close();
            } catch (Exception e) {
                System.out.println("GRESKA: " + e.getMessage());
            } 
        return postoji;
    }
}
