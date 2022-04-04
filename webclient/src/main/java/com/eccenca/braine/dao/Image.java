package com.eccenca.braine.dao;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author edgardmarx
 *
 */
public class Image extends Resource {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7178756098128692843L;
	
	public static final String LABEL_ATTR = "LABEL_ATTR";
	public static final String DESCRIPTION_ATTR = "DESCRIPTION_ATTR";
	public static final String MANIFEST_ATTR = "MANIFEST_ATTR";
	public static final String MANIFEST_FILE_ATTR = "MANIFEST_FILE_ATTR";
	public static final String VARIABLE_ATTR = "VARIABLE_ATTR";
	public static final String VARIABLE_FILE_ATTR = "VARIABLE_FILE_ATTR";
	public static final String VARIABLES_ATTR = "VARIABLES_ATTR";
	public static final String FILES_ATTR = "FILES_ATTR";

	protected String manifest;
	protected String manifestFile;
	protected List<String> files = new ArrayList<String>();
	protected String variables;
	protected String variableFile;
	
	public Image(String name) {
		super(name);
	}
	
	public List<String> getFiles() {
		return files;
	}
	
	public void setFiles(List<String> files) {
		this.files = files;
	}
	
	public String getVariables() {
		return variables;
	}
	
	public void setVariables(String variables) {
		this.variables = variables;
	}
	
	public String getManifest() {
		return manifest;
	}
	
	public void setManifest(String manifest) {
		this.manifest = manifest;
	}
	
	public String getVariableFile() {
		return variableFile;
	}
	
	public void setVariableFile(String variableFile) {
		this.variableFile = variableFile;
	}
	
	public String getManifestFile() {
		return manifestFile;
	}
	
	public void setManifestFile(String manifestFile) {
		this.manifestFile = manifestFile;
	}
}
