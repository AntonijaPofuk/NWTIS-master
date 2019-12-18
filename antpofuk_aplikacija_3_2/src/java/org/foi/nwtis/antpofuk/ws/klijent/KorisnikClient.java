/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.foi.nwtis.antpofuk.ws.klijent;

import org.foi.nwtis.antpofuk.ws.serveri.Korisnik;


/**
 *
 * @author antonija
 */
public class KorisnikClient {

    public static java.util.List<org.foi.nwtis.antpofuk.ws.serveri.Korisnik> dajSveKorisnike(java.lang.String korime, java.lang.String loz) {
        org.foi.nwtis.antpofuk.ws.serveri.AIRP2WS_Service service = new org.foi.nwtis.antpofuk.ws.serveri.AIRP2WS_Service();
        org.foi.nwtis.antpofuk.ws.serveri.AIRP2WS port = service.getAIRP2WSPort();
        return port.dajSveKorisnike(korime, loz);
    }

    public static Boolean azurirajKorisnika(java.lang.String korimeP, java.lang.String lozP, java.lang.String korime, java.lang.String lozinka, java.lang.String ime, java.lang.String prezime) {
        org.foi.nwtis.antpofuk.ws.serveri.AIRP2WS_Service service = new org.foi.nwtis.antpofuk.ws.serveri.AIRP2WS_Service();
        org.foi.nwtis.antpofuk.ws.serveri.AIRP2WS port = service.getAIRP2WSPort();
        return port.azurirajKorisnika(korimeP, lozP, korime, lozinka, ime, prezime);
    }

    public static Boolean dodajKorisnika(java.lang.String ime, java.lang.String prezime, java.lang.String korime, java.lang.String lozinka) {
        org.foi.nwtis.antpofuk.ws.serveri.AIRP2WS_Service service = new org.foi.nwtis.antpofuk.ws.serveri.AIRP2WS_Service();
        org.foi.nwtis.antpofuk.ws.serveri.AIRP2WS port = service.getAIRP2WSPort();
        return port.dodajKorisnika(ime, prezime, korime, lozinka);
    }

    public static Korisnik dajKorisnika(java.lang.String korime, java.lang.String loz, java.lang.String id) {
        org.foi.nwtis.antpofuk.ws.serveri.AIRP2WS_Service service = new org.foi.nwtis.antpofuk.ws.serveri.AIRP2WS_Service();
        org.foi.nwtis.antpofuk.ws.serveri.AIRP2WS port = service.getAIRP2WSPort();
        return port.dajKorisnika(korime, loz, id);
    }

 

  
    
    
    
}
