package co.gov.sic.copiasycertificaciones.rest;

import java.time.LocalDate;

public class PagoRecaudo {

    protected String tipoPago;

    protected String codigoBanco;

    protected String codigoSucursal;

    protected String numeroCuenta;

    protected long numeroPago;

    protected LocalDate fechaPago;

    protected double valorPago;

    public String getTipoPago() {
        return tipoPago;
    }

    public void setTipoPago(String value) {
        this.tipoPago = value;
    }

    public String getCodigoBanco() {
        return codigoBanco;
    }

    public void setCodigoBanco(String value) {
        this.codigoBanco = value;
    }

    public String getCodigoSucursal() {
        return codigoSucursal;
    }

    public void setCodigoSucursal(String value) {
        this.codigoSucursal = value;
    }

    public String getNumeroCuenta() {
        return numeroCuenta;
    }

    public void setNumeroCuenta(String value) {
        this.numeroCuenta = value;
    }

    public long getNumeroPago() {
        return numeroPago;
    }

    public void setNumeroPago(long value) {
        this.numeroPago = value;
    }

    public LocalDate getFechaPago() {
        return fechaPago;
    }

    public void setFechaPago(LocalDate value) {
        this.fechaPago = value;
    }

    public double getValorPago() {
        return valorPago;
    }

    public void setValorPago(double value) {
        this.valorPago = value;
    }
}
