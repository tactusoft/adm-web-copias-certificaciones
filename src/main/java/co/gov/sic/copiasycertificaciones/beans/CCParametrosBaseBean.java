/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package co.gov.sic.copiasycertificaciones.beans;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import co.gov.sic.copiasycertificaciones.dataaccess.Dal;
import co.gov.sic.copiasycertificaciones.entities.CamaraComercio;
import co.gov.sic.copiasycertificaciones.entities.Cesl_PersonaCamara;
import co.gov.sic.copiasycertificaciones.entities.Ciudad;
import co.gov.sic.copiasycertificaciones.entities.Region;
import co.gov.sic.copiasycertificaciones.util.Constantes;
import co.gov.sic.copiasycertificaciones.util.Utility;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import sic.ws.interop.api.InteropWSClient;
import sic.ws.interop.entities.Persona;
import sic.ws.interop.entities.Referencia;
import sic.ws.interop.entities.response.ResponsePersona;

/**
 *
 * @author emosq
 */
public abstract class CCParametrosBaseBean {

    protected Logger logger = LoggerFactory.getLogger(CCParametrosBaseBean.class);
    protected boolean isSuccess;
    protected boolean isEnabled; 
    protected CamaraComercio currentCamara;
    protected List<CamaraComercio> camaras;
    protected List<Region> listaDepartamentos;
    protected List<Ciudad> listaCiudades;
    protected List<Referencia> listaTiposDocumento;

    //Variables gestion de Camara
    protected String camaraTipoDoc = "NI";
    protected String camaraTipoDocDesc = "NIT";
    protected Long camaraNumDocumento;
    protected String camaraDireccion;
    protected String camaraDireccionTipo = "TR";
    protected String camaraDireccionTipoDes = "TRABAJO";
    protected String camaraFullName;
    protected String camaraEmail;
    protected String camaraTipoeEmail = "EM";
    protected String camaraTipoEmailDesc = "EMPRESA";
    protected String camaraTelefono;
    protected String camaraTelefonoTipo = "FI";
    protected String camaraTelefonoTipoDes = "FIJO";
    protected String camaraDigitoVerificacion;
    protected String camaraTipoPersona = "EM";
    protected String camaraTipoPersonaDesc = "EMPRESA";
    protected String camaraTipoEmpresa = "PU";
    protected String camaraTipoEmpresaDesc = "PÚBLICA";
    protected boolean camaraExits;
    protected int camaraCodigoCiudad = -1;
    protected int camaraCodigoRegion = -1;
    protected String camaraNombreRegion;
    protected String camaraNombreCiudad;
    protected Date camaraFechaActo;
    protected int camaraNumActo;
    protected boolean camaraEstado;
    protected Persona personaCamara;

    protected String wsMessageConsultarPersona;
    protected ResponsePersona responsePersona;
    protected InteropWSClient wsInteropClient;

    protected List<Cesl_PersonaCamara> listaFuncionarios;
    protected Cesl_PersonaCamara currentPersonaCamara;

    public CCParametrosBaseBean() {
        isSuccess=false;
        wsInteropClient = new InteropWSClient(Constantes.WS_INTEROP_USER, Constantes.WS_INTEROP_PASS, Constantes.URL_WS_INTEROP, 500, true);
        Dal dal = new Dal();
        listaDepartamentos = dal.getDepartamentos();
        listaCiudades = new ArrayList<>();
        listaFuncionarios = new ArrayList<>();
        currentPersonaCamara = new Cesl_PersonaCamara();
        listaTiposDocumento = new ArrayList<>();
         isEnabled = false;
        try {
            listaTiposDocumento = Utility.GetReferenciaWS("DOCUMENTO");
            camaras = dal.getAllCamaras();
        } catch (IOException ex) {
            logger.error(ex.toString());
        }
    }

    protected void AddErrorMessage(String error) {
        AddErrorMessage(error, null);
    }

    protected void AddStatusMessage(String error, String clientID) {
        if (FacesContext.getCurrentInstance() != null) {
            FacesContext.getCurrentInstance().addMessage(clientID, new FacesMessage(FacesMessage.SEVERITY_INFO, error, error));
        }
    }

