/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.foi.nwtis.antpofuk.podaci;

import java.io.Serializable;
import java.util.Date;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author Antonija Pofuk
 */
//@Entity
//@Table(name = "MQTT_PORUKE")
//@XmlRootElement
//@NamedQueries({
//    @NamedQuery(name = "MqttPoruke.findAll", query = "SELECT m FROM MqttPoruke m")
//    , @NamedQuery(name = "MqttPoruke.findById", query = "SELECT m FROM MqttPoruke m WHERE m.id = :id")
//    , @NamedQuery(name = "MqttPoruke.findBySadrzaj", query = "SELECT m FROM MqttPoruke m WHERE m.sadrzaj = :sadrzaj")
//    , @NamedQuery(name = "MqttPoruke.findByVrijeme", query = "SELECT m FROM MqttPoruke m WHERE m.vrijeme = :vrijeme")})
//public class MqttPoruke1 implements Serializable {
//
//    private static final long serialVersionUID = 1L;
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    @Basic(optional = false)
//    @Column(name = "ID")
//    private Integer id;
//    @Basic(optional = false)
//    @NotNull
//    @Size(min = 1, max = 255)
//    @Column(name = "SADRZAJ")
//    private String sadrzaj;
//    @Column(name = "VRIJEME")
//    @Temporal(TemporalType.TIMESTAMP)
//    private Date vrijeme;

//    public MqttPoruke1() {
//    }
//
//    public MqttPoruke1(Integer id) {
//        this.id = id;
//    }
//
//    public MqttPoruke1(Integer id, String sadrzaj) {
//        this.id = id;
//        this.sadrzaj = sadrzaj;
//    }
//
//    public Integer getId() {
//        return id;
//    }
//
//    public void setId(Integer id) {
//        this.id = id;
//    }
//
//    public String getSadrzaj() {
//        return sadrzaj;
//    }
//
//    public void setSadrzaj(String sadrzaj) {
//        this.sadrzaj = sadrzaj;
//    }
//
//    public Date getVrijeme() {
//        return vrijeme;
//    }
//
//    public void setVrijeme(Date vrijeme) {
//        this.vrijeme = vrijeme;
//    }
//
//    @Override
//    public int hashCode() {
//        int hash = 0;
//        hash += (id != null ? id.hashCode() : 0);
//        return hash;
//    }
//
//    @Override
//    public boolean equals(Object object) {
//         TODO: Warning - this method won't work in the case the id fields are not set
//        if (!(object instanceof MqttPoruke1)) {
//            return false;
//        }
//        MqttPoruke1 other = (MqttPoruke1) object;
//        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
//            return false;
//        }
//        return true;
////    }
//
//    @Override
//    public String toString() {
//        return "org.foi.nwtis.antpofuk.podaci.MqttPoruke[ id=" + id + " ]";
//    }
    
//}
