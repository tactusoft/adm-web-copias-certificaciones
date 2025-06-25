/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package co.gov.sic.copiasycertificaciones.beans;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.primefaces.PrimeFaces;
import org.primefaces.event.FlowEvent;

import co.gov.sic.copiasycertificaciones.dataaccess.Dal;
import co.gov.sic.copiasycertificaciones.entities.CamaraComercio;
import co.gov.sic.copiasycertificaciones.entities.CamarasDetalle;
import co.gov.sic.copiasycertificaciones.entities.Cesl_PersonaCamara;
import co.gov.sic.copiasycertificaciones.entities.Ciudad;
import co.gov.sic.copiasycertificaciones.entities.Region;
import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Named;
import sic.ws.interop.entities.Direccion;
import sic.ws.interop.entities.Email;
import sic.ws.interop.entities.Empresa;
import sic.ws.interop.entities.Persona;
import sic.ws.interop.entities.Telefono;

/**
 *
 * @author Ernesto Mosqera
 */
@Named("cCParametrosBean")
@SessionScoped
public class CCParametrosBean extends CCParametrosBaseBean implements Serializable {

    private static final long serialVersionUID = 1L;
	protected Logger logger = LoggerFactory.getLogger(CCParametrosBean.class);
    private List<String> filtroEstado;

    public void onChangeCiudadCamara() {

        for (Ciudad ciudad : listaCiudades) {
            if (ciudad.getCodi_ciud() == camaraCodigoCiudad) {
                camaraNombreCiudad = ciudad.getNomb_ciud();
            }
        }
    }

    public CCParametrosBean() {
        super();
        logger.info("Configuracion CCpAraemteros conbstructir");
        filtroEstado = new ArrayList<>();
        filtroEstado.add("Activo");
        filtroEstado.add("Inactivo");
    }

    public void realoadListaCamaras() {
        Dal Dal = new Dal();
        camaras = Dal.getAllCamaras();
    }

    public void eliminarFuncionario(Cesl_PersonaCamara funcionario) {
        Dal Dal = new Dal();
        if (Dal.deletePersonaCamara(funcionario)) {
            listaFuncionarios.remove(funcionario);
            currentPersonaCamara = new Cesl_PersonaCamara();
        }

    }

    private boolean guardarFuncionario() {
        Dal Dal = new Dal();
        boolean success = true;

        for (Cesl_PersonaCamara funcionario : listaFuncionarios) {
            funcionario.setIden_pers(personaCamara.getId());
            if (!Dal.insertPersonaCamara(funcionario)) {
                success = false;
                break;
            }
        }
        return success;
    }

    public void agregarFuncionario() {
        currentPersonaCamara.setIden_pers(personaCamara.getId());

        if (validateRol(currentPersonaCamara.getRol())) {
            this.AddWarningMessage("Ya existe un funcionario con el rol " + currentPersonaCamara.getRol() + ", para poder agregerlo por favor inhabilite el actual");
            return;
        }

        if (!listaFuncionarios.contains(currentPersonaCamara)) {

            listaFuncionarios.add(currentPersonaCamara);
            this.AddInfoMessage("Funcionario : " + currentPersonaCamara.getNomb_perso() + " agregado correctamente");
        } else {

            for (Cesl_PersonaCamara funcionario : listaFuncionarios) {

                if (funcionario.getIdsecrecamaras() == currentPersonaCamara.getIdsecrecamaras()) {
                    listaFuncionarios.remove(funcionario);
                    listaFuncionarios.add(currentPersonaCamara);
                    this.AddInfoMessage("Funcionario : " + currentPersonaCamara.getNomb_perso() + " Actualizado correctamente");
                    break;
                }
            }

        }

        currentPersonaCamara = new Cesl_PersonaCamara();
    }

    private boolean validateRol(String rol) {
        boolean rolExist = false;

        if (!rol.equals("RL")) {
            return rolExist;
        }

        for (Cesl_PersonaCamara pc : listaFuncionarios) {
            if (pc.getRol().equals(rol) && pc.isIsEnabled()) {
                rolExist = true;
                break;
            }
        }

        return rolExist;
    }

    public void onChangeNumDocumento() {

    }

