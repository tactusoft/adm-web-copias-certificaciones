package co.gov.sic.copiasycertificaciones.entities;

import java.io.Serializable;
import java.sql.Date;
import java.time.LocalDate;

public class PersonaActo implements Serializable {

    private static final long serialVersionUID = 1L;
    
	private long idenPers;
    private String nombrePersona;
    private String tipoDocumento;
    private long numeroDocumento;
    private LocalDate fechaNotificacion;
    private LocalDate fechaEjecutoria;
    private String constanciaEjecutoria;
    private LocalDate fechaConstancia;
    private int anioRadicado;
    private long numeroRadicado;

    public long getIdenPers() {
        return idenPers;
    }

    public void setIdenPers(long val) {
        idenPers = val;
    }

    public String getTipoDocumento() {
        return tipoDocumento;
    }

    public void setTipoDocumento(String val) {
        tipoDocumento = val;
    }

    public String getNombrePersona() {
        return nombrePersona;
    }

    public void setNombrePersona(String val) {
        nombrePersona = val;
    }

    public long getNumeroDocumento() {
        return numeroDocumento;
    }

    public void setNumeroDocumento(long val) {
        numeroDocumento = val;
    }

    public LocalDate getFechaNotificacion() {
        return fechaNotificacion;
    }

    public void setFechaNotificacion(LocalDate val) {
        fechaNotificacion = val;
    }

    public void setFechaNotificacion(Date val) {
        if (val != null) {
            fechaNotificacion = val.toLocalDate();
        }
    }

    public LocalDate getFechaEjecutoria() {
        return fechaEjecutoria;
    }

    public void setFechaEjecutoria(LocalDate val) {
        fechaEjecutoria = val;
    }

    public void setFechaEjecutoria(Date val) {
        if (val != null) {
            fechaEjecutoria = val.toLocalDate();
        }
    }

    public String getConstanciaEjecutoria() {
        return constanciaEjecutoria;
    }

    public void setConstanciaEjecutoria(String val) {
        constanciaEjecutoria = val;
    }

    public LocalDate getFechaConstancia() {
        return fechaConstancia;
    }

    public void setFechaConstancia(LocalDate val) {
        fechaConstancia = val;
    }

    public void setFechaConstancia(Date val) {
        if (val != null) {
            fechaConstancia = val.toLocalDate();
        }
    }

    public int getAnioRadicado() {
        return anioRadicado;
    }

    public void setAnioRadicado(int val) {
        anioRadicado = val;
    }

    public long getNumeroRadicado() {
        return numeroRadicado;
    }

    public void setNumeroRadicado(long val) {
        numeroRadicado = val;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 59 * hash + (int) this.idenPers;
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final PersonaActo other = (PersonaActo) obj;
        return this.idenPers == other.getIdenPers();
    }
}
