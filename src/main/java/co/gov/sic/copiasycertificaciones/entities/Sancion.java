package co.gov.sic.copiasycertificaciones.entities;

import co.gov.sic.copiasycertificaciones.enums.TipoSancion;
import java.sql.Date;
import java.time.LocalDate;
import java.util.Objects;

public class Sancion implements Comparable<Sancion> {

    private TipoSancion tipo;
    private Short anoRadicacion;
    private Integer numeroRadicacion;
    private String tipoActo;
    private Integer numeroActo;
    private LocalDate fechaActo;
    private boolean firmeza;

    public TipoSancion getTipo() {
        return tipo;
    }

    public void setTipo(TipoSancion val) {
        tipo = val;
    }

    public String getTipoActo() {
        return tipoActo;
    }

    public void setTipoActo(String val) {
        tipoActo = val;
    }

    public Integer getNumeroActo() {
        return numeroActo;
    }

    public void setNumeroActo(Integer val) {
        numeroActo = val;
    }

    public LocalDate getFechaActo() {
        return fechaActo;
    }

    public void setFechaActo(LocalDate val) {
        fechaActo = val;
    }

    public void setFechaActo(Date val) {
        if (val != null) {
            fechaActo = val.toLocalDate();
        }
    }

    public short getAnoRadicacion() {
        return anoRadicacion;
    }

    public void setAnoRadicacion(short val) {
        anoRadicacion = val;
    }

    public Integer getNumeroRadicacion() {
        return numeroRadicacion;
    }

    public void setNumeroRadicacion(Integer val) {
        numeroRadicacion = val;
    }

    public boolean getFirmeza() {
        return firmeza;
    }

    public void setFirmeza(boolean val) {
        firmeza = val;
    }

    @Override
    public boolean equals(Object o) {

        if (o == this) {
            return true;
        }
        if (!(o instanceof Sancion)) {
            return false;
        }
        Sancion u = (Sancion) o;
        return Objects.equals(this.getTipo(), u.getTipo()) && Objects.equals(this.getAnoRadicacion(), u.getAnoRadicacion())
                && Objects.equals(this.getNumeroRadicacion(), u.getNumeroRadicacion());
    }

    @Override
    public int hashCode() {
        return Objects.hash(tipo, this.anoRadicacion, this.numeroRadicacion);
    }

    @Override
    public int compareTo(Sancion s) {
        int c = 0;
        if (this.getTipo() != null) {
            c = this.getTipo().compareTo(s.getTipo());
        }
        if (c == 0) {
            Integer i = (int) this.getAnoRadicacion();
            c = i.compareTo((int) s.getAnoRadicacion());

            if (c == 0) {
                c = this.getNumeroRadicacion().compareTo(s.getNumeroRadicacion());
            }
        }

        return c;
    }
}
