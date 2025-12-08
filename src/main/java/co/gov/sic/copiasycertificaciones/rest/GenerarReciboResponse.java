package co.gov.sic.copiasycertificaciones.rest;

import java.util.List;

public class GenerarReciboResponse {

	private String nombreArchivo;
    private String extencion;
    private String datos;
    private List<String> rutas;

    public GenerarReciboResponse() {
    }

    public String getNombreArchivo() {
        return nombreArchivo;
    }

    public void setNombreArchivo(String nombreArchivo) {
        this.nombreArchivo = nombreArchivo;
    }

    public String getExtencion() {
        return extencion;
    }

    public void setExtencion(String extencion) {
        this.extencion = extencion;
    }

    public String getDatos() {
        return datos;
    }

    public void setDatos(String datos) {
        this.datos = datos;
    }

    public List<String> getRutas() {
        return rutas;
    }

    public void setRutas(List<String> rutas) {
        this.rutas = rutas;
    }
}
