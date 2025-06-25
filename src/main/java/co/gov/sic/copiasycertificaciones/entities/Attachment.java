/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package co.gov.sic.copiasycertificaciones.entities;

/**
 *
 * @author emosquera
 */
public class Attachment {

	private String fileName;
	private String fileExtension;
	private double fileSize;
	private String path;
	private String copyFileName;
	private String copyPath;

	public Attachment() {
	}

	public Attachment(String fileName, String fileExtension, long fileSize1, String path) {

		this.path = path;
		this.fileSize = fileSize1 / 1024 / 1024;
		double x = fileSize1 / 1024;
		x = x / 1024;
		if (fileSize1 <= 1048576) {
			this.fileSize = roundFileSize(x, 3);
		} else {
			this.fileSize = roundFileSize(x, 2);
		}

		this.fileName = fileName;
		this.fileExtension = fileExtension;

	}

	public double roundFileSize(double size, int numDigitis) {
		double result;
		result = size * Math.pow(10, numDigitis);
		result = Math.round(result);
		result = result / Math.pow(10, numDigitis);
		return result;
	}

	/**
	 * @return the fileName
	 */
	public String getFileName() {
		return fileName;
	}

	/**
	 * @param fileName the fileName to set
	 */
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	/**
	 * @return the fileExtension
	 */
	public String getFileExtension() {
		return fileExtension;
	}

	/**
	 * @param fileExtension the fileExtension to set
	 */
	public void setFileExtension(String fileExtension) {
		this.fileExtension = fileExtension;
	}

	/**
	 * @return the fileSize
	 */
	public double getFileSize() {
		return fileSize;
	}

	/**
	 * @param fileSize the fileSize to set
	 */
	public void setFileSize(double fileSize) {
		this.fileSize = fileSize;
	}

	/**
	 * @return the path
	 */
	public String getPath() {
		return path;
	}

	/**
	 * @param path the path to set
	 */
	public void setPath(String path) {
		this.path = path;
	}

	/**
	 * 
	 * @return
	 */
	public String getCopyFileName() {
		return copyFileName;
	}

	/**
	 * 
	 * @param copyFileName
	 */
	public void setCopyFileName(String copyFileName) {
		this.copyFileName = copyFileName;
	}

	/**
	 * 
	 * @return
	 */
	public String getCopyPath() {
		return copyPath;
	}

	/**
	 * 
	 * @param copyPath
	 */
	public void setCopyPath(String copyPath) {
		this.copyPath = copyPath;
	}

}
