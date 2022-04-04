package com.eccenca.braine.view;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.faces.model.SelectItemGroup;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.event.FilesUploadEvent;
import org.primefaces.event.RowEditEvent;
import org.primefaces.model.file.UploadedFile;

import com.eccenca.braine.dao.Image;
import com.eccenca.braine.dao.ImageService;

@Named("imageView")
@ViewScoped
public class ImageView implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -7797576003564796840L;
	
	private static final String IMAGE_NAMESPACE = "https://data.braine-project.eu/itops/docker/image/";

	protected static final Logger logger = LogManager.getLogger();
	
	@Inject
    private ImageService service;
	
	private Map<String, Image> imageMap = new HashMap<String, Image>();
	private List<Image> imageList;
	private Integer tab = 0;
	
	public ImageView() {
	}
	
	@PostConstruct
	public void init() {
		imageList = service.list();
		for(Image image : imageList) {
			imageMap.put(image.getUri(), image);
		}
		loadManifests(imageList);
	}
	
	private void loadManifests(List<Image> imageList) {
		for(Image image : imageList) {
			loadManifest(image);
			loadVariables(image);
		}
	}
	
	public Integer getTab() {
		return tab;
	}
	
	public void setTab(Integer tab) {
		this.tab = tab;
	}
	
	private void loadManifest(Image image) {
		String manifestFile = image.getManifestFile();
		try {
			if(manifestFile != null && !manifestFile.isEmpty()) {
				String encoding = getEncoding(service.getFilePath(image.getUri(), manifestFile));
				if(encoding != null && encoding.equals("UTF8")) {
					image.setManifest(service.getFileContent(service.getFilePath(image.getUri(), manifestFile)));
				} else {
					image.setManifest(" ENCODING NOT SUPPORTED ");
				}
			}	
		} catch (IOException e) {
			image.setManifest(" File could not be open. ");
		}
	}
	
	private void loadVariables(Image image) {
		String variableFile = image.getVariableFile();
		try {
			if(variableFile != null && !variableFile.isEmpty()) {
				String encoding = getEncoding(service.getFilePath(image.getUri(), variableFile));
				if(encoding != null && encoding.equals("UTF8")) {
					image.setVariables(service.getFileContent(service.getFilePath(image.getUri(), variableFile)));
				} else {
					image.setVariables(" ENCODING NOT SUPPORTED ");
				}
			}	
		} catch (IOException e) {
			image.setVariables(" File could not be open. ");
		}
	}
	
	public void handleFilesUpload(FilesUploadEvent event) {
		String imageURI = String.valueOf(event.getComponent().getAttributes().get("image"));
		Image image = imageMap.get(imageURI);
		List<String> imageFiles = image.getFiles();
		for(UploadedFile file :  event.getFiles().getFiles()) {
			try {
				service.save(image.getUri(), file);
				imageFiles.add(file.getFileName());
				update(imageURI);
			} catch (IOException e) {
				logger.error(e);
				FacesMessage message = new FacesMessage("Error", "An error occurred while uploading the file: " + file.getFileName());
			    FacesContext.getCurrentInstance().addMessage(null, message);
			    return;
			}
		}
        FacesMessage message = new FacesMessage("Successful", event.getFiles().getFiles().get(0).getFileName() + " is uploaded.");
        FacesContext.getCurrentInstance().addMessage(null, message);
    }
	
	public void handleFileUpload(FileUploadEvent event) {
		String imageURI = String.valueOf(event.getComponent().getAttributes().get("image"));
		Image image = imageMap.get(imageURI);
		List<String> imageFiles = image.getFiles();
		UploadedFile file =  event.getFile();
		try {
			service.save(image.getUri(), file);
			imageFiles.add(file.getFileName());
			update(imageURI);
		} catch (IOException e) {
			logger.error(e);
			FacesMessage message = new FacesMessage("Error", "An error occurred while uploading the file: " + file.getFileName());
		    FacesContext.getCurrentInstance().addMessage(null, message);
		    return;
		}
        FacesMessage message = new FacesMessage("Successful", event.getFile().getFileName() + " is uploaded.");
        FacesContext.getCurrentInstance().addMessage(null, message);
    }
	
	public void onRowEdit(RowEditEvent<Image> event) {
		Image image = event.getObject();
		service.update(image);
        FacesMessage msg = new FacesMessage("Image Updated", image.getName());
        FacesContext.getCurrentInstance().addMessage(null, msg);
    }
	
	public void remove(String imageUri) {
		try {
			Image image = imageMap.get(imageUri);
			for(String fileUri : image.getFiles()) {
				service.delete(image.getUri(), fileUri);
			}
			imageList.remove(image);
			imageMap.remove(imageUri);
			service.delete(imageUri);
			FacesMessage message = new FacesMessage("Successful", image.getName() + " was removed.");
	        FacesContext.getCurrentInstance().addMessage(null, message);
		} catch (Exception e) {
			logger.error(e);
		}
	}
	
	public void remove(String imageUri, String fileUri) {
		try {
			Image image = imageMap.get(imageUri);
			image.getFiles().remove(fileUri);
			service.delete(image.getUri(), fileUri);
			service.update(image);
			FacesMessage message = new FacesMessage("Successful", fileUri + " was removed from " + image.getName() + ".");
	        FacesContext.getCurrentInstance().addMessage(null, message);
		} catch (Exception e) {
			logger.error(e);
		}
	}
	
	public List<SelectItem> getImageFiles(String imageUri) {
		Image image = imageMap.get(imageUri);
		List<SelectItem> fileList = new ArrayList<SelectItem>();
		String manifestFile = null;
		for(String file : image.getFiles()) {
			SelectItem fileOption = null;
			if(file.endsWith(".zip")) {
				fileOption = new SelectItemGroup(file);
				List<String> zipInternalFiles = getZipFileEntries(service.getFilePath(image.getUri(), file));
				for(String zipFileName : zipInternalFiles) {
					if(!zipFileName.contains("__MACOSX/")) {
						fileOption = new SelectItem(zipFileName);
						fileList.add(fileOption);
						if(zipFileName.endsWith("Dockerfile")) {
							manifestFile = zipFileName;
						}
					}
				}
			} else {
				fileOption = new SelectItem(file);
				fileList.add(fileOption);
				if(file.endsWith("Dockerfile")) {
					manifestFile = file;
				}
			}
			if((image.getManifestFile() == null && manifestFile != null) && 
					(image.getManifest() == null || image.getManifest().isEmpty())) {
				loadManifest(image);
			}
		}
        return fileList;
	}
	
	public boolean isManifestEditMode(String imageUri) {
		Image image = imageMap.get(imageUri);
		if(image == null) return false;
		String manifestFile = image.getManifestFile();
		return manifestFile == null || manifestFile.isEmpty();
	}
	
	public boolean isVariablesEditMode(String imageUri) {
		Image image = imageMap.get(imageUri);
		if(image == null) return false;
		String variableFile = image.getVariableFile();
		return variableFile == null || variableFile.isEmpty();
	}
	
	public void setManifestFile(String imageUri, String manifestFile) {
		Image image = imageMap.get(imageUri);
		if(!manifestFile.equals("")) {
			image.setManifestFile(manifestFile);
			loadManifest(image);
			update(imageUri);
		} else {
			image.setManifestFile("");
			image.setManifest(null);
		}
	}
	
	public void setVariablesFile(String imageUri, String variablesFile) {
		Image image = imageMap.get(imageUri);
		if(!variablesFile.equals("")) {
			image.setVariableFile(variablesFile);
			loadVariables(image);
			update(imageUri);
		} else {
			image.setVariableFile("");
			image.setVariables(null);
		}
	}
	
	private String getEncoding(String filePath) throws IOException {
		if(filePath.contains(".zip/")) {
        	String[] subPaths = filePath.split(".zip/");
        	String zipFilePath = subPaths[0] + ".zip";
        	String insideEntryPath = subPaths[1];
	            // open a zip file for reading
	        try(ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFilePath));) {
	            // get an enumeration of the ZIP file entries
	            ZipEntry zipEntry = zis.getNextEntry();
	            while (zipEntry != null) {
	                if (!zipEntry.isDirectory() && zipEntry.getName().equals(insideEntryPath)) {
	                	try(InputStreamReader r = new InputStreamReader(zis);) {
	        				return r.getEncoding();
	        			}
	                } 
	                zipEntry = zis.getNextEntry();
	           }
	        }
	        return null;
		} else {
			File in =  new File(filePath);
			try(InputStreamReader r = new InputStreamReader(new FileInputStream(in));) {
				return r.getEncoding();
			}
		}
	}	

	public List<String> getZipFileEntries(String zipFilePath) {
		List<String> entries = new ArrayList<String>();
        try {
            // open a zip file for reading
        	File file = new File(zipFilePath);
            try(ZipFile zipFile = new ZipFile(file);) {
            // get an enumeration of the ZIP file entries
	            Enumeration<? extends ZipEntry> e = zipFile.entries();
	            while (e.hasMoreElements()) {
	                ZipEntry entry = e.nextElement();
	                // get the name of the entry
	                String entryName = entry.getName();
	                if(!entry.isDirectory()) {
	                	entries.add(file.getName() + "/" +entryName);
	                }
	            }
            }
        }
        catch (IOException e) {
        	logger.error(e);
        }
        return entries;
	}
	
	public void update(String imageUri) {
		try {
			Image image = imageMap.get(imageUri);
			service.update(image);
			FacesMessage message = new FacesMessage("Successful", "Image " + image.getName() + " was updated.");
	        FacesContext.getCurrentInstance().addMessage(null, message);
		} catch (Exception e) {
			logger.error(e);
		}
	}
	
	public List<Image> getList() {
		return imageList;
	}
	
	public void setList(List<Image> imageList) {
		this.imageList = imageList;
	}
	
	public void newImage() {
		Integer i = 0;
		String namespace = ImageView.IMAGE_NAMESPACE;
		String imageURI = namespace + i;
		if(imageMap != null) {
			while(imageMap.containsKey(namespace + i)) { i++; }
			imageURI = namespace + i;
		} else {
			imageMap = new HashMap<String, Image>();
		}
		Image image = new Image("Image " + i);
		image.setUri(imageURI);
		imageMap.put(imageURI, image);
		imageList.add(image);
		service.insert(image);
	}
}