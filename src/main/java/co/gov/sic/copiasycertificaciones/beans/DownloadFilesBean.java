/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package co.gov.sic.copiasycertificaciones.beans;

import static co.gov.sic.copiasycertificaciones.beans.BeanBase.encodeURL;
import static co.gov.sic.copiasycertificaciones.beans.BeanBase.getExternalContext;
import static co.gov.sic.copiasycertificaciones.beans.BeanBase.getMimeType;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.primefaces.model.StreamedContent;

import co.gov.sic.copiasycertificaciones.entities.Attachment;
import co.gov.sic.copiasycertificaciones.entities.Cesl_tramite;
import co.gov.sic.copiasycertificaciones.util.Constantes;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;
import jakarta.servlet.http.HttpServletResponse;

/**
 *
 * @author elmos
 */
@Named("downloadFilesBean")
@SessionScoped
public class DownloadFilesBean implements Serializable {

	private static final long serialVersionUID = 2348065440113364161L;
	private Cesl_tramite tramite;
	protected final Logger logger = LoggerFactory.getLogger(DownloadFilesBean.class);
	private List<Attachment> listaAdjuntos;
	private StreamedContent download;

	public DownloadFilesBean() {
		try {
			listaAdjuntos = new ArrayList<>();
		} catch (Exception e) {
			logger.error(e.toString());
		}
	}

	/**
	 * @return the tramite
	 */
	public Cesl_tramite getTramite() {
		return tramite;
	}

	/**
	 * @param tramite the tramite to set
	 */
	public void setTramite(Cesl_tramite tramite) {
		this.tramite = tramite;

	}

	private String getRuta() {
		return String.format("%s%02d/%02d-%06d/", Constantes.PATH_ARCHIVOS_OTROS_FORMULARIOS, tramite.getAno_radi(),
				tramite.getAno_radi(), tramite.getNume_radi());

	}

	public void cfgTramite(Cesl_tramite tramite) {
		setTramite(tramite);
		listaAdjuntos = new ArrayList<>();
		try {
			File directory = new File(getRuta());
			for (String fileName : directory.list()) {
				Attachment a = new Attachment(fileName,
						fileName.substring(fileName.indexOf(".") + 1, fileName.length()), 0,
						getRuta() + "/" + fileName);
				listaAdjuntos.add(a);
			}
		} catch (Exception e) {
			logger.error(e.toString());
		}
	}

	private ByteArrayOutputStream getFileContent(String fullFileName) throws FileNotFoundException, IOException {
	    byte[] buffer = new byte[4096];
	    ByteArrayOutputStream baos = new ByteArrayOutputStream();
	    
	    try (FileInputStream fis = new FileInputStream(fullFileName);
	         BufferedInputStream bis = new BufferedInputStream(fis)) {
	        
	        int bytes;
	        while ((bytes = bis.read(buffer, 0, buffer.length)) > 0) {
	            baos.write(buffer, 0, bytes);
	        }
	    }
	    
	    return baos;
	}

	public void descargarAdjunto(String pathToFind, String fileName) {
		try {
			logger.info("Descargando adjunto");
			if (pathToFind.isEmpty()) {
				logger.info("No se ha especificado una ruta valida");
				return;
			}
			ByteArrayOutputStream fileInMemory = getFileContent(pathToFind);
			downloadFile(fileInMemory, fileName, true);
		} catch (IOException ex) {
			logger.error("File Not found");
			logger.error(ex.getLocalizedMessage());
		}
	}

	public void downloadFile(ByteArrayOutputStream fileContent, String fileName, boolean attachment)
			throws IOException {
		ExternalContext externalContext = getExternalContext();
		HttpServletResponse response = (HttpServletResponse) externalContext.getResponse();
		int fileSize = fileContent.size();
		response.setHeader("Expires", "0");
		response.setHeader("Cache-Control", "must-revalidate, post-check=0, pre-check=0");
		response.setHeader("Pragma", "public");
		externalContext.setResponseHeader("Content-Disposition",
				String.format(Constantes.SENDFILE_HEADER, (attachment ? "attachment" : "inline"), encodeURL(fileName)));
		response.setHeader("Content-Length", String.valueOf(fileSize));
		response.setContentType(getMimeType(fileName));
		response.setBufferSize(Constantes.DEFAULT_SENDFILE_BUFFER_SIZE);
		response.setContentLength(fileSize);
		try (OutputStream os = response.getOutputStream()) {
			fileContent.writeTo(os);
		}
		FacesContext.getCurrentInstance().responseComplete();
	}

	/**
	 * @return the listaAdjuntos
	 */
	public List<Attachment> getListaAdjuntos() {
		return listaAdjuntos;
	}

	/**
	 * @param listaAdjuntos the listaAdjuntos to set
	 */
	public void setListaAdjuntos(List<Attachment> listaAdjuntos) {
		this.listaAdjuntos = listaAdjuntos;
	}

	/**
	 * @return the download
	 */
	public StreamedContent getDownload() {
		return download;
	}

	/**
	 * @param download the download to set
	 */
	public void setDownload(StreamedContent download) {
		this.download = download;
	}

	protected void AddErrorMessage(String error) {
		AddErrorMessage(error, null);
	}

	protected void AddStatusMessage(String error, String clientID) {
		if (FacesContext.getCurrentInstance() != null) {
			FacesContext.getCurrentInstance().addMessage(clientID,
					new FacesMessage(FacesMessage.SEVERITY_INFO, error, error));
		}
	}

	protected void AddErrorMessage(String error, String clientID) {
		if (FacesContext.getCurrentInstance() != null) {
			FacesContext.getCurrentInstance().addMessage(clientID,
					new FacesMessage(FacesMessage.SEVERITY_ERROR, error, error));
		}
	}

	protected void AddWarningMessage(String message) {
		AddWarningMessage(message, null);
	}

	protected void AddWarningMessage(String message, String clientID) {
		if (FacesContext.getCurrentInstance() != null) {
			FacesContext.getCurrentInstance().addMessage(clientID,
					new FacesMessage(FacesMessage.SEVERITY_WARN, message, message));
		}
	}

	protected void AddInfoMessage(String message) {
		AddInfoMessage(message, null);
	}

	protected void AddInfoMessage(String message, String clientId) {
		if (FacesContext.getCurrentInstance() != null) {
			FacesContext.getCurrentInstance().addMessage(clientId,
					new FacesMessage(FacesMessage.SEVERITY_INFO, message, message));
		}
	}

}
