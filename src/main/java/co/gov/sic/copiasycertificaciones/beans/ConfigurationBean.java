/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package co.gov.sic.copiasycertificaciones.beans;

import static co.gov.sic.copiasycertificaciones.util.Constantes.COORDINADOR_SCC;
import static co.gov.sic.copiasycertificaciones.util.Constantes.RESPONSABLE_SCC;
import static co.gov.sic.copiasycertificaciones.util.Constantes.VENTANILLA_SE;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import co.gov.sic.copiasycertificaciones.dataaccess.Dal;
import co.gov.sic.copiasycertificaciones.entities.Cesl_config;
import co.gov.sic.copiasycertificaciones.entities.Dependencia;
import co.gov.sic.copiasycertificaciones.entities.Tasa;
import co.gov.sic.copiasycertificaciones.util.Constantes;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;
import sic.ws.interop.api.InteropWSClient;
import sic.ws.interop.entities.AdscritoDepe;
import sic.ws.interop.entities.Persona;
import sic.ws.interop.entities.response.ResponseAdscritos;
import sic.ws.interop.entities.response.ResponsePersona;

/**
 *
 * @author emosquera
 */
@Named("configurationBean")
@SessionScoped
public class ConfigurationBean implements Serializable {
	
	private static final long serialVersionUID = 6674042845641020324L;
	private List<Cesl_config> cfg;
    private String title = "Configuración de los días";
    private String nomb_tasa;
    private double tasa;
    private int maturityDays;
    private boolean MaturityBusinessDay = false;
    private int paymentDays;
    private boolean paymentBusinessDay = false;
    private int defermentDays;
    private boolean defermentBusinessDay = false;
    private int alertDays;
    private boolean alertBusinessDay = false;
    private int complementDays;
    private boolean complementBusinessDay = false;
    private int internalDays;
    private boolean internalBusinessDay = false;

    private Integer codigoDependencia = -1;
    private Long idFuncionario = 0L;
    private List<AdscritoDepe> listaAdscritos;
    private List<Dependencia> listaDependencias;
    private Persona funcionarioAsignado;
    private int rol = -1;
    private boolean habilitaAsignarRol = false;
    private List<Tasa> listaTasas;
    protected final Logger logger = LoggerFactory.getLogger(BeanBase.class);
    private String codigoExpCopias;
    private String linkEncuesta;

    public ConfigurationBean() throws Exception {
        cfg = new ArrayList<>();
        listaTasas = new ArrayList<>();

        listaAdscritos = new ArrayList<>();
        listaDependencias = new ArrayList<>();
        getAllDependencies();

        try ( Dal Dal = new Dal()) {
            cfg = Dal.getDayConfigParameters();
        }

        try ( Dal Dal = new Dal()) {
            listaTasas = Dal.getTasas();
        }

        for (Cesl_config conf : cfg) {
            switch (conf.getLlave()) {
                case "linkencuesta":
                    linkEncuesta = conf.getValorString();
                    break;
                case "codigocopias":
                    codigoExpCopias = conf.getValorString();
                    break;
                case "maturity_days":
                    maturityDays = conf.getValor();
                    MaturityBusinessDay = conf.isBusinessDays();
                    break;
                case "payment_days":
                    paymentDays = conf.getValor();
                    paymentBusinessDay = conf.isBusinessDays();
                    break;
                case "deferment_days":
                    defermentDays = conf.getValor();
                    defermentBusinessDay = conf.isBusinessDays();
                    break;
                case "alert_days":
                    alertDays = conf.getValor();
                    alertBusinessDay = conf.isBusinessDays();
                    break;
                case "complement_days":
                    complementDays = conf.getValor();
                    complementBusinessDay = conf.isBusinessDays();
                    break;
                case "internal_days":
                    internalDays = conf.getValor();
                    internalBusinessDay = conf.isBusinessDays();
                    break;
                default:
                    break;
            }

        }

    }

    public void guardarMicelaneos() {
        try {
            Dal dal = new Dal();
            if (!dal.saveMicelaneos("linkencuesta", linkEncuesta) || !dal.saveMicelaneos("codigocopias", codigoExpCopias)) {
                this.AddErrorMessage("Error al guardar los datos", "msgMiselaneos");
            } else {
                this.AddStatusMessage("Configuraciones guardadas correctamente", "msgMiselaneos");
            }

        } catch (Exception ex) {
            logger.error(ex.toString());
        }
    }

