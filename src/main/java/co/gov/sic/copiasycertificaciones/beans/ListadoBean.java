package co.gov.sic.copiasycertificaciones.beans;

import java.io.ByteArrayOutputStream;
import java.io.Serializable;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import co.gov.sic.copiasycertificaciones.dataaccess.Dal;
import co.gov.sic.copiasycertificaciones.entities.Cesl_tramite;
import co.gov.sic.copiasycertificaciones.enums.TipoAmbienteEnum;
import co.gov.sic.copiasycertificaciones.enums.TipoTramite;
import co.gov.sic.copiasycertificaciones.util.Constantes;
import co.gov.sic.copiasycertificaciones.util.Functions;
import co.gov.sic.copiasycertificaciones.util.Utility;
import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Named;

@Named("listadoBean")
@SessionScoped
public class ListadoBean extends BeanBase implements Serializable {

    private static final long serialVersionUID = 3719402537545279148L;

	public ListadoBean() throws Exception {
        super();
    }



    public List<Cesl_tramite> getListaTramitesUsua() throws Exception {
        try (Dal Dal = new Dal()) {
            return Dal.consultarTramites(getDatosSesion().getPersona().getId());
        }
    }

    public TipoAmbienteEnum getAMBIENTE_ACTIVO() {
        return Constantes.AMBIENTE_ACTIVO;
    }

    public void descargarExpediente(Cesl_tramite tramite) {
        try {
            String folderPath = Functions.getRadicacionFolderPath(tramite.getAno_radi(), tramite.getNume_radi());
            Path folder = Paths.get(folderPath);
            Path folderName = folder.getFileName();
            String fileZipName = String.format("%s.%s", folderName, Constantes.ZIP_EXTENSION);

            //Se arma zip con todos los documentos generados en el expediente
            ByteArrayOutputStream zipMemory = Utility.zipDirectory(folderPath);

            download(zipMemory, fileZipName, true);
        } catch (Exception ex) {
            logger.error("descargarExpediente", ex);
            this.AddErrorMessage("No se pudo descargar el archivo:" + ex.getMessage());
        };
    }

    public boolean validaTipoTramiteCopiaSimple(Cesl_tramite tramite) {
        return tramite.getIdtiposolicitud() == TipoTramite.COPIAS_SIMPLES;
    }
}
