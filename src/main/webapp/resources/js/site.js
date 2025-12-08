const PROCESSING_TEMPLATE = "<div class='text-success' style='vertical-align:middle'><div class='spinner-border text-success' role='status'><span class='sr-only'>Procesando...</span></div> Procesando...</div>";
var divMaskTemp = null;
var SPMaskBehavior = function (val) {
    return '#.##0,';
};
var spOptions = {
    translation: {
        ',': {
            pattern: /[,]/,
            optional: true
        }
    },
    reverse: true,
    onKeyPress: function (val, e, field, options) {
        field.mask(SPMaskBehavior.apply({}, arguments), options);
    }
};
function goToPSE() {
    var frmPay = $('#frmPay');
    if (frmPay.length > 0)
    {
        var form = $('<form method="post" action="' + frmPay.data("url") + '"><form>');
        var inputs = frmPay.find('input');
        inputs.each(function (index) {
            form.append($(this));
        });
        $('body').append(form);
        form.submit();
    } else
        hideProcessing(0);
}


function gotToVisor(url){
    var objeto_window_referencia;
    var configuracion_ventana = "menubar=yes,location=yes,resizable=yes,scrollbars=yes,status=yes";
    console.log(url);
    objeto_window_referencia = window.open(url, "Pagina_CNN", configuracion_ventana);
    
}

function hideProcessing(timeDelay) {
    if (timeDelay === null)
        timeDelay = 1000;
    setTimeout(function () {
        PF('dlgProcessing').hide();
    }, timeDelay);
}

function removeUIClasess() {
    var clases = $("[class*='ui-']");
    clases.removeClass("ui-widget").removeClass("ui-state-default")
            .removeClass("ui-corner-all").removeClass("ui-inputfield")
            .removeClass("ui-password").removeClass("ui-button").removeClass(
            "ui-button-text-only").removeClass("ui-inputtext");
    var divError = $(".ui-messages-error");
    divError.removeClass("ui-messages-error ui-corner-all").addClass(
            "alert alert-danger").find(".ui-messages-error-icon").remove();
    $(".ui-messages-error-summary").removeClass("ui-messages-error-summary")
            .prepend("<i class='fas fa-exclamation-triangle fa-lg fa-fw mr-2'></i>");

    divError = $(".ui-messages-info");
    divError.removeClass("ui-messages-info").addClass("alert alert-success")
            .find(".ui-messages-info-icon").remove();
    $(".ui-messages-info-summary").removeClass("ui-messages-info-summary")
            .prepend("<i class='fas fa-check fa-lg fa-fw mr-2'></i>");

    divError = $(".ui-messages-warn");
    divError.removeClass("ui-messages-warn").addClass("alert alert-warning")
            .find(".ui-messages-warn-icon").remove();
    $(".ui-messages-warn-summary").removeClass("ui-messages-warn-summary")
            .prepend("<i class='fas fa-exclamation-triangle fa-lg fa-fw mr-2'></i>");

    $(".ui-selectonemenu-trigger").find(".ui-icon").removeClass(
            "ui-icon ui-icon-triangle-1-s ui-c").addClass(
            "fas fa-caret-down mt-2 mx-auto");
    fixDataTableClass();
    BindToolTips();
    formatInputsValor();
    hideProcessing(0);
}
function formatInputsValor() {
    $("*[data-type='valor'],input[id*='tbNum']").each(function () {
        var ctrTarget = $(this);
        formatInputValor(ctrTarget);
    });
}
function formatInputValor(ctrTarget) {
    /*ctrTarget.attr("maxlength", "16");*/
    ctrTarget.mask(SPMaskBehavior, spOptions);
}
function fixDataTableClass() {

    var wraper = $(".ui-datatable");
    var tables = wraper.find("table");
    tables.addClass("table table-sm table-bordered table-hover");
    tables.find("thead").addClass("thead-gray");
    tables.parent().removeClass("ui-datatable");
    wraper.find(".ui-paginator-prev").html("<i class='fas fa-angle-left'></i>");
    wraper.find(".ui-paginator-next")
            .html("<i class='fas fa-angle-right'></i>");
    wraper.find(".ui-paginator-first").html(
            "<i class='fas fa-angle-double-left'></i>");
    wraper.find(".ui-paginator-last").html(
            "<i class='fas fa-angle-double-right'></i>");
}

