/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.foi.nwtis.antpofuk.rest.klijent;

import javax.ws.rs.ClientErrorException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.WebTarget;

/**
 * Jersey REST client generated for REST resource:AIRP2REST [aerodromi]<br>
 * USAGE:
 * <pre>
 *        RESTAerodromi client = new RESTAerodromi();
 *        Object response = client.XXX(...);
 *        // do whatever with response
 *        client.close();
 * </pre>
 *
 * @author antonija
 */
public class RESTAerodromi {

    private WebTarget webTarget;
    private Client client;
    private static final String BASE_URI = "http://localhost:8084/antpofuk_aplikacija_1/webresources";

    public RESTAerodromi() {
        client = javax.ws.rs.client.ClientBuilder.newClient();
        webTarget = client.target(BASE_URI).path("aerodromi");
    }

    public String putJson(Object requestEntity, String id, String korisnik, String lozinka) throws ClientErrorException {
        return webTarget.path(java.text.MessageFormat.format("{0}/korisnik/{1}/lozinka/{2}", new Object[]{id, korisnik, lozinka})).request(javax.ws.rs.core.MediaType.APPLICATION_JSON).put(javax.ws.rs.client.Entity.entity(requestEntity, javax.ws.rs.core.MediaType.APPLICATION_JSON), String.class);
    }

    public String getJsonIdAvioni(String id, String korisnik, String lozinka) throws ClientErrorException {
        WebTarget resource = webTarget;
        resource = resource.path(java.text.MessageFormat.format("{0}/avioni/korisnik/{1}/lozinka/{2}", new Object[]{id, korisnik, lozinka}));
        return resource.request(javax.ws.rs.core.MediaType.APPLICATION_JSON).get(String.class);
    }

    public String postJson(Object requestEntity, String korisnik, String lozinka) throws ClientErrorException {
        return webTarget.path(java.text.MessageFormat.format("korisnik/{0}/lozinka/{1}", new Object[]{korisnik, lozinka})).request(javax.ws.rs.core.MediaType.APPLICATION_JSON).post(javax.ws.rs.client.Entity.entity(requestEntity, javax.ws.rs.core.MediaType.APPLICATION_JSON), String.class);
    }

    public String deleteJsonIdAvioni(String id, String korisnik, String lozinka) throws ClientErrorException {
        return webTarget.path(java.text.MessageFormat.format("{0}/avion/korisnik/{1}/lozinka/{2}", new Object[]{id, korisnik, lozinka})).request().delete(String.class);
    }

    public String getJsonId(String id, String korisnik, String lozinka) throws ClientErrorException {
        WebTarget resource = webTarget;
        resource = resource.path(java.text.MessageFormat.format("{0}/korisnik/{1}/lozinka/{2}", new Object[]{id, korisnik, lozinka}));
        return resource.request(javax.ws.rs.core.MediaType.APPLICATION_JSON).get(String.class);
    }

    public String deleteJsonAvionSAerodroma(String id, String aid, String korisnik, String lozinka) throws ClientErrorException {
        return webTarget.path(java.text.MessageFormat.format("{0}/avion/{1}/korisnik/{2}/lozinka/{3}", new Object[]{id, aid, korisnik, lozinka})).request().delete(String.class);
    }

    public String deleteJson(String id, String korisnik, String lozinka) throws ClientErrorException {
        return webTarget.path(java.text.MessageFormat.format("{0}/korisnik/{1}/lozinka/{2}", new Object[]{id, korisnik, lozinka})).request().delete(String.class);
    }

    public String getJson(String korisnik, String lozinka) throws ClientErrorException {
        WebTarget resource = webTarget;
        resource = resource.path(java.text.MessageFormat.format("korisnik/{0}/lozinka/{1}", new Object[]{korisnik, lozinka}));
        return resource.request(javax.ws.rs.core.MediaType.APPLICATION_JSON).get(String.class);
    }

    public void close() {
        client.close();
    }
    
}
