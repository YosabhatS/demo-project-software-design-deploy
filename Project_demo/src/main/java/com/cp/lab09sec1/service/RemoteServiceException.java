package com.cp.lab09sec1.service;
 
public class RemoteServiceException extends RuntimeException {

 // Constructor ที่รับข้อความ (message)
 public RemoteServiceException(String message) {
     super(message);
 }

 // Constructor ที่รับข้อความและสาเหตุ (cause)
 public RemoteServiceException(String message, Throwable cause) {
     super(message, cause);
 }
}
