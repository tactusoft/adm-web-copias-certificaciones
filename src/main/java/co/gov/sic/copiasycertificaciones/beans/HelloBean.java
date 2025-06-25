package co.gov.sic.copiasycertificaciones.beans;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Named;

@Named
@RequestScoped
public class HelloBean {

	private String name;
	private String message;

	public void sayHello() {
		message = "Hola " + name + " desde PrimeFaces 14 + Jakarta EE 10";
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

}