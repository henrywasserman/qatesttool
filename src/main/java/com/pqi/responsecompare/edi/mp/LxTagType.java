//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.11 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2018.08.13 at 05:01:13 PM EDT 
//


package com.pqi.responsecompare.edi.mp;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for lx-tagType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="lx-tagType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="LX" type="{}LXType"/&gt;
 *         &lt;element name="N1" type="{}N1Type"/&gt;
 *         &lt;element name="REF" type="{}REFType"/&gt;
 *         &lt;element name="DTP" type="{}DTPType"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "lx-tagType", propOrder = {
    "lx",
    "n1",
    "ref",
    "dtp"
})
public class LxTagType {

    @XmlElement(name = "LX", required = true)
    protected LXType lx;
    @XmlElement(name = "N1", required = true)
    protected N1Type n1;
    @XmlElement(name = "REF", required = true)
    protected REFType ref;
    @XmlElement(name = "DTP", required = true)
    protected DTPType dtp;

    /**
     * Gets the value of the lx property.
     * 
     * @return
     *     possible object is
     *     {@link LXType }
     *     
     */
    public LXType getLX() {
        return lx;
    }

    /**
     * Sets the value of the lx property.
     * 
     * @param value
     *     allowed object is
     *     {@link LXType }
     *     
     */
    public void setLX(LXType value) {
        this.lx = value;
    }

    /**
     * Gets the value of the n1 property.
     * 
     * @return
     *     possible object is
     *     {@link N1Type }
     *     
     */
    public N1Type getN1() {
        return n1;
    }

    /**
     * Sets the value of the n1 property.
     * 
     * @param value
     *     allowed object is
     *     {@link N1Type }
     *     
     */
    public void setN1(N1Type value) {
        this.n1 = value;
    }

    /**
     * Gets the value of the ref property.
     * 
     * @return
     *     possible object is
     *     {@link REFType }
     *     
     */
    public REFType getREF() {
        return ref;
    }

    /**
     * Sets the value of the ref property.
     * 
     * @param value
     *     allowed object is
     *     {@link REFType }
     *     
     */
    public void setREF(REFType value) {
        this.ref = value;
    }

    /**
     * Gets the value of the dtp property.
     * 
     * @return
     *     possible object is
     *     {@link DTPType }
     *     
     */
    public DTPType getDTP() {
        return dtp;
    }

    /**
     * Sets the value of the dtp property.
     * 
     * @param value
     *     allowed object is
     *     {@link DTPType }
     *     
     */
    public void setDTP(DTPType value) {
        this.dtp = value;
    }

}
