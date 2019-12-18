/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.foi.nwtis.antpofuk.soap.klijent;


/**
 *
 * @author Antonija Pofuk
 */
public class AerodromiClient {

    public static MeteoPodaci dajVazeceMeteoPodatke(java.lang.String icao, java.lang.String korisnickoIme, java.lang.String loz) {
        org.foi.nwtis.antpofuk.soap.klijent.AIRP2WS_Service service = new org.foi.nwtis.antpofuk.soap.klijent.AIRP2WS_Service();
        org.foi.nwtis.antpofuk.soap.klijent.AIRP2WS port = service.getAIRP2WSPort();
        return port.dajVazeceMeteoPodatke(icao, korisnickoIme, loz);
    }

    public static Korisnik dajKorisnika(java.lang.String korime, java.lang.String loz, java.lang.String id) {
        org.foi.nwtis.antpofuk.soap.klijent.AIRP2WS_Service service = new org.foi.nwtis.antpofuk.soap.klijent.AIRP2WS_Service();
        org.foi.nwtis.antpofuk.soap.klijent.AIRP2WS port = service.getAIRP2WSPort();
        return port.dajKorisnika(korime, loz, id);
    }

    
}
