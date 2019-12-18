
package org.foi.nwtis.antpofuk.web.zrna;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.servlet.ServletContext;
import org.foi.nwtis.antpofuk.konfiguracije.Konfiguracija;
import org.foi.nwtis.antpofuk.konfiguracije.bp.BP_Konfiguracija;
import org.foi.nwtis.antpofuk.web.podaci.Korisnik;
import org.foi.nwtis.antpofuk.web.slusaci.SlusacAplikacije;


/**
 * Klasa koja služi za pregled korisnika
 * @author Antonija Pofuk
 */
@ManagedBean
@SessionScoped
public class PregledKorisnika implements Serializable {

    private List<Korisnik> lista = new ArrayList<>();

   
    private int pocetak;
    private int ukupno = 0; 
    private int limit = 0;
    private int start = 0;

    private String ime;
    private String prezime;
    
    ServletContext sc;
    BP_Konfiguracija bp_konf;
    String server;
    String baza;
    String korisnik;
    String lozinka;
    String driver;
    Konfiguracija konf;
    int ciklus;
    int trajanje;
    String token;
    /**
     * Creates a new instance of PregledKorisnika
     */
    public PregledKorisnika() {
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
    
    public String dajPodatke(){
     ucitajKonfiguraciju();
            lista.clear();
       try {
                Class.forName(driver);
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(PregledKorisnika.class.getName()).log(Level.SEVERE, null, ex);
            }
            try (Connection con = DriverManager.getConnection(baza, korisnik, lozinka);) {
                String upit = "SELECT COUNT(*) AS broj FROM `korisnici`";
                Statement stmt = con.createStatement();
                ResultSet rs = stmt.executeQuery(upit); 
                rs.next();
                this.ukupno = rs.getInt("broj");
                rs.close();
                stmt.close();                
                String upit2 = "SELECT * FROM `korisnici`";
                Statement stmt2 = con.createStatement();
                ResultSet rs2 = stmt2.executeQuery(upit2); 
                while(rs2.next()){
                    Korisnik k = new Korisnik();
                    k.setId(rs2.getInt("id"));
                    k.setKorisnickoIme(rs2.getString("korime"));
                    k.setLozinka(rs2.getString("lozinka"));
                    k.setIme(rs2.getString("ime"));
                    k.setPrezime(rs2.getString("prezime"));
                    lista.add(k);
                    System.out.println(k.getKorisnickoIme());
                }
                rs2.close();
                stmt2.close();
            }catch (SQLException ex) {
                Logger.getLogger(PregledKorisnika.class.getName()).log(Level.SEVERE, null, ex);
            }
           return "dajKorisnike"; 
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
     public String odjavi() {
        return "odjava";
    }

    public String pregledDnevnika() {
        return "pregledDnevnika";
    }

    
}
