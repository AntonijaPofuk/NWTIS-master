/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.foi.nwtis.antpofuk.ws.klijenti;

import org.foi.nwtis.antpofuk.ws.StatusKorisnika;

/**
 *
 * @author antonija
 */
public class AerodromClient {

    public static Boolean autenticirajGrupu(java.lang.String korisnickoIme, java.lang.String korisnickaLozinka) {
        org.foi.nwtis.antpofuk.ws.AerodromiWS_Service service = new org.foi.nwtis.antpofuk.ws.AerodromiWS_Service();
        org.foi.nwtis.antpofuk.ws.AerodromiWS port = service.getAerodromiWSPort();
        return port.autenticirajGrupu(korisnickoIme, korisnickaLozinka);
    }

    public static Boolean registrirajGrupu(java.lang.String korisnickoIme, java.lang.String korisnickaLozinka) {
        org.foi.nwtis.antpofuk.ws.AerodromiWS_Service service = new org.foi.nwtis.antpofuk.ws.AerodromiWS_Service();
        org.foi.nwtis.antpofuk.ws.AerodromiWS port = service.getAerodromiWSPort();
        return port.registrirajGrupu(korisnickoIme, korisnickaLozinka);
    }

    public static Boolean deregistrirajGrupu(java.lang.String korisnickoIme, java.lang.String korisnickaLozinka) {
        org.foi.nwtis.antpofuk.ws.AerodromiWS_Service service = new org.foi.nwtis.antpofuk.ws.AerodromiWS_Service();
        org.foi.nwtis.antpofuk.ws.AerodromiWS port = service.getAerodromiWSPort();
        return port.deregistrirajGrupu(korisnickoIme, korisnickaLozinka);
    }

    public static Boolean aktivirajGrupu(java.lang.String korisnickoIme, java.lang.String korisnickaLozinka) {
        org.foi.nwtis.antpofuk.ws.AerodromiWS_Service service = new org.foi.nwtis.antpofuk.ws.AerodromiWS_Service();
        org.foi.nwtis.antpofuk.ws.AerodromiWS port = service.getAerodromiWSPort();
        return port.aktivirajGrupu(korisnickoIme, korisnickaLozinka);
    }

    public static Boolean blokirajGrupu(java.lang.String korisnickoIme, java.lang.String korisnickaLozinka) {
        org.foi.nwtis.antpofuk.ws.AerodromiWS_Service service = new org.foi.nwtis.antpofuk.ws.AerodromiWS_Service();
        org.foi.nwtis.antpofuk.ws.AerodromiWS port = service.getAerodromiWSPort();
        return port.blokirajGrupu(korisnickoIme, korisnickaLozinka);
    }

    public static StatusKorisnika dajStatusGrupe(java.lang.String korisnickoIme, java.lang.String korisnickaLozinka) {
        org.foi.nwtis.antpofuk.ws.AerodromiWS_Service service = new org.foi.nwtis.antpofuk.ws.AerodromiWS_Service();
        org.foi.nwtis.antpofuk.ws.AerodromiWS port = service.getAerodromiWSPort();
        return port.dajStatusGrupe(korisnickoIme, korisnickaLozinka);
    }
    
    
    
}
