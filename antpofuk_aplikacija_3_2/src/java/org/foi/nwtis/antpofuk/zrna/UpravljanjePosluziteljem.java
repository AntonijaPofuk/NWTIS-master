package org.foi.nwtis.antpofuk.zrna;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import org.foi.nwtis.antpofuk.konfiguracije.Konfiguracija;
import org.foi.nwtis.antpofuk.konfiguracije.bp.BP_Konfiguracija;
import org.foi.nwtis.antpofuk.podaci.Korisnik;
import org.foi.nwtis.antpofuk.rest.server.KorisnikREST;
import org.foi.nwtis.antpofuk.rest.klijent.NewJerseyClient;

import org.foi.nwtis.antpofuk.slusaci.SlusacAplikacije;
import org.primefaces.context.RequestContext;

/**
 *
 * @author Antonija Pofuk
 */
@Named(value = "upravljanjePosluziteljem")
@SessionScoped
public class UpravljanjePosluziteljem implements Serializable {

    private String korime;

    public String getKorime() {
        return korime;
    }

    public void setKorime(String korime) {
        this.korime = korime;
    }

    public String getLoz() {
        return loz;
    }

    public void setLoz(String loz) {
        this.loz = loz;
    }
    private String loz;
    private boolean error_login = false;
    private String status;
    private String statusGrupe;
    private int port;
    private String server;
    private String korisnik;
    private String lozinka;
    ServletContext sc;
    Konfiguracija konf;
    String prijavljen;
    String lozinkaSesija;
    BP_Konfiguracija bp_konf;
    private String jezik = "hr";
    private List<Korisnik> lista = new ArrayList<>();
    private int limit = 0;
    private String ime;
    private String prezime;
    HttpSession session = (HttpSession) FacesContext.getCurrentInstance().getExternalContext().getSession(false);

    /**
     * Creates a new instance of UpravljanjePosluziteljem
     */
    public UpravljanjePosluziteljem() {
        dajSesiju();
        ucitajKonfiguraciju();

    }

