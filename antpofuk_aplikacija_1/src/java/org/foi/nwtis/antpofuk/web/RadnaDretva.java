package org.foi.nwtis.antpofuk.web;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.ServletContext;
import org.foi.nwtis.antpofuk.konfiguracije.Konfiguracija;
import org.foi.nwtis.antpofuk.konfiguracije.bp.BP_Konfiguracija;
import org.foi.nwtis.antpofuk.web.slusaci.SlusacAplikacije;
import org.foi.nwtis.antpofuk.ws.StatusKorisnika;
import org.foi.nwtis.antpofuk.ws.klijenti.AerodromClient;

/**
 * Klasa dretve koja sluzi za komunikaciju s serverskom dretvom i prima komande
 *
 * @author Antonija Pofuk
 */
class RadnaDretva extends Thread {

    public static volatile boolean pauza = false;
    public static volatile boolean preuzimaSveKomande = true;
    Socket socket;
    Date vrijeme;
    Konfiguracija konf;
    BP_Konfiguracija bp_konf;
    ServletContext sc;
    OutputStream out;
    private String naredba;
    private String server;
    private String baza;
    private String korisnik;
    private String lozinkaBaza;
    private String driver;
    String korime;
    String lozinkaC;
    public RadnaDretva(Socket socket, Date vrijeme) {
        this.socket = socket;
        this.vrijeme = vrijeme;
    }

    @Override
    public void interrupt() {
        super.interrupt();

    }

    @Override
    public void run() {
        System.out.println(korime + lozinkaC);
        InputStream is = null;
        OutputStream os = null;
        try {
            is = socket.getInputStream();
            os = socket.getOutputStream();
            this.out = os;
            StringBuilder buffer = new StringBuilder();
            while (true) {
                int znak = is.read();
                if (znak == -1) {
                    break;
                }
                buffer.append((char) znak);
            }
            System.out.println("Komanda: " + buffer.toString());
            naredba = buffer.toString().trim();
            System.out.println("Naredba: " + naredba);
            String radi = "";
            int status = testirajNaredbu(naredba);
         switch (status) {
                case 1: 
                    radi = "OK 10;";
                    break;
                case 4:
                    radi = kreni();
                    break;
                case 5:
                    radi = pasivno();
                    break;
                case 6:
                    radi = aktivno();
                    break;
                case 7:
                    radi = stani();
                    break;
                case 8:
                    radi = stanje();
                    break;
                case 9:                   
                    break;
                case 10:
                    radi = dodajGrupu(naredba);
                    break;
                case 11:
                    radi = prekid(naredba);
                    break;
                case 12:
                    radi = kreniGrupa(naredba);
                    break;
                case 13:
                    radi = pauzaGrupa(naredba);
                    break;
                case 14:
                    radi = stanjeGrupa(naredba);
                    break;
                case 100: 
                    radi = "ERR 11; Nije dobra lozinka ili korime";
                    break;
                case -1:
                    break;
                    
            }
            os.write(radi.getBytes());
            os.flush();
        } catch (IOException ex) {
            Logger.getLogger(RadnaDretva.class.getName()).log(Level.SEVERE, null, ex);
        }finally {
            try {
                if (is != null) {
                    is.close();
                }
                if (os != null) {
                    os.close();
                }
                if (socket != null) {
                    socket.close();
                }
            } catch (IOException ex) {
                System.out.println("ERROR; Problem kod zatvaranja socketa ;" + ex);
            }
        }        
    }


    @Override
    public synchronized void start() {
        super.start();
    }
    