    protected void AddErrorMessage(String error, String clientID) {
        if (FacesContext.getCurrentInstance() != null) {
            FacesContext.getCurrentInstance().addMessage(clientID, new FacesMessage(FacesMessage.SEVERITY_ERROR, error, error));
        }
    }

    protected void AddWarningMessage(String message) {
        AddWarningMessage(message, null);
    }

    protected void AddWarningMessage(String message, String clientID) {
        if (FacesContext.getCurrentInstance() != null) {
            FacesContext.getCurrentInstance().addMessage(clientID, new FacesMessage(FacesMessage.SEVERITY_WARN, message, message));
        }
    }

    protected void AddInfoMessage(String message) {
        AddInfoMessage(message, null);
    }

    protected void AddInfoMessage(String message, String clientId) {
        if (FacesContext.getCurrentInstance() != null) {
            FacesContext.getCurrentInstance().addMessage(clientId, new FacesMessage(FacesMessage.SEVERITY_INFO, message, message));
        }
    }

    public void clearAll(){
    listaFuncionarios = new ArrayList<>();
        currentCamara = new CamaraComercio();
        camaraTipoDoc = "NI";
        camaraTipoDocDesc = "NIT";
        camaraNumDocumento=null;
        camaraDireccion = null;
        camaraDireccionTipo = "TR";
        camaraDireccionTipoDes = "TRABAJO";
        camaraFullName = null;
        camaraEmail = null;
        camaraTipoeEmail = "EM";
        camaraTipoEmailDesc = "EMPRESA";
        camaraTelefono = null;
        camaraTelefonoTipo = "FI";
        camaraTelefonoTipoDes = "FIJO";
        camaraDigitoVerificacion = null;
        camaraTipoPersona = "EM";
        camaraTipoPersonaDesc = "EMPRESA";
        camaraTipoEmpresa = "PU";
        camaraTipoEmpresaDesc = "PÚBLICA";
        camaraExits = false;
        camaraCodigoCiudad = -1;
        camaraCodigoRegion = -1;
        camaraNombreRegion = null;
        camaraNombreCiudad = null;
        camaraFechaActo = null;
        camaraNumActo=0;
        isEnabled=false;
        isSuccess=false;
        personaCamara = new Persona();
    
    
    }
    protected void limpardatos() {
        listaFuncionarios = new ArrayList<>();
        currentCamara = new CamaraComercio();
        camaraTipoDoc = "NI";
        camaraTipoDocDesc = "NIT";
        //camaraNumDocumento=0L;
        camaraDireccion = null;
        camaraDireccionTipo = "TR";
        camaraDireccionTipoDes = "TRABAJO";
        camaraFullName = null;
        camaraEmail = null;
        camaraTipoeEmail = "EM";
        camaraTipoEmailDesc = "EMPRESA";
        camaraTelefono = null;
        camaraTelefonoTipo = "FI";
        camaraTelefonoTipoDes = "FIJO";
        camaraDigitoVerificacion = null;
        camaraTipoPersona = "EM";
        camaraTipoPersonaDesc = "EMPRESA";
        camaraTipoEmpresa = "PU";
        camaraTipoEmpresaDesc = "PÚBLICA";
        camaraExits = false;
        camaraCodigoCiudad = -1;
        camaraCodigoRegion = -1;
        camaraNombreRegion = null;
        camaraNombreCiudad = null;
        camaraFechaActo = null;
        camaraNumActo = 0;
        isEnabled=false;
        personaCamara = new Persona();
    }

    /**
     * @return the listaDepartamentos
     */
    public List<Region> getListaDepartamentos() {
        return listaDepartamentos;
    }

    /**
     * @param listaDepartamentos the listaDepartamentos to set
     */
    public void setListaDepartamentos(List<Region> listaDepartamentos) {
        this.listaDepartamentos = listaDepartamentos;
    }

    /**
     * @return the listaCiudades
     */
    public List<Ciudad> getListaCiudades() {
        return listaCiudades;
    }