    /**
     * @return the codigoDependencia
     */
    public Integer getCodigoDependencia() {
        return codigoDependencia;
    }

    /**
     * @param codigoDependencia the codigoDependencia to set
     */
    public void setCodigoDependencia(Integer codigoDependencia) {
        this.codigoDependencia = codigoDependencia;
    }

    private void getAllDependencies() throws Exception {
        try ( Dal Dal = new Dal()) {
            setListaDependencias(Dal.getAllDependencies());
        }
    }

    public void onChangeDependency() throws IOException {
        InteropWSClient wsInteropClient = new InteropWSClient(Constantes.WS_INTEROP_USER, Constantes.WS_INTEROP_PASS, Constantes.URL_WS_INTEROP, 500, true);
        getListaAdscritos().clear();

        ResponseAdscritos adscritos = wsInteropClient.dependenciaAdscritos(codigoDependencia);
        setListaAdscritos(adscritos.getAdscritos());

        setHabilitaAsignarRol(false);

    }

    /**
     * @return the idFuncionario
     */
    public Long getIdFuncionario() {
        return idFuncionario;
    }

    /**
     * @param idFuncionario the idFuncionario to set
     */
    public void setIdFuncionario(Long idFuncionario) {
        this.idFuncionario = idFuncionario;
    }

    public void saveConfiguration() throws Exception {

        List<Cesl_config> localCfg = new ArrayList<>();

        Cesl_config cfgMaturity = new Cesl_config("maturity_days",
                String.valueOf(maturityDays),
                MaturityBusinessDay);

        Cesl_config cfgPayment = new Cesl_config("payment_days",
                String.valueOf(paymentDays),
                paymentBusinessDay);
        Cesl_config cfgDeferment = new Cesl_config("deferment_days",
                String.valueOf(defermentDays),
                defermentBusinessDay);

        Cesl_config cfgAlert = new Cesl_config("alert_days",
                String.valueOf(alertDays),
                alertBusinessDay);

        Cesl_config cfgComplement = new Cesl_config("complement_days",
                String.valueOf(complementDays),
                complementBusinessDay);

        Cesl_config cfgInternal = new Cesl_config("internal_days",
                String.valueOf(internalDays),
                internalBusinessDay);

        localCfg.add(cfgAlert);
        localCfg.add(cfgInternal);
        localCfg.add(cfgComplement);
        localCfg.add(cfgDeferment);
        localCfg.add(cfgPayment);
        localCfg.add(cfgMaturity);
        boolean response;
        try ( Dal Dal = new Dal()) {
            response = Dal.saveDayConfiguration(localCfg);
        }

        if (response) {
            this.AddStatusMessage("Las configuraciones han sido guardadas", "msgCfg");
        } else {
            this.AddErrorMessage("Se presento un error al guardar, por favor intente mas tarde o contacte a soporte técnico", "msgCfg");
        }
    }

    @PostConstruct
    public void init() {

    }

    /**
     * @return the cfg
     */
    public List<Cesl_config> getCfg() {
        return cfg;
    }

    /**
     * @param cfg the cfg to set
     */
    public void setCfg(List<Cesl_config> cfg) {
        this.cfg = cfg;
    }

    /**
     * @return the title
     */
    public String getTitle() {
        return title;
    }

    /**
     * @return the maturityDays
     */
    public int getMaturityDays() {
        return maturityDays;
    }

    /**
     * @param maturityDays the maturityDays to set
     */
    public void setMaturityDays(int maturityDays) {
        this.maturityDays = maturityDays;
    }

    /**
     * @return the MaturityBusinessDay
     */
    public boolean isMaturityBusinessDay() {
        return MaturityBusinessDay;
    }

    /**
     * @param MaturityBusinessDay the MaturityBusinessDay to set
     */
    public void setMaturityBusinessDay(boolean MaturityBusinessDay) {
        this.MaturityBusinessDay = MaturityBusinessDay;
    }

    /**
     * @return the paymentDays
     */
    public int getPaymentDays() {
        return paymentDays;
    }

    /**
     * @param paymentDays the paymentDays to set
     */
    public void setPaymentDays(int paymentDays) {
        this.paymentDays = paymentDays;
    }

    /**
     * @return the paymentBusinessDay
     */
    public boolean isPaymentBusinessDay() {
        return paymentBusinessDay;
    }