    public void dajSesiju() {
        prijavljen = (String) session.getAttribute("korime");
        lozinkaSesija = (String) session.getAttribute("lozinka");
        System.out.println("Sesija za: " + prijavljen);
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStatusGrupe() {
        return statusGrupe;
    }

    public void setStatusGrupe(String statusGrupe) {
        this.statusGrupe = statusGrupe;
    }

    public void dajStanje() {
        dajSesiju();
        String komanda = "KORISNIK " + prijavljen + "; LOZINKA " + lozinkaSesija + "; STANJE;";
        String odgovor = posaljiNaServer(komanda);
        System.out.println("ODGOVOR: " + odgovor);
        if (odgovor.contains("11")) {
            this.status = "Preuzima sve komande i preuzima  podatke.";
        } else if (odgovor.contains("12")) {
            this.status = "Preuzima sve komande i ne  podatke.";
        } else if (odgovor.contains("13")) {
            this.status = "Preuzima samo poslužiteljske komande i preuzima  podatke.";
        } else if (odgovor.contains("14")) {
            this.status = "Preuzima samo poslužiteljske komande i ne preuzima  podatke.";
        } else {
            this.status = "Problem kod dohvaćanja stanja poslužitelja!";
        }
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

    public void dajStart() {
        String komanda = "KORISNIK " + prijavljen + "; LOZINKA " + lozinkaSesija + "; KRENI;";
        String odgovor = posaljiNaServer(komanda);
        System.out.println("ODGOVOR: " + odgovor);
        if (odgovor.contains("13")) {
            this.status = "Nije u pauzi";
        } else if (odgovor.contains("10")) {
            this.status = "U pauzi je";
        }
    }

    public void dajPauzu() {
        String komanda = "KORISNIK " + prijavljen + "; LOZINKA " + lozinkaSesija + "; PAUZA;";
        String odgovor = posaljiNaServer(komanda);
        System.out.println("ODGOVOR: " + odgovor);
        if (odgovor.contains("11")) {
            this.status = "Je u pauzi";
        } else if (odgovor.contains("10")) {
            this.status = "Nije u pauzi";
        }
    }

    public void dajStani() {
        String komanda = "KORISNIK " + prijavljen + "; LOZINKA " + lozinkaSesija + "; STANI;";
        String odgovor = posaljiNaServer(komanda);
        System.out.println("ODGOVOR: " + odgovor);
        if (odgovor.contains("11")) {
            this.status = "U prekidu je";
        } else if (odgovor.contains("10")) {
            this.status = "Nije u prekidu";
        }
    }

    public void dajAktivno() {
        String komanda = "KORISNIK " + prijavljen + "; LOZINKA " + lozinkaSesija + "; AKTIVNO;";
        String odgovor = posaljiNaServer(komanda);
        System.out.println("ODGOVOR: " + odgovor);
        if (odgovor.contains("15")) {
            this.status = "U aktivnom je";
        } else if (odgovor.contains("10")) {
            this.status = "U pasivnom je";
        }
    }

    public void dajPasivno() {
        String komanda = "KORISNIK " + prijavljen + "; LOZINKA " + lozinkaSesija + "; PASIVNO;";
        String odgovor = posaljiNaServer(komanda);
        System.out.println("ODGOVOR: " + odgovor);
        if (odgovor.contains("10")) {
            this.status = "U aktivnom je";
        } else if (odgovor.contains("14")) {
            this.status = "U pasivnom je";
        }
    }

    public void ucitajKonfiguraciju() {
        sc = (ServletContext) SlusacAplikacije.getServletContext();
        konf = (Konfiguracija) sc.getAttribute("Konfiguracija");
        server = konf.dajPostavku("serverName");
        port = Integer.parseInt(konf.dajPostavku("port"));
        korisnik = konf.dajPostavku("korisnickoImeSVN");
        lozinka = konf.dajPostavku("lozinkaSVN");
        port = Integer.parseInt(konf.dajPostavku("port"));
        System.out.println("port je:" + port);

    }

    public void dajDodajGrupu() {
        String komanda = "KORISNIK " + prijavljen + "; LOZINKA " + lozinkaSesija + "; GRUPA DODAJ;";
        String odgovor = posaljiNaServer(komanda);
        System.out.println("ODGOVOR: " + odgovor);
        if (odgovor.contains("20")) {
            this.statusGrupe = "Nije registrirana";
        } else if (odgovor.contains("20")) {
            this.statusGrupe = "Registrirana je";
        }
    }

    public void dajPrekidGrupa() {
        String komanda = "KORISNIK " + prijavljen+ "; LOZINKA " + lozinkaSesija + "; GRUPA PREKID;";
        String odgovor = posaljiNaServer(komanda);
        System.out.println("ODGOVOR: " + odgovor);
        if (odgovor.contains("20")) {
            this.statusGrupe = "Registrirana je";
        } else if (odgovor.contains("21")) {
            this.statusGrupe = "Nije registrirana";
        }
    }

    public void dajKreniGrupa() {
        String komanda = "KORISNIK " + prijavljen + "; LOZINKA " + lozinkaSesija + "; GRUPA KRENI;";
        String odgovor = posaljiNaServer(komanda);
        System.out.println("ODGOVOR: " + odgovor);
        if (odgovor.contains("20")) {
            this.statusGrupe = "Nije aktivna";
        } else if (odgovor.contains("22")) {
            this.statusGrupe = "Aktivna";
        } else if (odgovor.contains("21")) {
            this.statusGrupe = "Ne postoji";
        }
    }

    public void dajPauzaGrupe() {
        String komanda = "KORISNIK " + prijavljen + "; LOZINKA " + lozinkaSesija + "; GRUPA PAUZA;";
        String odgovor = posaljiNaServer(komanda);
        System.out.println("ODGOVOR: " + odgovor);
        if (odgovor.contains("20")) {
            this.statusGrupe = "Aktivna je";
        } else if (odgovor.contains("23")) {
            this.statusGrupe = "Nije aktivna";
        } else if (odgovor.contains("21")) {
            this.statusGrupe = "Ne postoji";
        }
    }

    public void dajStanjeGrupa() {
        String komanda = "KORISNIK " + prijavljen + "; LOZINKA " + lozinkaSesija + "; GRUPA STANJE;";
        String odgovor = posaljiNaServer(komanda);
        System.out.println("ODGOVOR: " + odgovor);

        if (odgovor.contains("OK 21")) {
            this.statusGrupe = "Grupa je aktivna.";
        } else if (odgovor.contains("OK 22")) {
            this.statusGrupe = "Grupa je blokirana.";
        } else if (odgovor.contains("OK 23")) {
            this.statusGrupe = "Grupa ne postoji.";
        }
    }

    public String login() {
       
        if (this.korime != null && this.loz != null && !this.loz.isEmpty() && !this.korime.isEmpty()) {
            System.out.println(korime + loz);
            error_login = false;
            NewJerseyClient autentikacija = new NewJerseyClient();
            String odgovor = autentikacija.getJsonId("0", korime, loz);
            System.out.println(odgovor);
            if (odgovor.contains("OK")) {
                System.out.println("Prijavljen korisnik:" + odgovor);
                staviUSesiju(korime, loz);
                this.korime = "";
                this.loz = "";
                dajSesiju();
                prikaziPoruku("Prijavili ste se pod korisničkim imenom: " + prijavljen);               
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


    public void odjaviSe() {
        session.removeAttribute("korime");
        session.removeAttribute("lozinka");
        this.prijavljen = null;
        prikaziPoruku("Odjavili ste se!");
    }

    public void staviUSesiju(String user, String pass) {
        session.setAttribute("korime", user);
        session.setAttribute("lozinka", pass);
    }

    public void prikaziPoruku(String String) {
        RequestContext.getCurrentInstance().showMessageInDialog(new FacesMessage(FacesMessage.SEVERITY_INFO, "Upozorenje", String));
    }
}