    /**
     * @param listaCiudades the listaCiudades to set
     */
    public void setListaCiudades(List<Ciudad> listaCiudades) {
        this.listaCiudades = listaCiudades;
    }

    /**
     * @return the camaraTipoDoc
     */
    public String getCamaraTipoDoc() {
        return camaraTipoDoc;
    }

    /**
     * @param camaraTipoDoc the camaraTipoDoc to set
     */
    public void setCamaraTipoDoc(String camaraTipoDoc) {
        this.camaraTipoDoc = camaraTipoDoc;
    }

    /**
     * @return the camaraTipoDocDesc
     */
    public String getCamaraTipoDocDesc() {
        return camaraTipoDocDesc;
    }

    /**
     * @param camaraTipoDocDesc the camaraTipoDocDesc to set
     */
    public void setCamaraTipoDocDesc(String camaraTipoDocDesc) {
        this.camaraTipoDocDesc = camaraTipoDocDesc;
    }

    /**
     * @return the camaraNumDocumento
     */
    public Long getCamaraNumDocumento() {
        return camaraNumDocumento;
    }

    /**
     * @param camaraNumDocumento the camaraNumDocumento to set
     */
    public void setCamaraNumDocumento(Long camaraNumDocumento) {
        this.camaraNumDocumento = camaraNumDocumento;
    }

    /**
     * @return the camaraDireccion
     */
    public String getCamaraDireccion() {
        return camaraDireccion;
    }

    /**
     * @param camaraDireccion the camaraDireccion to set
     */
    public void setCamaraDireccion(String camaraDireccion) {
        this.camaraDireccion = camaraDireccion;
    }

    /**
     * @return the camaraDireccionTipo
     */
    public String getCamaraDireccionTipo() {
        return camaraDireccionTipo;
    }

    /**
     * @param camaraDireccionTipo the camaraDireccionTipo to set
     */
    public void setCamaraDireccionTipo(String camaraDireccionTipo) {
        this.camaraDireccionTipo = camaraDireccionTipo;
    }

    /**
     * @return the camaraDireccionTipoDes
     */
    public String getCamaraDireccionTipoDes() {
        return camaraDireccionTipoDes;
    }

    /**
     * @param camaraDireccionTipoDes the camaraDireccionTipoDes to set
     */
    public void setCamaraDireccionTipoDes(String camaraDireccionTipoDes) {
        this.camaraDireccionTipoDes = camaraDireccionTipoDes;
    }

    /**
     * @return the camaraFullName
     */
    public String getCamaraFullName() {
        return camaraFullName;
    }

    /**
     * @param camaraFullName the camaraFullName to set
     */
    public void setCamaraFullName(String camaraFullName) {
        this.camaraFullName = camaraFullName;
    }

    /**
     * @return the camaraEmail
     */
    public String getCamaraEmail() {
        return camaraEmail;
    }

    /**
     * @param camaraEmail the camaraEmail to set
     */
    public void setCamaraEmail(String camaraEmail) {
        this.camaraEmail = camaraEmail;
    }

    /**
     * @return the camaraTipoeEmail
     */
    public String getCamaraTipoeEmail() {
        return camaraTipoeEmail;
    }

    /**
     * @param camaraTipoeEmail the camaraTipoeEmail to set
     */
    public void setCamaraTipoeEmail(String camaraTipoeEmail) {
        this.camaraTipoeEmail = camaraTipoeEmail;
    }

    /**
     * @return the camaraTipoEmailDesc
     */
    public String getCamaraTipoEmailDesc() {
        return camaraTipoEmailDesc;
    }

    /**
     * @param camaraTipoEmailDesc the camaraTipoEmailDesc to set
     */
    public void setCamaraTipoEmailDesc(String camaraTipoEmailDesc) {
        this.camaraTipoEmailDesc = camaraTipoEmailDesc;
    }

    /**
     * @return the camaraTelefono
     */
    public String getCamaraTelefono() {
        return camaraTelefono;
    }

    /**
     * @param camaraTelefono the camaraTelefono to set
     */
    public void setCamaraTelefono(String camaraTelefono) {
        this.camaraTelefono = camaraTelefono;
    }