    public void onChangeNumDocumentoCamara() {

        try {
            //personaCamara = new Persona();
            personaCamara.setTipoDocumento(camaraTipoDoc);
            personaCamara.setNumeroDocumento(camaraNumDocumento);
            personaCamara.setRetornarSoloUltimosDatos(true);

            if (!personExists(personaCamara)) {
                camaraExits = false;
                limpardatos();
                personaCamara.setTipoDocumento(camaraTipoDoc);
                personaCamara.setNumeroDocumento(camaraNumDocumento);
                personaCamara.setRetornarSoloUltimosDatos(true);
                return;
            }
            camaraExits = true;
            personaCamara = responsePersona.getPersona();
            camaraTipoDoc = personaCamara.getTipoDocumento();
            camaraTipoDocDesc = personaCamara.getTipoDocumentoDesc();
            camaraNumDocumento = personaCamara.getNumeroDocumento();
            if (!personaCamara.getDirecciones().isEmpty()) {
                camaraDireccion = personaCamara.getDirecciones().get(0).getDescripcion();

                if (!personaCamara.getDirecciones().get(0).getTelefonos().isEmpty()) {
                    camaraTelefono = personaCamara.getDirecciones().get(0).getTelefonos().get(0).getNumero();
                }
            }
            if (!personaCamara.getEmails().isEmpty()) {
                camaraEmail = personaCamara.getEmails().get(0).getDescripcion();
            }

            if (personaCamara.getEsEmpresa()) {
                camaraDigitoVerificacion = personaCamara.getEmpresa().getDigitoVerificacion();
                camaraFullName = personaCamara.getEmpresa().getRazonSocial();
                camaraTipoEmpresa = personaCamara.getEmpresa().getTipoEmpresa();
                camaraTipoEmpresaDesc = personaCamara.getEmpresa().getTipoEmpresaDes();
            }

            camaraTipoPersona = personaCamara.getTipoPersona();
            camaraTipoPersonaDesc = personaCamara.getTipoPersonaDesc();

            Dal Dal = new Dal();
            CamarasDetalle dc = Dal.getCamaraDetalles(personaCamara.getId());
            camaraNumActo = dc.getNum_decre();
            camaraFechaActo = dc.getFech_decre();
            camaraEstado = dc.getEstado();

            for (Region r : listaDepartamentos) {
                if (!personaCamara.getDirecciones().isEmpty()) {
                    if (r.getCodi_regi() == personaCamara.getDirecciones().get(0).getCodigoRegion()) {
                        camaraCodigoRegion = r.getCodi_regi();
                        listaCiudades = Dal.getCiudades(camaraCodigoRegion);
                    }
                }
            }
            for (Ciudad ciudad : listaCiudades) {
                if (!personaCamara.getDirecciones().isEmpty()) {
                    if (ciudad.getCodi_ciud() == personaCamara.getDirecciones().get(0).getCodigoCiudad()) {
                        camaraCodigoCiudad = ciudad.getCodi_ciud();
                    }
                }
            }

            onChangeDepartamento();
            onChangeCiudadCamara();

            //listaFuncionarios = Dal.getAllPersonascamara(personaCamara.getId());
        } catch (NullPointerException ex) {
            this.AddErrorMessage("El numero de documento es requerido");
        }

    }

    public void onChangeDepartamento() {
        setListaCiudades(new ArrayList<>());
        try {

            for (Region r : listaDepartamentos) {
                if (r.getCodi_regi() == camaraCodigoRegion) {
                    camaraNombreRegion = r.getNomb_regi();
                }
            }

            Dal Dal = new Dal();
            setListaCiudades(Dal.getCiudades(camaraCodigoRegion));
        } catch (Exception ex) {
            logger.error(ex.toString());
        }
    }

    private boolean guardarDatosCamara() {

        Dal Dal = new Dal();
        try {

            if (personExists(personaCamara)) {
                return true;
            }

            responsePersona = wsInteropClient.personaRegistrar(personaCamara);
            if (responsePersona.getCodigo() != 0) {
                wsMessageConsultarPersona = responsePersona.getMensaje();
                return false;
            }
            personaCamara = responsePersona.getPersona();
            CamarasDetalle cd = new CamarasDetalle(responsePersona.getPersona().getId(), camaraNumActo, camaraFechaActo, camaraEstado);
            boolean answer = Dal.insertDetalleCamara(cd);
            //onChangeNumDocumentoCamara();

            return answer;

        } catch (IOException | NumberFormatException ex) {
            logger.error(ex.toString());
            wsMessageConsultarPersona = ex.toString();
            return false;
        }
    }

