
package com.rapid_i.repository.wsimport;

import java.util.List;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.ws.RequestWrapper;
import javax.xml.ws.ResponseWrapper;


/**
 * This class was generated by the JAX-WS RI.
 * JAX-WS RI 2.1.6 in JDK 6
 * Generated source version: 2.1
 * 
 */
@WebService(name = "ProcessService_1_3", targetNamespace = "http://service.web.rapidanalytics.de/")
@XmlSeeAlso({
    ObjectFactory.class
})
public interface ProcessService13 {


    /**
     * 
     * @param scheduledProcessId
     * @return
     *     returns com.rapid_i.repository.wsimport.Response
     */
    @WebMethod
    @WebResult(targetNamespace = "")
    @RequestWrapper(localName = "stopProcess", targetNamespace = "http://service.web.rapidanalytics.de/", className = "com.rapid_i.repository.wsimport.StopProcess")
    @ResponseWrapper(localName = "stopProcessResponse", targetNamespace = "http://service.web.rapidanalytics.de/", className = "com.rapid_i.repository.wsimport.StopProcessResponse")
    public Response stopProcess(
        @WebParam(name = "scheduledProcessId", targetNamespace = "")
        int scheduledProcessId);

    /**
     * 
     * @param processLocation
     * @param executionTime
     * @param processContext
     * @param queueName
     * @return
     *     returns com.rapid_i.repository.wsimport.ExecutionResponse
     */
    @WebMethod(operationName = "executeProcessSimple_1_3")
    @WebResult(targetNamespace = "")
    @RequestWrapper(localName = "executeProcessSimple_1_3", targetNamespace = "http://service.web.rapidanalytics.de/", className = "com.rapid_i.repository.wsimport.ExecuteProcessSimple13")
    @ResponseWrapper(localName = "executeProcessSimple_1_3Response", targetNamespace = "http://service.web.rapidanalytics.de/", className = "com.rapid_i.repository.wsimport.ExecuteProcessSimple13Response")
    public ExecutionResponse executeProcessSimple13(
        @WebParam(name = "processLocation", targetNamespace = "")
        String processLocation,
        @WebParam(name = "executionTime", targetNamespace = "")
        XMLGregorianCalendar executionTime,
        @WebParam(name = "processContext", targetNamespace = "")
        ProcessContextWrapper processContext,
        @WebParam(name = "queueName", targetNamespace = "")
        String queueName);

    /**
     * 
     * @param jobId
     * @return
     *     returns java.util.List<java.lang.Integer>
     */
    @WebMethod
    @WebResult(targetNamespace = "")
    @RequestWrapper(localName = "getProcessIdsForJobId", targetNamespace = "http://service.web.rapidanalytics.de/", className = "com.rapid_i.repository.wsimport.GetProcessIdsForJobId")
    @ResponseWrapper(localName = "getProcessIdsForJobIdResponse", targetNamespace = "http://service.web.rapidanalytics.de/", className = "com.rapid_i.repository.wsimport.GetProcessIdsForJobIdResponse")
    public List<Integer> getProcessIdsForJobId(
        @WebParam(name = "jobId", targetNamespace = "")
        int jobId);

    /**
     * 
     * @param queueName
     * @return
     *     returns com.rapid_i.repository.wsimport.QueueState
     */
    @WebMethod
    @WebResult(targetNamespace = "")
    @RequestWrapper(localName = "getQueueState", targetNamespace = "http://service.web.rapidanalytics.de/", className = "com.rapid_i.repository.wsimport.GetQueueState")
    @ResponseWrapper(localName = "getQueueStateResponse", targetNamespace = "http://service.web.rapidanalytics.de/", className = "com.rapid_i.repository.wsimport.GetQueueStateResponse")
    public QueueState getQueueState(
        @WebParam(name = "queueName", targetNamespace = "")
        String queueName);

    /**
     * 
     * @param queueName
     * @return
     *     returns java.util.List<com.rapid_i.repository.wsimport.QueueProperty>
     */
    @WebMethod
    @WebResult(targetNamespace = "")
    @RequestWrapper(localName = "getQueueInfo", targetNamespace = "http://service.web.rapidanalytics.de/", className = "com.rapid_i.repository.wsimport.GetQueueInfo")
    @ResponseWrapper(localName = "getQueueInfoResponse", targetNamespace = "http://service.web.rapidanalytics.de/", className = "com.rapid_i.repository.wsimport.GetQueueInfoResponse")
    public List<QueueProperty> getQueueInfo(
        @WebParam(name = "queueName", targetNamespace = "")
        String queueName);