    /**
     * @return the camaraTelefonoTipo
     */
    public String getCamaraTelefonoTipo() {
        return camaraTelefonoTipo;
    }

    /**
     * @param camaraTelefonoTipo the camaraTelefonoTipo to set
     */
    public void setCamaraTelefonoTipo(String camaraTelefonoTipo) {
        this.camaraTelefonoTipo = camaraTelefonoTipo;
    }

    /**
     * @return the camaraTelefonoTipoDes
     */
    public String getCamaraTelefonoTipoDes() {
        return camaraTelefonoTipoDes;
    }

    /**
     * @param camaraTelefonoTipoDes the camaraTelefonoTipoDes to set
     */
    public void setCamaraTelefonoTipoDes(String camaraTelefonoTipoDes) {
        this.camaraTelefonoTipoDes = camaraTelefonoTipoDes;
    }

    /**
     * @return the camaraDigitoVerificacion
     */
    public String getCamaraDigitoVerificacion() {
        return camaraDigitoVerificacion;
    }

    /**
     * @param camaraDigitoVerificacion the camaraDigitoVerificacion to set
     */
    public void setCamaraDigitoVerificacion(String camaraDigitoVerificacion) {
        this.camaraDigitoVerificacion = camaraDigitoVerificacion;
    }

    /**
     * @return the camaraTipoPersona
     */
    public String getCamaraTipoPersona() {
        return camaraTipoPersona;
    }

    /**
     * @param camaraTipoPersona the camaraTipoPersona to set
     */
    public void setCamaraTipoPersona(String camaraTipoPersona) {
        this.camaraTipoPersona = camaraTipoPersona;
    }

    /**
     * @return the camaraTipoPersonaDesc
     */
    public String getCamaraTipoPersonaDesc() {
        return camaraTipoPersonaDesc;
    }

    /**
     * @param camaraTipoPersonaDesc the camaraTipoPersonaDesc to set
     */
    public void setCamaraTipoPersonaDesc(String camaraTipoPersonaDesc) {
        this.camaraTipoPersonaDesc = camaraTipoPersonaDesc;
    }

    /**
     * @return the camaraTipoEmpresa
     */
    public String getCamaraTipoEmpresa() {
        return camaraTipoEmpresa;
    }

    /**
     * @param camaraTipoEmpresa the camaraTipoEmpresa to set
     */
    public void setCamaraTipoEmpresa(String camaraTipoEmpresa) {
        this.camaraTipoEmpresa = camaraTipoEmpresa;
    }

    /**
     * @return the camaraTipoEmpresaDesc
     */
    public String getCamaraTipoEmpresaDesc() {
        return camaraTipoEmpresaDesc;
    }

    /**
     * @param camaraTipoEmpresaDesc the camaraTipoEmpresaDesc to set
     */
    public void setCamaraTipoEmpresaDesc(String camaraTipoEmpresaDesc) {
        this.camaraTipoEmpresaDesc = camaraTipoEmpresaDesc;
    }

    /**
     * @return the camaraExits
     */
    public boolean isCamaraExits() {
        return camaraExits;
    }

    /**
     * @param camaraExits the camaraExits to set
     */
    public void setCamaraExits(boolean camaraExits) {
        this.camaraExits = camaraExits;
    }

    /**
     * @return the camaraCodigoCiudad
     */
    public int getCamaraCodigoCiudad() {
        return camaraCodigoCiudad;
    }

    /**
     * @param camaraCodigoCiudad the camaraCodigoCiudad to set
     */
    public void setCamaraCodigoCiudad(int camaraCodigoCiudad) {
        this.camaraCodigoCiudad = camaraCodigoCiudad;
    }

    /**
     * @return the camaraCodigoRegion
     */
    public int getCamaraCodigoRegion() {
        return camaraCodigoRegion;
    }

    /**
     * @param camaraCodigoRegion the camaraCodigoRegion to set
     */
    public void setCamaraCodigoRegion(int camaraCodigoRegion) {
        this.camaraCodigoRegion = camaraCodigoRegion;
    }

