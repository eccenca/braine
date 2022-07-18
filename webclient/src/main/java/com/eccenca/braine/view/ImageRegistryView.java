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
import com.eccenca.braine.dao.ImageRegistry;
import com.eccenca.braine.dao.ImageRegistryService;
import com.eccenca.braine.dao.ImageService;
import com.eccenca.braine.dao.PublishedImage;

@Named
@ViewScoped
public class ImageRegistryView implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -7797576003564796840L;

	protected static final Logger logger = LogManager.getLogger();
	
	private static final String IMAGE_REPOSITORY_NAMESPACE = "https://data.braine-project.eu/itops/docker/registry/";
	
	@Inject
    private ImageRegistryService service;
	@Inject
    private ImageService imageService;
	@Inject
    private DockerService dockerService;
	
	private Image selectedImage = null;
	private String selectedRegistry = null;
	
	private Map<String, ImageRegistry> imageRegistryMap = new HashMap<String, ImageRegistry>();
	private Map<String, List<PublishedImage>> imageRegistryImagesMap = new HashMap<String, List<PublishedImage>>();
	private List<ImageRegistry> imageRegistryList;
	
	public ImageRegistryView() {
	}
	
	@PostConstruct
	public void init() {
		imageRegistryList = service.list();
		for(ImageRegistry image : imageRegistryList) {
			imageRegistryMap.put(image.getUri(), image);
		}
	}
		
	public void onRowEdit(RowEditEvent<ImageRegistry> event) {
		ImageRegistry image = event.getObject();
		service.update(image);
        FacesMessage msg = new FacesMessage("Image Registry Updated", image.getName());
        FacesContext.getCurrentInstance().addMessage(null, msg);
    }
	
	public void remove(String imageRegistryUri) {
		try {
			service.delete(imageRegistryUri);
			ImageRegistry image = imageRegistryMap.get(imageRegistryUri);
			imageRegistryList.remove(image);
			imageRegistryMap.remove(imageRegistryUri);
			FacesMessage message = new FacesMessage("Successful", image.getName() + " was removed.");
	        FacesContext.getCurrentInstance().addMessage(null, message);
		} catch (Exception e) {
			FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Error removing Image Registry: " + imageRegistryUri);
	        FacesContext.getCurrentInstance().addMessage(null, message);
	        logger.error(e);
		}
	}
	
	public void remove(String imageRegistryUri, PublishedImage image) {
		try { // http://127.0.0.1:2375
			ImageRegistry registry = imageRegistryMap.get(imageRegistryUri);
			dockerService.remove(image.getRepoId());
			imageRegistryImagesMap.get(imageRegistryUri).remove(image); // removing from the registry list
			FacesMessage message = new FacesMessage("Successful",image.getRepoId() + " was removed from " + registry.getName() + ".");
	        FacesContext.getCurrentInstance().addMessage(null, message);
		} catch (Exception e) {
			FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Error removing Image: " + image.getBeautifulRepoId() + " " + e.getMessage());
	        FacesContext.getCurrentInstance().addMessage(null, message);
	        logger.error(e);
		}
	}
	
	public void update(String imageRegistryUri) {
		try {
			ImageRegistry image = imageRegistryMap.get(imageRegistryUri);
			service.update(image);
			FacesMessage message = new FacesMessage("Successful", "Image " + image.getName() + " was updated.");
	        FacesContext.getCurrentInstance().addMessage(null, message);
		} catch (Exception e) {
			FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Error updating Image Registry: " + imageRegistryUri);
	        FacesContext.getCurrentInstance().addMessage(null, message);
	        logger.error(e);
		}
	}
	
	public List<ImageRegistry> getList() {
		return imageRegistryList;
	}
	
	public List<PublishedImage> list(String imageRegistryUri) {
		ImageRegistry imageRegistry = imageRegistryMap.get(imageRegistryUri);
		List<PublishedImage> publishedImageList = imageRegistryImagesMap.get(imageRegistryUri);
		if(publishedImageList == null) {
			try {
				publishedImageList = new ArrayList<PublishedImage>();
				List<com.github.dockerjava.api.model.Image> repoImages = dockerService.getList();
				Map<String, PublishedImage> imageRegistryImageMap = new HashMap<String, PublishedImage>();
				String registryID = getRegistryPrefix(imageRegistry.getNetworkAddress());
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
				String message = "Error listing images from registry " + imageRegistry.getName() + ": " + e.getMessage();
				FacesMessage faceMessage = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", message);
		        FacesContext.getCurrentInstance().addMessage(null, faceMessage);
		        PrimeFaces.current().ajax().update("contentPanel:msgs");
		        logger.error(message, e);
			}			
			imageRegistryImagesMap.put(imageRegistryUri, publishedImageList);
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

	private String getRegistryPrefix(String imageRegistryUri) throws MalformedURLException {
		if(imageRegistryUri == null || imageRegistryUri.isEmpty()) {
			return imageRegistryUri;
		}
		URL url = new URL(imageRegistryUri);
		String path = url.getPath();
		if(path != null) {
			return url.getAuthority() + path;
		}
		return url.getAuthority();
	}
	
	public void setList(List<ImageRegistry> imageRegistryList) {
		this.imageRegistryList = imageRegistryList;
	}
	
	public void newRegistry() {
		Integer i = 0;
		String namespace = ImageRegistryView.IMAGE_REPOSITORY_NAMESPACE;
		String imageURI = namespace + i;
		if(imageRegistryMap != null) {
			while(imageRegistryMap.containsKey(namespace + i)) { i++; }
			imageURI = namespace + i;
		} else {
			imageRegistryMap = new HashMap<String, ImageRegistry>();
		}
		ImageRegistry imageRegistry = new ImageRegistry("Image Registry " + i);
		imageRegistry.setUri(imageURI);
		imageRegistryMap.put(imageURI, imageRegistry);
		imageRegistryList.add(imageRegistry);
		service.insert(imageRegistry);
	}
	
	public void selectImage(String imageRegistry) {
        this.selectedRegistry = imageRegistry;
		Map<String, Object> options = new HashMap<>();
        options.put("modal", true);
        options.put("minWidth", "400");
        options.put("minHeight", "300");
        options.put("headerElement", "customheader");
        options.put("widgetVar", "imageDeploymentDialog");
        options.put("id", "imageDeploymentDialog");
        PrimeFaces.current().dialog().openDynamic("imageDeploymentDialog", options, null);
	}
	
	public void setSelectedImage(Image selectedImage) {
		this.selectedImage = selectedImage;
	}
	
	public Image getSelectedImage() {
		return selectedImage;
	}
	
	public void deploy() {
		try {
			ImageRegistry imageRegistry = imageRegistryMap.get(selectedRegistry);
			dockerService.deploy(getRegistryPrefix(imageRegistry.getNetworkAddress()), selectedImage);
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