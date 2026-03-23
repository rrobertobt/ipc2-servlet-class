package edu.rb.ejemploservlets.utils;

public class DBException extends RuntimeException {
    public DBException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