    /**
     * @return the camaraNombreRegion
     */
    public String getCamaraNombreRegion() {
        return camaraNombreRegion;
    }

    /**
     * @param camaraNombreRegion the camaraNombreRegion to set
     */
    public void setCamaraNombreRegion(String camaraNombreRegion) {
        this.camaraNombreRegion = camaraNombreRegion;
    }

    /**
     * @return the camaraNombreCiudad
     */
    public String getCamaraNombreCiudad() {
        return camaraNombreCiudad;
    }

    /**
     * @param camaraNombreCiudad the camaraNombreCiudad to set
     */
    public void setCamaraNombreCiudad(String camaraNombreCiudad) {
        this.camaraNombreCiudad = camaraNombreCiudad;
    }

    /**
     * @return the camaraFechaActo
     */
    public Date getCamaraFechaActo() {
        return camaraFechaActo;
    }

    /**
     * @param camaraFechaActo the camaraFechaActo to set
     */
    public void setCamaraFechaActo(Date camaraFechaActo) {
        this.camaraFechaActo = camaraFechaActo;
    }

    /**
     * @return the camaras
     */
    public List<CamaraComercio> getCamaras() {
        return camaras;
    }

    /**
     * @param camaras the camaras to set
     */
    public void setCamaras(List<CamaraComercio> camaras) {
        this.camaras = camaras;
    }

    /**
     * @return the camaraNumActo
     */
    public int getCamaraNumActo() {
        return camaraNumActo;
    }

    /**
     * @param camaraNumActo the camaraNumActo to set
     */
    public void setCamaraNumActo(int camaraNumActo) {
        this.camaraNumActo = camaraNumActo;
    }

    /**
     * @return the listaFuncionarios
     */
    public List<Cesl_PersonaCamara> getListaFuncionarios() {
        return listaFuncionarios;
    }

    /**
     * @param listaFuncionarios the listaFuncionarios to set
     */
    public void setListaFuncionarios(List<Cesl_PersonaCamara> listaFuncionarios) {
        this.listaFuncionarios = listaFuncionarios;
    }

    /**
     * @return the currentPersonaCamara
     */
    public Cesl_PersonaCamara getCurrentPersonaCamara() {
        return currentPersonaCamara;
    }

    /**
     * @param currentPersonaCamara the currentPersonaCamara to set
     */
    public void setCurrentPersonaCamara(Cesl_PersonaCamara currentPersonaCamara) {
        this.currentPersonaCamara = currentPersonaCamara;
    }

    /**
     * @return the listaTiposDocumento
     */
    public List<Referencia> getListaTiposDocumento() {
        return listaTiposDocumento;
    }

    /**
     * @param listaTiposDocumento the listaTiposDocumento to set
     */
    public void setListaTiposDocumento(List<Referencia> listaTiposDocumento) {
        this.listaTiposDocumento = listaTiposDocumento;
    }

    /**
     * @return the currentCamara
     */
    public CamaraComercio getCurrentCamara() {
        return currentCamara;
    }

    /**
     * @param currentCamara the currentCamara to set
     */
    public void setCurrentCamara(CamaraComercio currentCamara) {
        this.currentCamara = currentCamara;
    }

    /**
     * @return the isSuccess
     */
    public boolean isIsSuccess() {
        return isSuccess;
    }

    /**
     * @param isSuccess the isSuccess to set
     */
    public void setIsSuccess(boolean isSuccess) {
        this.isSuccess = isSuccess;
    }

    /**
     * @return the isEnabled
     */
    public boolean isIsEnabled() {
        return isEnabled;
    }

    /**
     * @param isEnabled the isEnabled to set
     */
    public void setIsEnabled(boolean isEnabled) {
        this.isEnabled = isEnabled;
    }

    /**
     * @return the camaraEstado
     */
    public boolean isCamaraEstado() {
        return camaraEstado;
    }

    /**
     * @param camaraEstado the camaraEstado to set
     */
    public void setCamaraEstado(boolean camaraEstado) {
        this.camaraEstado = camaraEstado;
    }



    
    
}