    /**
     * @param paymentBusinessDay the paymentBusinessDay to set
     */
    public void setPaymentBusinessDay(boolean paymentBusinessDay) {
        this.paymentBusinessDay = paymentBusinessDay;
    }

    /**
     * @return the defermentDays
     */
    public int getDefermentDays() {
        return defermentDays;
    }

    /**
     * @param defermentDays the defermentDays to set
     */
    public void setDefermentDays(int defermentDays) {
        this.defermentDays = defermentDays;
    }

    /**
     * @return the defermentBusinessDay
     */
    public boolean isDefermentBusinessDay() {
        return defermentBusinessDay;
    }

    /**
     * @param defermentBusinessDay the defermentBusinessDay to set
     */
    public void setDefermentBusinessDay(boolean defermentBusinessDay) {
        this.defermentBusinessDay = defermentBusinessDay;
    }

    /**
     * @return the alertDays
     */
    public int getAlertDays() {
        return alertDays;
    }

    /**
     * @param alertDays the alertDays to set
     */
    public void setAlertDays(int alertDays) {
        this.alertDays = alertDays;
    }

    /**
     * @return the alertBusinessDay
     */
    public boolean isAlertBusinessDay() {
        return alertBusinessDay;
    }

    /**
     * @param alertBusinessDay the alertBusinessDay to set
     */
    public void setAlertBusinessDay(boolean alertBusinessDay) {
        this.alertBusinessDay = alertBusinessDay;
    }

    /**
     * @return the complementDays
     */
    public int getComplementDays() {
        return complementDays;
    }

    /**
     * @param complementDays the complementDays to set
     */
    public void setComplementDays(int complementDays) {
        this.complementDays = complementDays;
    }

    /**
     * @return the complementBusinessDay
     */
    public boolean isComplementBusinessDay() {
        return complementBusinessDay;
    }

    /**
     * @param complementBusinessDay the complementBusinessDay to set
     */
    public void setComplementBusinessDay(boolean complementBusinessDay) {
        this.complementBusinessDay = complementBusinessDay;
    }

    /**
     * @return the internalDays
     */
    public int getInternalDays() {
        return internalDays;
    }

    /**
     * @param internalDays the internalDays to set
     */
    public void setInternalDays(int internalDays) {
        this.internalDays = internalDays;
    }

    /**
     * @return the internalBusinessDay
     */
    public boolean isInternalBusinessDay() {
        return internalBusinessDay;
    }

    /**
     * @param internalBusinessDay the internalBusinessDay to set
     */
    public void setInternalBusinessDay(boolean internalBusinessDay) {
        this.internalBusinessDay = internalBusinessDay;
    }

    protected void AddErrorMessage(String error, String clientID) {
        if (FacesContext.getCurrentInstance() != null) {
            FacesContext.getCurrentInstance().addMessage(clientID, new FacesMessage(FacesMessage.SEVERITY_ERROR, error, error));
        }
    }

    protected void AddStatusMessage(String error, String clientID) {
        if (FacesContext.getCurrentInstance() != null) {
            FacesContext.getCurrentInstance().addMessage(clientID, new FacesMessage(FacesMessage.SEVERITY_INFO, error, error));
        }
    }

    public void onChangeTasa() {
        listaTasas.stream().filter(tasaLocal -> (tasaLocal.getShortName().equals(nomb_tasa))).forEachOrdered(tasaLocal -> {
            tasa = tasaLocal.getTasa();
        });

    }

    public void onChangeDependencyFuncionario() throws IOException {

        InteropWSClient wsInteropClient = new InteropWSClient(Constantes.WS_INTEROP_USER, Constantes.WS_INTEROP_PASS, Constantes.URL_WS_INTEROP, 500, true);
        ResponsePersona responsePersona = wsInteropClient.personaConsultar(getIdFuncionario());
        setFuncionarioAsignado(responsePersona.getPersona());
        if (funcionarioAsignado != null) {
            setHabilitaAsignarRol(true);
        } else {
            setHabilitaAsignarRol(false);
        }

    }

    public void asignarRol() throws Exception {
        boolean response;
        try ( Dal Dal = new Dal()) {
            response = Dal.asignarRol(funcionarioAsignado, rol);
        }

        if (response) {
            this.AddStatusMessage("El rol fue asignado correctamente", "msgAsignarRol");
        } else {
            this.AddErrorMessage("Ha ocurrido un error. El rol no fue asignado", "msgAsignarRol");
        }
    }

