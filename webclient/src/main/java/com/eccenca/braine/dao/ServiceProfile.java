package com.eccenca.braine.dao;

/**
 * 
 * @author edgardmarx
 *
 */
public class ServiceProfile extends Resource {

	/**
	 * static
	 */
	private static final long serialVersionUID = 7178756098128692843L;
	
	public static final String LABEL_ATTR = "LABEL_ATTR";
	public static final String DESCRIPTION_ATTR = "DESCRIPTION_ATTR";
	public static final String MANIFEST_ATTR = "MANIFEST_ATTR";
	public static final String VARIABLES_ATTR = "VARIABLES_ATTR";
	public static final String PROCESSING_UNIT_ATTR = "PROCESSING_UNIT_ATTR";
	public static final String NUMBER_OF_CPUS_ATTR = "NUMBER_OF_CPUS";
	public static final String NUMBER_OF_PODS_ATTR = "NUMBER_OF_PODS";
	public static final String STORAGE_ATTR = "STORAGE_ATTR";
	public static final String NUMBER_OF_TENANTS_ATTR = "NUMBER_OF_TENANTS_ATTR";
	public static final String NUMBER_OF_SLICES_ATTR = "NUMBER_OF_SLICES_ATTR";
	public static final String USE_ENCRYPTION = "USE_ENCRYPTION";

	/**
	 * protected
	 */
	protected String manifest;
	protected String variables;
	protected String variableFile;
	protected Integer slices;
	protected Integer tenants;
	protected Integer storage;
	protected String processingUnit;
	protected Integer numberOfCPUs;
	protected Integer numberOfPODs;
	protected Boolean useEncryption;
	
	public Integer getStorage() {
		return storage;
	}

	public void setStorage(Integer storage) {
		this.storage = storage;
	}

	public String getProcessingUnit() {
		return processingUnit;
	}

	public void setProcessingUnit(String processingUnit) {
		this.processingUnit = processingUnit;
	}

	public Integer getNumberOfCPUs() {
		return numberOfCPUs;
	}

	public void setNumberOfCPUs(Integer numberOfCPUs) {
		this.numberOfCPUs = numberOfCPUs;
	}

	public Integer getNumberOfPODs() {
		return numberOfPODs;
	}

	public void setNumberOfPODs(Integer numberOfPODs) {
		this.numberOfPODs = numberOfPODs;
	}
	
	public ServiceProfile(String name) {
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

	public String getVariableFile() {
		return variableFile;
	}

	public void setVariableFile(String variableFile) {
		this.variableFile = variableFile;
	}

	public Integer getSlices() {
		return slices;
	}

	public void setSlices(Integer slices) {
		this.slices = slices;
	}

	public Integer getTenants() {
		return tenants;
	}

	public void setTenants(Integer tentants) {
		this.tenants = tentants;
	}

	public Boolean getUseEncryption() {
		return useEncryption;
	}

	public void setUseEncryption(Boolean useEncryption) {
		this.useEncryption = useEncryption;
	}
}
