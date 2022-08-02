package com.eccenca.braine.view;

import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.primefaces.PrimeFaces;
import org.primefaces.event.RowEditEvent;

import com.eccenca.braine.dao.DockerService;
import com.eccenca.braine.dao.Image;
import com.eccenca.braine.dao.ImageService;
import com.eccenca.braine.dao.PublishedImage;
import com.eccenca.braine.dao.WorkflowRegistry;
import com.eccenca.braine.dao.WorkflowRegistryService;

@Named
@ViewScoped
public class WorkflowRegistryView implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -7797576003564796840L;

	protected static final Logger logger = LogManager.getLogger();
	
	private static final String WORKFLOW_REPOSITORY_NAMESPACE = "https://data.braine-project.eu/itops/workflow/registry/";
	
	@Inject
    private WorkflowRegistryService service;
	@Inject
    private ImageService imageService;
	@Inject
    private DockerService dockerService;
	
	private Image selectedImage = null;
	private String selectedRegistry = null;
	
	private Map<String, WorkflowRegistry> imageRegistryMap = new HashMap<String, WorkflowRegistry>();
	private Map<String, List<PublishedImage>> imageRegistryImagesMap = new HashMap<String, List<PublishedImage>>();
	private List<WorkflowRegistry> workflowRegistryList;
	
	public WorkflowRegistryView() {
	}
	
	@PostConstruct
	public void init() {
		workflowRegistryList = service.list();
		for(WorkflowRegistry image : workflowRegistryList) {
			imageRegistryMap.put(image.getUri(), image);
		}
	}
		
	public void onRowEdit(RowEditEvent<WorkflowRegistry> event) {
		WorkflowRegistry image = event.getObject();
		service.update(image);
        FacesMessage msg = new FacesMessage("Workflow Registry Updated", image.getName());
        FacesContext.getCurrentInstance().addMessage(null, msg);
    }
	
	public void remove(String workflowRegistryUri) {
		try {
			service.delete(workflowRegistryUri);
			WorkflowRegistry image = imageRegistryMap.get(workflowRegistryUri);
			workflowRegistryList.remove(image);
			imageRegistryMap.remove(workflowRegistryUri);
			FacesMessage message = new FacesMessage("Successful", image.getName() + " was removed.");
	        FacesContext.getCurrentInstance().addMessage(null, message);
		} catch (Exception e) {
			FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Error removing Workflow Registry: " + workflowRegistryUri);
	        FacesContext.getCurrentInstance().addMessage(null, message);
	        logger.error(e);
		}
	}
	
	public void remove(String imageRegistryUri, PublishedImage image) {
		try { // http://127.0.0.1:2375
			WorkflowRegistry registry = imageRegistryMap.get(imageRegistryUri);
			dockerService.remove(image.getRepoId());
			imageRegistryImagesMap.get(imageRegistryUri).remove(image); // removing from the registry list
			FacesMessage message = new FacesMessage("Successful",image.getRepoId() + " was removed from " + registry.getName() + ".");
	        FacesContext.getCurrentInstance().addMessage(null, message);
		} catch (Exception e) {
			FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Error removing Workflow: " + image.getBeautifulRepoId() + " " + e.getMessage());
	        FacesContext.getCurrentInstance().addMessage(null, message);
	        logger.error(e);
		}
	}
	
	public void update(String registryUri) {
		try {
			WorkflowRegistry image = imageRegistryMap.get(registryUri);
			service.update(image);
			FacesMessage message = new FacesMessage("Successful", "Image " + image.getName() + " was updated.");
	        FacesContext.getCurrentInstance().addMessage(null, message);
		} catch (Exception e) {
			FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Error updating Workflow Registry: " + registryUri);
	        FacesContext.getCurrentInstance().addMessage(null, message);
	        logger.error(e);
		}
	}
	
	public List<WorkflowRegistry> getList() {
		return workflowRegistryList;
	}
	
	public List<PublishedImage> list(String registryUri) {
		WorkflowRegistry workflowRegistry = imageRegistryMap.get(registryUri);
		List<PublishedImage> publishedImageList = imageRegistryImagesMap.get(registryUri);
		if(publishedImageList == null) {
			try {
				publishedImageList = new ArrayList<PublishedImage>();
				List<com.github.dockerjava.api.model.Image> repoImages = dockerService.getList();
				Map<String, PublishedImage> imageRegistryImageMap = new HashMap<String, PublishedImage>();
				String registryID = getRegistryPrefix(workflowRegistry.getNetworkAddress());
				for(com.github.dockerjava.api.model.Image repoImage : repoImages) {
					if(isLocal(registryID) || contains(repoImage.getRepoTags(), registryID)) {
						PublishedImage publishedImage = new PublishedImage(repoImage);
						Map<String, String> labels = publishedImage.getRepoLabels();
						if(labels != null) {
							String braineID = labels.get("BRAINE_ID");
							if(braineID != null) {
								Image image = imageService.getImage(braineID);
								publishedImage.setImage(image);
								imageRegistryImageMap.put(braineID, publishedImage);
							}
						}
						publishedImageList.add(publishedImage);
					}
				}
			} catch (Exception e) {
				String message = "Error listing workflow from registry " + workflowRegistry.getName() + ": " + e.getMessage();
				FacesMessage faceMessage = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", message);
		        FacesContext.getCurrentInstance().addMessage(null, faceMessage);
		        PrimeFaces.current().ajax().update("contentPanel:msgs");
		        logger.error(message, e);
			}			
			imageRegistryImagesMap.put(registryUri, publishedImageList);
		}
		return publishedImageList;
	}
	
	private boolean isLocal(String registryID) {
		return registryID.contains("127.0.0.1") || registryID.contains("localhost");
	}

	private boolean contains(String[] repoTags, String registryPrefix) {
		if(repoTags != null) {
			for(String repoTag : repoTags) {
				if(repoTag.startsWith(registryPrefix)) {
					return true;
				}
			}
		}
		return false;
	}

	private String getRegistryPrefix(String registryUri) throws MalformedURLException {
		if(registryUri == null || registryUri.isEmpty()) {
			return registryUri;
		}
		URL url = new URL(registryUri);
		String path = url.getPath();
		if(path != null) {
			return url.getAuthority() + path;
		}
		return url.getAuthority();
	}
	
	public void setList(List<WorkflowRegistry> workflowRegistryList) {
		this.workflowRegistryList = workflowRegistryList;
	}
	
	public void newRegistry() {
		Integer i = 0;
		String namespace = WorkflowRegistryView.WORKFLOW_REPOSITORY_NAMESPACE;
		String imageURI = namespace + i;
		if(imageRegistryMap != null) {
			while(imageRegistryMap.containsKey(namespace + i)) { i++; }
			imageURI = namespace + i;
		} else {
			imageRegistryMap = new HashMap<String, WorkflowRegistry>();
		}
		WorkflowRegistry imageRegistry = new WorkflowRegistry("Workflow Registry " + i);
		imageRegistry.setUri(imageURI);
		imageRegistryMap.put(imageURI, imageRegistry);
		workflowRegistryList.add(imageRegistry);
		service.insert(imageRegistry);
	}
	
	public void selectWorkflow(String workflowRegistry) {
        this.selectedRegistry = workflowRegistry;
		Map<String, Object> options = new HashMap<>();
        options.put("modal", true);
        options.put("minWidth", "400");
        options.put("minHeight", "300");
        options.put("headerElement", "customheader");
        options.put("widgetVar", "workflowDeploymentDialog");
        options.put("id", "workflowDeploymentDialog");
        PrimeFaces.current().dialog().openDynamic("workflowDeploymentDialog", options, null);
	}
	
	public void setSelectedWorkflow(Image selectedImage) {
		this.selectedImage = selectedImage;
	}
	
	public Image getSelectedWorkflow() {
		return selectedImage;
	}
	
	public void deploy() {
		try {
			WorkflowRegistry workflowRegistry = imageRegistryMap.get(selectedRegistry);
			dockerService.deploy(getRegistryPrefix(workflowRegistry.getNetworkAddress()), selectedImage);
			String imageUri = selectedImage.getUri();
			com.github.dockerjava.api.model.Image repoImage = dockerService.getImage(imageUri);
			PublishedImage runningPublishedImage = imageRegistryImagesMap.get(selectedRegistry).stream()
					  .filter(image -> imageUri.equals(image.getUri()))
					  .findAny()
					  .orElse(null);
			if(runningPublishedImage != null) { // update repository image
				runningPublishedImage.setRepoImage(repoImage);
			} else { // create published image metadata
				PublishedImage publishedImage = new PublishedImage(repoImage);
				publishedImage.setImage(selectedImage);
				List<PublishedImage> registryPublishedImages = imageRegistryImagesMap.get(selectedRegistry);
				registryPublishedImages.add(0, publishedImage);
			}
			FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO, "Success", "Image " + selectedImage.getName() + " deployed.");
	        FacesContext.getCurrentInstance().addMessage(null, message);
		} catch (Exception e) {
			FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Error deploying image " + selectedImage.getName() + ": " + e.getMessage());
	        FacesContext.getCurrentInstance().addMessage(null, message);
	        logger.error(e);
		}
		PrimeFaces.current().dialog().closeDynamic(null);
	}
}