    /**
     * @return the listaAdscritos
     */
    public List<AdscritoDepe> getListaAdscritos() {
        return listaAdscritos;
    }

    /**
     * @param listaAdscritos the listaAdscritos to set
     */
    public void setListaAdscritos(List<AdscritoDepe> listaAdscritos) {
        this.listaAdscritos = listaAdscritos;
    }

    /**
     * @return the listaDependencias
     */
    public List<Dependencia> getListaDependencias() {
        return listaDependencias;
    }

    /**
     * @param listaDependencias the listaDependencias to set
     */
    public void setListaDependencias(List<Dependencia> listaDependencias) {
        this.listaDependencias = listaDependencias;
    }

    /**
     * @return the funcionarioAsignado
     */
    public Persona getFuncionarioAsignado() {
        return funcionarioAsignado;
    }

    /**
     * @param funcionarioAsignado the funcionarioAsignado to set
     */
    public void setFuncionarioAsignado(Persona funcionarioAsignado) {
        this.funcionarioAsignado = funcionarioAsignado;
    }

    /**
     * @return the rol
     */
    public int getRol() {
        return rol;
    }

    /**
     * @param rol the rol to set
     */
    public void setRol(int rol) {
        this.rol = rol;
    }

    /**
     * @return the habilitaAsignarRol
     */
    public boolean isHabilitaAsignarRol() {
        return habilitaAsignarRol;
    }

    /**
     * @param habilitaAsignarRol the habilitaAsignarRol to set
     */
    public void setHabilitaAsignarRol(boolean habilitaAsignarRol) {
        this.habilitaAsignarRol = habilitaAsignarRol;
    }

    /**
     * @return the listaTasas
     */
    public List<Tasa> getListaTasas() {
        return listaTasas;
    }

    /**
     * @param listaTasas the listaTasas to set
     */
    public void setListaTasas(List<Tasa> listaTasas) {
        this.listaTasas = listaTasas;
    }

    /**
     * @return the nomb_tasa
     */
    public String getNomb_tasa() {
        return nomb_tasa;
    }

    /**
     * @param nomb_tasa the nomb_tasa to set
     */
    public void setNomb_tasa(String nomb_tasa) {
        this.nomb_tasa = nomb_tasa;
    }

    /**
     * @return the tasa
     */
    public double getTasa() {
        return tasa;
    }

    /**
     * @param tasa the tasa to set
     */
    public void setTasa(double tasa) {
        this.tasa = tasa;
    }

    public void guardarTasa() {
        if (tasa == 0) {
            this.AddErrorMessage("El campo valor de la tasa es obligatorio", "msgSetTasa");
            return;
        } else if (nomb_tasa.isEmpty() || nomb_tasa == null) {
            this.AddErrorMessage("El campo nombre de la tasa es obligatorio", "msgSetTasa");
            return;
        }
        try {
            Dal dal = new Dal();
            if (dal.guardarTasa(nomb_tasa, tasa)) {
                listaTasas.clear();
                listaTasas = dal.getTasas();
                this.AddStatusMessage("Tasa guardada correctamente", "msgSetTasa");
            } else {
                this.AddErrorMessage("Ocurrio un error al guardar la tasa", "msgSetTasa");
            }

        } catch (Exception ex) {
            logger.error(ex.toString());
            this.AddErrorMessage("Ocurrio un error al guardar la tasa", "msgSetTasa");
        }
    }

    /**
     * @return the codigoExpCopias
     */
    public String getCodigoExpCopias() {
        return codigoExpCopias;
    }

    /**
     * @param codigoExpCopias the codigoExpCopias to set
     */
    public void setCodigoExpCopias(String codigoExpCopias) {
        this.codigoExpCopias = codigoExpCopias;
    }

    /**
     * @return the linkEncuesta
     */
    public String getLinkEncuesta() {
        return linkEncuesta;
    }

    /**
     * @param linkEncuesta the linkEncuesta to set
     */
    public void setLinkEncuesta(String linkEncuesta) {
        this.linkEncuesta = linkEncuesta;
    }
    
    
    public String rolCoordinador() {
        return COORDINADOR_SCC;
    }

    public String rolFuncionario() {
        return RESPONSABLE_SCC;
    }

    public  String rolVentanilla() {
        return VENTANILLA_SE;
    }

}
