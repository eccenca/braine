package com.eccenca.braine.dao;

/**
 * 
 * @author edgardmarx
 *
 */
public class Workflow extends Resource {

	/**
	 * static
	 */
	private static final long serialVersionUID = 7178756098128692843L;
	
	public static final String LABEL_ATTR = "LABEL_ATTR";
	public static final String DESCRIPTION_ATTR = "DESCRIPTION_ATTR";
	public static final String MANIFEST_ATTR = "MANIFEST_ATTR";
	public static final String VARIABLES_ATTR = "VARIABLES_ATTR";

	/**
	 * protected
	 */
	protected String manifest;
	protected String variables;
	protected String description;
	
	public Workflow(String name) {
		super(name);
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
	
	@Override
	public String getDescription() {
		return super.getDescription();
	}
	
	@Override
	public void setDescription(String description) {
		super.setDescription(description);
	}

}