    /**
     * 
     * @return
     *     returns java.util.List<java.lang.String>
     */
    @WebMethod
    @WebResult(targetNamespace = "")
    @RequestWrapper(localName = "getQueueNames", targetNamespace = "http://service.web.rapidanalytics.de/", className = "com.rapid_i.repository.wsimport.GetQueueNames")
    @ResponseWrapper(localName = "getQueueNamesResponse", targetNamespace = "http://service.web.rapidanalytics.de/", className = "com.rapid_i.repository.wsimport.GetQueueNamesResponse")
    public List<String> getQueueNames();

    /**
     * 
     * @param scheduledProcessId
     * @return
     *     returns com.rapid_i.repository.wsimport.ProcessResponse
     */
    @WebMethod
    @WebResult(targetNamespace = "")
    @RequestWrapper(localName = "getRunningProcessesInfo", targetNamespace = "http://service.web.rapidanalytics.de/", className = "com.rapid_i.repository.wsimport.GetRunningProcessesInfo")
    @ResponseWrapper(localName = "getRunningProcessesInfoResponse", targetNamespace = "http://service.web.rapidanalytics.de/", className = "com.rapid_i.repository.wsimport.GetRunningProcessesInfoResponse")
    public ProcessResponse getRunningProcessesInfo(
        @WebParam(name = "scheduledProcessId", targetNamespace = "")
        int scheduledProcessId);

    /**
     * 
     * @param processLocation
     * @param start
     * @param processContext
     * @param queueName
     * @param cronExpression
     * @param end
     * @return
     *     returns com.rapid_i.repository.wsimport.ExecutionResponse
     */
    @WebMethod(operationName = "executeProcessCron_1_3")
    @WebResult(targetNamespace = "")
    @RequestWrapper(localName = "executeProcessCron_1_3", targetNamespace = "http://service.web.rapidanalytics.de/", className = "com.rapid_i.repository.wsimport.ExecuteProcessCron13")
    @ResponseWrapper(localName = "executeProcessCron_1_3Response", targetNamespace = "http://service.web.rapidanalytics.de/", className = "com.rapid_i.repository.wsimport.ExecuteProcessCron13Response")
    public ExecutionResponse executeProcessCron13(
        @WebParam(name = "processLocation", targetNamespace = "")
        String processLocation,
        @WebParam(name = "cronExpression", targetNamespace = "")
        String cronExpression,
        @WebParam(name = "start", targetNamespace = "")
        XMLGregorianCalendar start,
        @WebParam(name = "end", targetNamespace = "")
        XMLGregorianCalendar end,
        @WebParam(name = "processContext", targetNamespace = "")
        ProcessContextWrapper processContext,
        @WebParam(name = "queueName", targetNamespace = "")
        String queueName);

    /**
     * 
     * @param since
     * @return
     *     returns java.util.List<java.lang.Integer>
     */
    @WebMethod
    @WebResult(targetNamespace = "")
    @RequestWrapper(localName = "getRunningProcesses", targetNamespace = "http://service.web.rapidanalytics.de/", className = "com.rapid_i.repository.wsimport.GetRunningProcesses")
    @ResponseWrapper(localName = "getRunningProcessesResponse", targetNamespace = "http://service.web.rapidanalytics.de/", className = "com.rapid_i.repository.wsimport.GetRunningProcessesResponse")
    public List<Integer> getRunningProcesses(
        @WebParam(name = "since", targetNamespace = "")
        XMLGregorianCalendar since);

    /**
     * 
     * @param triggerName
     * @return
     *     returns com.rapid_i.repository.wsimport.Response
     */
    @WebMethod
    @WebResult(targetNamespace = "")
    @RequestWrapper(localName = "cancelTrigger", targetNamespace = "http://service.web.rapidanalytics.de/", className = "com.rapid_i.repository.wsimport.CancelTrigger")
    @ResponseWrapper(localName = "cancelTriggerResponse", targetNamespace = "http://service.web.rapidanalytics.de/", className = "com.rapid_i.repository.wsimport.CancelTriggerResponse")
    public Response cancelTrigger(
        @WebParam(name = "triggerName", targetNamespace = "")
        String triggerName);

}
