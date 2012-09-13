
package com.rapidminer.deployment.client.wsimport;

import javax.xml.ws.WebFault;


/**
 * This class was generated by the JAX-WS RI.
 * JAX-WS RI 2.1.6 in JDK 6
 * Generated source version: 2.1
 * 
 */
@WebFault(name = "UpdateServiceException", targetNamespace = "http://ws.update.deployment.rapid_i.com/")
public class UpdateServiceException_Exception
    extends Exception
{

    /**
     * Java type that goes as soapenv:Fault detail element.
     * 
     */
    private UpdateServiceException faultInfo;

    /**
     * 
     * @param message
     * @param faultInfo
     */
    public UpdateServiceException_Exception(String message, UpdateServiceException faultInfo) {
        super(message);
        this.faultInfo = faultInfo;
    }

    /**
     * 
     * @param message
     * @param faultInfo
     * @param cause
     */
    public UpdateServiceException_Exception(String message, UpdateServiceException faultInfo, Throwable cause) {
        super(message, cause);
        this.faultInfo = faultInfo;
    }

    /**
     * 
     * @return
     *     returns fault bean: com.rapidminer.deployment.client.wsimport.UpdateServiceException
     */
    public UpdateServiceException getFaultInfo() {
        return faultInfo;
    }

}