function BindToolTips(parent) {
    var targets = null;
    if (parent == null)
        targets = $('[data-toggle="tooltip"]');
    else
        targets = $(parent).find('[data-toggle="tooltip"]');
    targets.tooltip({container: 'body'});
}

// Extended as number function
jQuery.fn.extend({
    valAsNumber: function () {
        var $this = $(this);
        var currentValue = Number($this.val());
        return currentValue;
    }
});

jQuery.fn.tagName = function () {
    return this.prop("tagName");
};

// Extended clean value as number function
jQuery.fn.extend({
    cleanValAsNumber: function () {
        var cleanVal = $(this).tagName() === "DIV" ? $(this).text() : $(this).val();
        cleanVal = cleanVal.replace(/\./g, '');
        cleanVal = cleanVal.replace(/,/g, '.');
        var currentValue = Number(cleanVal);
        return currentValue;
    }
});

// Extended disable function
jQuery.fn.extend({
    disable: function (state) {
        return this.each(function () {
            var $this = $(this);
            if ($this.is('input, button, textarea, select'))
                this.disabled = state;
            else
                $this.toggleClass('disabled', state);
        });
    }
});
function MaskedVal(val) {
    var tempDiv = $("<div>" + val + "</div>");
    tempDiv.mask(SPMaskBehavior, spOptions);
    var result = tempDiv.html();
    tempDiv.remove();
    return result;
}

function MaskedVal2(val) {
    val = val.toString().replace(/\./g, ',');
    var index = val.indexOf(",");
    var decimalPart = "";
    if (index > 0) {
        decimalPart = val.substring(index);
        val = val.substring(0, index);
    }
    
    var tempDiv = $("<div>" + val + "</div>");
    tempDiv.mask(SPMaskBehavior, spOptions);
    var result = tempDiv.html();
    tempDiv.remove();
    result += decimalPart;
    return result;
}
var divStepMenu = null;
var currentWizardStepIndex = 1;
function onmoveWizard(forward) {
    var items = divStepMenu.find(".ui-steps-item");
    console.log(currentWizardStepIndex);
    console.log(items);
    console.log(forward);
    if (forward && currentWizardStepIndex > items.length)
        return;
    else if (!forward && currentWizardStepIndex === 1)
        return;

    forward ? currentWizardStepIndex++ : currentWizardStepIndex--;

    for (var i = 1; i <= items.length; i++)
        divStepMenu.removeClass("step-" + i);

    divStepMenu.addClass("step-" + currentWizardStepIndex);
    items.removeClass("ui-state-highlight").find("a").removeClass("sucess-step");
    for (var index2 = 1; index2 <= currentWizardStepIndex; index2++)
    {
        var item = divStepMenu.find("[id$='stepsMenuItem" + index2 + "']");
        var parent = item.parent();
        parent.addClass("ui-state-highlight");
        if (index2 < currentWizardStepIndex)
            item.addClass("sucess-step");
    }
}

$(function () {
    PrimeFaces.locales['es'] = {closeText: 'Cerrar', prevText: 'Anterior', nextText: 'Siguiente', monthNames: ['Enero', 'Febrero', 'Marzo', 'Abril', 'Mayo', 'Junio', 'Julio', 'Agosto', 'Septiembre', 'Octubre', 'Noviembre', 'Diciembre'], monthNamesShort: ['Ene', 'Feb', 'Mar', 'Abr', 'May', 'Jun', 'Jul', 'Ago', 'Sep', 'Oct', 'Nov', 'Dic'], dayNames: ['Domingo', 'Lunes', 'Martes', 'Miércoles', 'Jueves', 'Viernes', 'Sábado'], dayNamesShort: ['Dom', 'Lun', 'Mar', 'Mie', 'Jue', 'Vie', 'Sab'], dayNamesMin: ['D', 'L', 'M', 'X', 'J', 'V', 'S'], weekHeader: 'Semana', firstDay: 1, isRTL: false, showMonthAfterYear: false, yearSuffix: '', timeOnlyTitle: 'Sólo hora', timeText: 'Tiempo', hourText: 'Hora', minuteText: 'Minuto', secondText: 'Segundo', currentText: 'Fecha actual', ampm: false, month: 'Mes', week: 'Semana', day: 'Día', allDayText: 'Todo el día'};
    removeUIClasess();
    divStepMenu = $("#divStepMenu");
});
