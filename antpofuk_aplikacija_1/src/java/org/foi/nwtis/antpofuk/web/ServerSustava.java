package org.foi.nwtis.antpofuk.web;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import static java.lang.System.out;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletContext;
import org.foi.nwtis.antpofuk.konfiguracije.Konfiguracija;
import org.foi.nwtis.antpofuk.web.slusaci.SlusacAplikacije;

/**
 * Klasa koja pokreće serverski dio
 *
 * @author Antonija Pofuk
 */
public class ServerSustava extends Thread {

    String server;
    int port;
    Konfiguracija konf = null;
    public static ServerSocket serverSocket = null;
    Socket socket = null;
    public static boolean serverPauza = false;
    ServletContext sc;
    OutputStream out;

    @Override
    public void run() {
        ucitajKonfiguraciju();
        if (provjeraPort()) {
            try {
                while (serverPauza == false) {
                    socket = serverSocket.accept();
                    System.out.println("Dretva radi....Pokrece se radna...");
                    RadnaDretva radnaDretva = new RadnaDretva(socket, new Date());
                    radnaDretva.start();
                }
            } catch (IOException ex) {
                Logger.getLogger(ServerSustava.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    @Override
    public synchronized void start() {
        super.start();
    }

    @Override
    public void interrupt() {
        if (serverSocket != null && !serverSocket.isClosed()) {
            try {
                serverSocket.close();
            } catch (IOException ex) {
                Logger.getLogger(ServerSustava.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        super.interrupt();
    }

    /**
     * Metoda koja učitava konfiguracijske podatke
     */
    public void ucitajKonfiguraciju() throws NumberFormatException {
        sc = (ServletContext) SlusacAplikacije.getServletContext();
        konf = (Konfiguracija) sc.getAttribute("Konfiguracija");
        port = Integer.parseInt(konf.dajPostavku("port"));
        System.out.println("port je:" + port);
    }

    /**
     * Metoda koja provjera je li port slobodan
     *
     * @return uspješnost provjere porta
     */
    private boolean provjeraPort() {
        ucitajKonfiguraciju();
        try {
            serverSocket = new ServerSocket(port);
            System.out.println("Osluškujem na localhostu:" + port);
        } catch (IOException ex) {
            System.out.println("Port " + port + " je zauzet!");
            return false;
        }
        return true;
    }
}