    /**
     * Metoda provjerava dobivenu naredbu odgovara li navedenim regexima
     *   
     */
    private int testirajNaredbu(String naredba) {
        String[] param = naredba.split(" ");
        String komanda = "^KORISNIK ([^\\s]+); LOZINKA ([^\\s]+);$";
        String komanda1 = "^KORISNIK ([^\\s]+); LOZINKA ([^\\s]+); (PAUZA|KRENI|PASIVNO|AKTIVNO|STANI|STANJE);$";
        String komanda2 = "^KORISNIK ([^\\s]+); LOZINKA ([^\\s]+); GRUPA (DODAJ|PREKID|KRENI|PAUZA|STANJE);$";
        if ("KORISNIK".equals(param[0]) && "LOZINKA".equals(param[2])) {
            Pattern pattern1 = Pattern.compile(komanda1);
            Matcher matcher1 = pattern1.matcher(naredba);
            boolean prvi = matcher1.matches();
            boolean korisnik = korisnikProvjera(param[1], param[3]);
            if (korisnik) {
                Pattern pattern = Pattern.compile(komanda);
                Matcher matcher = pattern.matcher(naredba);
                boolean auth = matcher.matches();
                System.out.println("Autentifikacija je prosla? " + auth);
                Pattern pattern2 = Pattern.compile(komanda1);
                Matcher matcher2 = pattern2.matcher(naredba);
                boolean drugi = matcher2.matches();
                System.out.println("Ima li komanda za poslužitelja? " + drugi);
                Pattern pattern3 = Pattern.compile(komanda2);
                Matcher matcher3 = pattern3.matcher(naredba);
                boolean treci = matcher3.matches();
                System.out.println("Ima li komanda za grupu? " + treci);
                if (auth){
                        System.out.println("Samo autentikacija");
                        return 1;
                    } else if (drugi) {
                        if (null == param[4]) {
                            return 9;
                        } else switch (param[4]) {
                        case "PAUZA;":
                            return 3;
                        case "KRENI;":
                            return 4;
                        case "PASIVNO;":
                            return 5;
                        case "AKTIVNO;":
                            return 6;
                        case "STANI;":
                            return 7;
                        case "STANJE;":
                            return 8;
                        default:
                            return 9;
                    }
                } else if (treci) {
                    System.out.println("za grupu");
                    if (null == param[5]) {
                        return 14;
                    } else switch (param[5]) {
                        case "DODAJ;":
                            return 10;
                        case "PREKID;":
                            return 11;
                        case "KRENI;":
                            return 12;
                        case "PAUZA;":
                            return 13;
                        default:
                            return 14;
                    }
                } else {
                    System.out.println("Niti jedna naredba ne odgovara");
                    return -1;
                }
            } else {
                System.out.println("Korisnik ne odgovara");
                return 100;
            }
        } else {
            return -1;
        }
    }