    private boolean setCamaraPersona() {

        try {
            if ((camaraTipoDoc.isEmpty() || camaraTipoDoc == null)
                    || (camaraNumDocumento == 0 || camaraNumDocumento == null)
                    || (camaraDireccion.isEmpty() || camaraDireccion == null)
                    || (camaraFullName.isEmpty() || camaraFullName == null)
                    || (camaraTelefono.isEmpty() || camaraTelefono == null)
                    || (camaraEmail.isEmpty() || camaraEmail == null)
                    || (camaraDigitoVerificacion.isEmpty() || camaraDigitoVerificacion == null)
                    || (camaraTipoPersona.isEmpty() || camaraTipoPersona == null)
                    || (camaraNombreRegion.isEmpty() || camaraNombreRegion == null)
                    || (camaraNombreCiudad.isEmpty() || camaraNombreCiudad == null)
                    || (camaraCodigoCiudad == -1)
                    || (camaraCodigoRegion == -1)) {

                wsMessageConsultarPersona = "Los campos marcados en \"|\" rojo son obligatorios";
                return false;
            }
        } catch (Exception ex) {
            logger.warn("Ningun campo puede ser null");
            wsMessageConsultarPersona = "Los campos marcados en \"|\" rojo son obligatorios";
            return false;
        }

        List<Email> listEmail = new ArrayList<>();
        //personaCamara = new Persona();
        personaCamara.setTipoDocumento(camaraTipoDoc);
        personaCamara.setTipoDocumentoDesc(camaraTipoDocDesc);
        personaCamara.setTipoPersona("EM");
        personaCamara.setTipoPersonaDesc("EMPRESA");
        personaCamara.setAnonimo(false);
        personaCamara.setNumeroDocumento(getCamaraNumDocumento());
        List<Direccion> listaDir = new ArrayList<>();
        List<Telefono> listaTel = new ArrayList<>();
        Direccion dir = new Direccion();
        dir.setCodigoCiudad(camaraCodigoCiudad);
        dir.setCodigoRegion(camaraCodigoRegion);
        dir.setCodigoPais("CO");
        dir.setCodigoPaisDesc("COLOMBIA");
        dir.setCodigoContinente("AME");
        dir.setTipo("TR");
        dir.setTipoDesc("TRABAJO");

        Telefono tel = new Telefono();
        tel.setExtension("");
        tel.setNumero(camaraTelefono);
        tel.setTipo(camaraTelefonoTipo);
        tel.setTipoDesc(camaraTelefonoTipoDes);
        listaTel.add(tel);
        dir.setTelefonos(listaTel);

        dir.setDescripcion(camaraDireccion);
        listaDir.add(dir);
        personaCamara.setDirecciones(listaDir);

        Email emailLocal = new Email();
        emailLocal.setDescripcion(camaraEmail);
        emailLocal.setTipo(camaraTipoeEmail);
        emailLocal.setTipoDesc(camaraTipoEmailDesc);
        listEmail.add(emailLocal);
        personaCamara.setEmails(listEmail);

        Empresa empresaLocal = new Empresa();
        empresaLocal.setRazonSocial(camaraFullName);
        empresaLocal.setDescripcion(getCamaraFullName());
        empresaLocal.setTipoEmpresa(camaraTipoEmpresa);
        empresaLocal.setTipoEmpresaDesc(camaraTipoEmpresa);
        empresaLocal.setDigitoVerificacion(getCamaraDigitoVerificacion());
        personaCamara.setEmpresa(empresaLocal);
        return true;
    }

    public String processWorkFlow(FlowEvent event) {
        final String anteriorStep = event.getOldStep();
        final String nuevoStep = event.getNewStep();
        Dal Dal = new Dal();
        Boolean moveWizardSteps = null;
        try {
            PrimeFaces.current().ajax().update("frmWizardCamaras:pnlButtons");
            if (anteriorStep.equals("datosgenerales")) {
                if (!setCamaraPersona()) {
                    this.AddErrorMessage(wsMessageConsultarPersona);
                    return anteriorStep;
                }
                if (personExists(personaCamara)) {
                    listaFuncionarios = Dal.getAllPersonascamara(personaCamara.getId());
                }
            }
            if (anteriorStep.equals("funcionarios") && nuevoStep.equals("datosgenerales")) {
                moveWizardSteps = false;
                return nuevoStep;
            } else if (anteriorStep.equals("funcionarios") && nuevoStep.equals("pago")) {
                setIsSuccess(guardarDatosCamara() && guardarFuncionario());
                if (isSuccess) {
                    limpardatos();
                    moveWizardSteps = true;
                    return nuevoStep;
                } else {
                    this.AddErrorMessage(wsMessageConsultarPersona);
                    return anteriorStep;
                }

            }
            moveWizardSteps = true;
            return nuevoStep;
        } catch (Exception e) {
            logger.error("procesoFlujo", e);
            this.AddErrorMessage("Ha ocurrido un error inesperado.");
            moveWizardSteps = null;
            return event.getOldStep();
        } finally {
            if (moveWizardSteps != null) {
                PrimeFaces.current().executeScript("onmoveWizard(" + String.valueOf(moveWizardSteps) + ");");
            }
            PrimeFaces.current().executeScript("BindToolTips();");
        }

    }

    private boolean personExists(Persona persona) {

        try {
            responsePersona = wsInteropClient.personaConsultar(persona);
            if (responsePersona.getCodigo() == 0) {
                if (responsePersona.getPersona() == null) {
                    return false;
                } else {
                    personaCamara = responsePersona.getPersona();
                    return true;
                }
            } else {
                wsMessageConsultarPersona = responsePersona.getMensaje();
                return true;
            }
        } catch (IOException ex) {
            logger.error(ex.toString());
            wsMessageConsultarPersona = ex.toString();
        }
        return true;
    }

    // onChangeNumDocumentoCamara() {
    @Override
    public void setCurrentCamara(CamaraComercio currentCamara) {
        clearAll();
        super.setCurrentCamara(currentCamara);
        camaraTipoDoc = currentCamara.getTipoDocumento();
        camaraNumDocumento = currentCamara.getNumeroDocumento();
        onChangeNumDocumentoCamara();
    }

    /**
     * @return the filtroEstado
     */
    public List<String> getFiltroEstado() {
        return filtroEstado;
    }
    
    
    

}
