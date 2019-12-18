package org.foi.nwtis.antpofuk.web.slusaci;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import org.foi.nwtis.antpofuk.konfiguracije.Konfiguracija;
import org.foi.nwtis.antpofuk.konfiguracije.KonfiguracijaApstraktna;
import org.foi.nwtis.antpofuk.konfiguracije.NeispravnaKonfiguracija;
import org.foi.nwtis.antpofuk.konfiguracije.NemaKonfiguracije;
import org.foi.nwtis.antpofuk.konfiguracije.bp.BP_Konfiguracija;
import org.foi.nwtis.antpofuk.web.PreuzimanjeAviona;
import org.foi.nwtis.antpofuk.web.ServerSustava;

/**
 * Klasa koja djeluje kao slusac konteksta servleta
 *
 * @author Antonija Pofuk
 */
@WebListener
public class SlusacAplikacije implements ServletContextListener {

    private static ServletContext sc;
    PreuzimanjeAviona preuzimanjeAviona;
    ServerSustava server;

    public static ServletContext getServletContext() {
        return sc;
    }

    /**
     * Metoda koja služi za inicijalizaciju konteksta servleta
     * 
     * @param sce označava kontekst servleta
     */
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        sc = sce.getServletContext();
        String datoteka = sc.getInitParameter("konfiguracija");
        String putanja = sc.getRealPath("/WEB-INF") + java.io.File.separator;
        String puniNazivDatoteke = putanja + datoteka;
        try {
            BP_Konfiguracija bpk = new BP_Konfiguracija(puniNazivDatoteke);
            sc.setAttribute("BP_Konfig", bpk);
            Konfiguracija konf = KonfiguracijaApstraktna.preuzmiKonfiguraciju(puniNazivDatoteke);
            sc.setAttribute("Konfiguracija", konf);
            System.out.println("Učitana glavna konfiguracija");
        } catch (NemaKonfiguracije | NeispravnaKonfiguracija ex) {
            Logger.getLogger(SlusacAplikacije.class.getName()).log(Level.SEVERE, null, ex);
             System.out.println(ex + "Provjerite podatke konfiguracijske datoteke");
        }
        
   
    server = new ServerSustava();
    server.start();
    preuzimanjeAviona = new PreuzimanjeAviona();       
    preuzimanjeAviona.start();
       
    }

      
    /**
     * Metoda koja služi za uništavanje konteksta servleta
     * 
     * @param sce označava kontekst servleta
     */
    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        if (preuzimanjeAviona != null) {
            preuzimanjeAviona.interrupt();
        }
        
          if(server != null){
            server.interrupt();
        }
        sc = sce.getServletContext();
        sc.removeAttribute("BP_Konfig");
    }
}