    /**
     * Metoda provjerava korisnika je li u bazi
     * @param korisnickoIme
     * @param lozinka
     * @return 
     */
    private boolean korisnikProvjera(String korisnickoIme, String lozinka) {
        korisnickoIme = korisnickoIme.substring(0, korisnickoIme.length() - 1);
        lozinka = lozinka.substring(0, lozinka.length() - 1);
        ucitajKonfiguraciju();
        String upit = "SELECT * FROM korisnici WHERE korime = '" + korisnickoIme
                + "' AND lozinka = '" + lozinka + "'";
        try {
            Class.forName(driver);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(RadnaDretva.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
        try (Connection conection = DriverManager.getConnection(baza, korisnik, lozinkaBaza);
                Statement stmt = conection.createStatement();
                ResultSet resultSet = stmt.executeQuery(upit);) {
            if (resultSet.next()) {
                System.out.println("Korisnik je u redu");
                return true;
            } else {
                System.out.println("Korisnik nije u redu");
                return false;
            }
        } catch (Exception e) {
            System.out.println("GREŠKA kod dohvaćanja korisnika: " + e.getMessage());
            return false;
        }
    }

    private String pauziraj(String naredba) {
        if (preuzimaSveKomande == false) {
            return "ERR 12;";
        }
        preuzimaSveKomande = false;
        return "OK 10";
    }

    public synchronized void setPauza() {
        pauza = !pauza;
    }

    public String kreni() {
        if (preuzimaSveKomande == true) {
            return "ERR 13;";
        }
        preuzimaSveKomande = true;
        return "OK 10;";
    }

    private String pasivno() {
        if (PreuzimanjeAviona.kraj == false) {
            PreuzimanjeAviona.kraj = true;
            return "OK 10;";
        } else {
            return "ERR 14;";
        }
    }

    private String aktivno() {
        if (PreuzimanjeAviona.kraj == true) {
            PreuzimanjeAviona.kraj = false;
            return "OK 10;";
        } else {
            return "ERR 15;";
        }
    }

    public String stani() throws IOException {
        if (ServerSustava.serverPauza == true) {
            return "ERR 16;";
        } else {
            pauza = true;
            PreuzimanjeAviona.kraj = true;
            ServerSustava.serverPauza = true;
            ServerSustava.serverSocket.close();
            return "OK 10;";
        }
    }

    public String stanje() {
        if (PreuzimanjeAviona.kraj == false && preuzimaSveKomande == true) {
            return "OK 11;";
        } else if (PreuzimanjeAviona.kraj == true && preuzimaSveKomande == true) {
            return "OK 12;";
        } else if (preuzimaSveKomande == false && PreuzimanjeAviona.kraj == false) {
            return "OK 13;";
        } else {
            return "OK 14;";
        }
    }

    private String dodajGrupu(String naredba) {
        String[] nar = naredba.split(" ");
        if (preuzimaSveKomande == true) {
            boolean aut = AerodromClient.autenticirajGrupu(korime, lozinkaC);
            if (aut) {
                boolean reg = AerodromClient.registrirajGrupu(korime, lozinkaC);
                if (reg) {
                    return "OK 20;";
                } else {
                    return "ERR 20;";
                }
            } else {
                return "ERR 20;";
            }
        } else {
            return "ERR; Server je u pauzi!";
        }
    }

    private String prekid(String naredba) {
        String[] nar = naredba.split(" ");
        if (preuzimaSveKomande == true) {
            boolean aut = AerodromClient.autenticirajGrupu(korime, lozinkaC);
            if (aut) {
                boolean dereg = AerodromClient.deregistrirajGrupu(korime, lozinkaC);
                if (dereg) {
                    return "OK 20;";
                } else {
                    return "ERR 21;";
                }
            } else {
                return "ERR 21;";
            }
        } else {
            return "ERR; Server je u pauzi!";
        }
    }

    private String kreniGrupa(String naredba) {
        String[] nar = naredba.split(" ");
        if (preuzimaSveKomande == true) {
            boolean postoji = AerodromClient.autenticirajGrupu(korime, lozinkaC);
            if (postoji) {
                boolean akt = AerodromClient.aktivirajGrupu(korime, lozinkaC);
                if (akt) {
                    return "OK 20;";
                } else {
                    return "ERR 22;";
                }
            } else {
                return "ERR 21;";
            }
        } else {
            return "ERR; Server je pauziran!";
        }
    }

    private String pauzaGrupa(String naredba) {
        String[] nar = naredba.split(" ");
        if (preuzimaSveKomande == true) {
            boolean postoji = AerodromClient.autenticirajGrupu(korime, lozinkaC);
            if (postoji) {
                boolean akt = AerodromClient.blokirajGrupu(korime, lozinkaC);
                if (akt) {
                    return "OK 20;";
                } else {
                    return "ERR 23;";
                }
            } else {
                return "ERR 21;";
            }
        } else {
            return "ERR; Server je pauziran!";
        }
    }

    private String stanjeGrupa(String naredba) {
        String[] nar = naredba.split(" ");
        if (preuzimaSveKomande == true) {
            StatusKorisnika status = AerodromClient.dajStatusGrupe(korime, lozinkaC);
            if (null == status) {
                return "ERR 23;";
            } else switch (status) {
                case AKTIVAN:
                    return "OK 21;";
                case BLOKIRAN:
                    return "OK 22;";
                default:
                    return "ERR 23;";
            }
        } else {
            return "ERR; Server je pauziran!";
        }
    }

    public void ucitajKonfiguraciju() {
        sc = SlusacAplikacije.getServletContext();
        bp_konf = (BP_Konfiguracija) sc.getAttribute("BP_Konfig");
        server = bp_konf.getServerDatabase();
        baza = server + bp_konf.getUserDatabase();
        korisnik = bp_konf.getUserUsername();
        lozinkaBaza = bp_konf.getUserPassword();
        driver = bp_konf.getDriverDatabase();
        konf = (Konfiguracija) sc.getAttribute("Konfiguracija");
        korime = konf.dajPostavku("korimeSVN");
        lozinkaC = konf.dajPostavku("lozinkaSVN");
    }
}
