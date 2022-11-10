package com.eccenca.braine.view;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
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
import org.springframework.beans.factory.annotation.Value;

import com.eccenca.braine.dao.Deployment;
import com.eccenca.braine.dao.KubernetesService;
import com.eccenca.braine.dao.Pod;
import com.eccenca.braine.dao.PublishedService;
import com.eccenca.braine.dao.ServiceProfile;
import com.eccenca.braine.dao.ServiceProfileService;
import com.eccenca.braine.dao.ServiceRegistry;
import com.eccenca.braine.dao.ServiceRegistryService;
import com.eccenca.braine.dao.Statefulset;

import io.kubernetes.client.openapi.models.V1Deployment;
import io.kubernetes.client.openapi.models.V1Pod;
import io.kubernetes.client.openapi.models.V1Service;
import io.kubernetes.client.openapi.models.V1StatefulSet;

@Named
@ViewScoped
public class ServiceRegistryView implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -7797576003564796840L;

	protected static final Logger logger = LogManager.getLogger();
	
	private static final String IMAGE_REPOSITORY_NAMESPACE = "https://data.braine-project.eu/itops/service/registry/";
	
	@Inject
    private ServiceRegistryService service;
	@Inject
    private ServiceProfileService profileService;
	@Inject
    private KubernetesService kubeService;
	
	@Value("${kube.config:#{null}}")
	private String configFilePath;
	
	private File config = null;
	
	private ServiceProfile selectedProfile = null;
	private String selectedRegistry = null;
	
	private Map<String, ServiceRegistry> serviceRegistryMap = new HashMap<String, ServiceRegistry>();
	private Map<String, List<String>> serviceRegistryAppMap = new HashMap<String, List<String>>();
	private Map<String, List<PublishedService>> serviceRegistryServiceMap = new HashMap<String, List<PublishedService>>();
	private List<ServiceRegistry> serviceRegistryList;
	private List<ServiceRegistry> localServiceRegistryList;
	
	public ServiceRegistryView() {
	}
	
	@PostConstruct
	public void init() {
		serviceRegistryList = service.list();
		for(ServiceRegistry service : serviceRegistryList) {
			serviceRegistryMap.put(service.getUri(), service);
		}
		if(configFilePath != null) {
			String userHomeDir = System.getProperty("user.home");
			config = new File(userHomeDir + File.separator + configFilePath);
			if(config != null && config.exists()) {
				localServiceRegistryList = kubeService.getRegistryList(config);
				for(ServiceRegistry service : localServiceRegistryList) {
					serviceRegistryMap.put(service.getName(), service);
				}
			}
		}
	}
		
	public void onRowEdit(RowEditEvent<ServiceRegistry> event) {
		ServiceRegistry serviceRegistry = event.getObject();
		service.update(serviceRegistry);
        FacesMessage msg = new FacesMessage("Service Registry Updated", serviceRegistry.getName());
        FacesContext.getCurrentInstance().addMessage(null, msg);
    }
	
	public void remove(String serviceRegistryUri) {
		try {
			service.delete(serviceRegistryUri);
			ServiceRegistry serviceRegistry = serviceRegistryMap.get(serviceRegistryUri);
			serviceRegistryList.remove(serviceRegistry);
			serviceRegistryMap.remove(serviceRegistryUri);
			FacesMessage message = new FacesMessage("Successful", serviceRegistry.getName() + " was removed.");
	        FacesContext.getCurrentInstance().addMessage(null, message);
		} catch (Exception e) {
			FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Error removing Service Registry: " + serviceRegistryUri);
	        FacesContext.getCurrentInstance().addMessage(null, message);
	        logger.error(e);
		}
	}
	
	public void removeApp(String serviceRegistryUri, String appName) {
		try { // http://127.0.0.1:2375
			ServiceRegistry registry = serviceRegistryMap.get(serviceRegistryUri);
			kubeService.removeApp(registry.getConfig(), "app", appName);
			serviceRegistryAppMap.get(serviceRegistryUri).remove(appName); // removing from the registry list
			FacesMessage message = new FacesMessage("Successful", appName + " was removed from " + registry.getName() + ".");
	        FacesContext.getCurrentInstance().addMessage(null, message);
		} catch (Exception e) {
			FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Error removing application: " + appName + " " + e.getMessage());
	        FacesContext.getCurrentInstance().addMessage(null, message);
	        logger.error(e);
		}
	}
	
	public void update(String serviceRegistryUri) {
		try {
			ServiceRegistry serviceRegistry = serviceRegistryMap.get(serviceRegistryUri);
			service.update(serviceRegistry);
			list(serviceRegistryUri);
			FacesMessage message = new FacesMessage("Successful", "Service " + serviceRegistry.getName() + " was updated.");
	        FacesContext.getCurrentInstance().addMessage(null, message);
		} catch (Exception e) {
			FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Error updating Service Registry: " + serviceRegistryUri);
	        FacesContext.getCurrentInstance().addMessage(null, message);
	        logger.error(e);
		}
	}
	
	public List<ServiceRegistry> getList() {
		return serviceRegistryList;
	}
	
	public List<ServiceRegistry> getLocalList() {
		return localServiceRegistryList;
	}
	
	public List<PublishedService> listServices(String serviceRegistryUri, String appName) {
		ServiceRegistry serviceRegistry = serviceRegistryMap.get(serviceRegistryUri);
		List<PublishedService> publishedServiceList = serviceRegistryServiceMap.get(serviceRegistryUri);
		String serviceRegistryConfig = serviceRegistry.getConfig();
		if(publishedServiceList == null && serviceRegistryConfig != null) {
			publishedServiceList = new ArrayList<PublishedService>();
			try {
				List<V1Service> repoServices = kubeService.getServiceList(serviceRegistryConfig, "app", appName);
				Map<String, PublishedService> serviceRegistryServiceMap = new HashMap<String, PublishedService>();
				for(V1Service repoImage : repoServices) {
					PublishedService publishedService = new PublishedService(repoImage);
					Map<String, String> labels = publishedService.getRepoLabels();
					if(labels != null) {
						String braineID = labels.get("BRAINE_ID");
						if(braineID != null) {
							ServiceProfile profile = profileService.get(braineID);
							publishedService.setService(profile);
							serviceRegistryServiceMap.put(braineID, publishedService);
						}
					}
					publishedServiceList.add(publishedService);
				}
			} catch (Exception e) {
				String message = "Error listing services from registry " + serviceRegistry.getName() + ": " + e.getMessage();
				FacesMessage faceMessage = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", message);
		        FacesContext.getCurrentInstance().addMessage(null, faceMessage);
		        logger.error(message, e);
			}
		} else if(publishedServiceList == null){
			publishedServiceList = new ArrayList<PublishedService>();
		}
		serviceRegistryServiceMap.put(serviceRegistryUri, publishedServiceList);
		return publishedServiceList;
	}
	
	public List<Deployment> listDeployments(String serviceRegistryUri, String appLabel) {
		ServiceRegistry serviceRegistry = serviceRegistryMap.get(serviceRegistryUri);
		List<Deployment> deployments = new ArrayList<Deployment>();
		try {
			String serviceRegistryConfig = serviceRegistry.getConfig();
			if(serviceRegistryConfig != null) {
				List<V1Deployment> repoServices = kubeService.getDeploymentList(serviceRegistryConfig, "app", appLabel);
				for(V1Deployment repoImage : repoServices) {
					Deployment publishedService = new Deployment(repoImage);
					deployments.add(publishedService);
				}
			}
		} catch (Exception e) {
			String message = "Error listing deployments from registry " + serviceRegistry.getName() + ": " + e.getMessage();
			FacesMessage faceMessage = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", message);
	        FacesContext.getCurrentInstance().addMessage(null, faceMessage);
	        logger.error(message, e);
		}
		return deployments;
	}
	
	public List<Pod> listPods(String serviceRegistryUri, String appLabel) {
		ServiceRegistry serviceRegistry = serviceRegistryMap.get(serviceRegistryUri);
		List<Pod> pods = new ArrayList<Pod>();
		try {
			String serviceRegistryConfig = serviceRegistry.getConfig();
			if(serviceRegistryConfig != null) {
				List<V1Pod> repoPods = kubeService.getPodList(serviceRegistryConfig, "app", appLabel);
				for(V1Pod repoPod : repoPods) {
					Pod publishedService = new Pod(repoPod);
					pods.add(publishedService);
				}
			}
		} catch (Exception e) {
			String message = "Error listing deployments from registry " + serviceRegistry.getName() + ": " + e.getMessage();
			FacesMessage faceMessage = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", message);
	        FacesContext.getCurrentInstance().addMessage(null, faceMessage);
	        logger.error(message, e);
		}
		return pods;
	}
	
	public List<Statefulset> listStatefulSets(String serviceRegistryUri, String appLabel) {
		ServiceRegistry serviceRegistry = serviceRegistryMap.get(serviceRegistryUri);
		List<Statefulset> deployments = new ArrayList<Statefulset>();
		try {
			String serviceRegistryConfig = serviceRegistry.getConfig();
			if(serviceRegistryConfig != null) {
				List<V1StatefulSet> repoServices = kubeService.getStatefulsetList(serviceRegistryConfig, "app", appLabel);
				for(V1StatefulSet repoImage : repoServices) {
					Statefulset publishedService = new Statefulset(repoImage);
					deployments.add(publishedService);
				}
			}
		} catch (Exception e) {
			String message = "Error listing deployments from registry " + serviceRegistry.getName() + ": " + e.getMessage();
			FacesMessage faceMessage = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", message);
	        FacesContext.getCurrentInstance().addMessage(null, faceMessage);
	        logger.error(message, e);
		}
		return deployments;
	}
	
	public List<PublishedService> list(String serviceRegistryUri) {
		ServiceRegistry serviceRegistry = serviceRegistryMap.get(serviceRegistryUri);
		List<PublishedService> publishedServiceList = serviceRegistryServiceMap.get(serviceRegistryUri);
		String serviceRegistryConfig = serviceRegistry.getConfig();
		if(publishedServiceList == null && serviceRegistryConfig != null) {
			publishedServiceList = new ArrayList<PublishedService>();
			try {
				List<V1Service> repoServices = kubeService.getServiceList(serviceRegistryConfig);
				Map<String, PublishedService> serviceRegistryServiceMap = new HashMap<String, PublishedService>();
				for(V1Service repoImage : repoServices) {
					PublishedService publishedService = new PublishedService(repoImage);
					Map<String, String> labels = publishedService.getRepoLabels();
					if(labels != null) {
						String braineID = labels.get("BRAINE_ID");
						if(braineID != null) {
							ServiceProfile profile = profileService.get(braineID);
							publishedService.setService(profile);
							serviceRegistryServiceMap.put(braineID, publishedService);
						}
					}
					publishedServiceList.add(publishedService);
				}
			} catch (Exception e) {
				String message = "Error listing services from registry " + serviceRegistry.getName() + ": " + e.getMessage();
				FacesMessage faceMessage = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", message);
		        FacesContext.getCurrentInstance().addMessage(null, faceMessage);
		        logger.error(message, e);
			}
		} else if(publishedServiceList == null){
			publishedServiceList = new ArrayList<PublishedService>();
		}
		serviceRegistryServiceMap.put(serviceRegistryUri, publishedServiceList);
		return publishedServiceList;
	}
	
	public List<String> listApps(String serviceRegistryUri) {
		ServiceRegistry serviceRegistry = serviceRegistryMap.get(serviceRegistryUri);
		List<String> publishedAppList = serviceRegistryAppMap.get(serviceRegistryUri);
		String serviceRegistryConfig = serviceRegistry.getConfig();
		if(publishedAppList == null && serviceRegistryConfig != null) {
			publishedAppList = new ArrayList<String>();
			try {
				Collection<String> registryApps = kubeService.getAppList(serviceRegistryConfig, "app");
				for(String app : registryApps) {
					publishedAppList.add(app);
				}
			} catch (Exception e) {
				String message = "Error listing apps: " + e.getMessage();
				FacesMessage faceMessage = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", message);
		        FacesContext.getCurrentInstance().addMessage(null, faceMessage);
		        logger.error(message, e);
			}
		} else if(publishedAppList == null) {
			publishedAppList = new ArrayList<String>();
		}
		serviceRegistryAppMap.put(serviceRegistryUri, publishedAppList);
		return publishedAppList;
	}

	public List<PublishedService> localList(String serviceRegistryName) {
		ServiceRegistry serviceRegistry = serviceRegistryMap.get(serviceRegistryName);
		List<PublishedService> publishedServiceList = serviceRegistryServiceMap.get(serviceRegistryName);
		if(publishedServiceList == null) {
			publishedServiceList = new ArrayList<PublishedService>();
			try {
				List<V1Service> repoServices = kubeService.getServiceList(serviceRegistry.getConfig());
				Map<String, PublishedService> serviceRegistryImageMap = new HashMap<String, PublishedService>();
				for(V1Service repoImage : repoServices) {
					PublishedService publishedService = new PublishedService(repoImage);
					Map<String, String> labels = publishedService.getRepoLabels();
					if(labels != null) {
						String braineID = labels.get("BRAINE_ID");
						if(braineID != null) {
							ServiceProfile profile = profileService.get(braineID);
							publishedService.setService(profile);
							serviceRegistryImageMap.put(braineID, publishedService);
						}
					}
					publishedServiceList.add(publishedService);
				}
			} catch (Exception e) {
				String message = "Error listing services from registry " + serviceRegistry.getName() + ": " + e.getMessage();
				FacesMessage faceMessage = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", message);
		        FacesContext.getCurrentInstance().addMessage(null, faceMessage);
		        logger.error(message, e);
			}
			serviceRegistryServiceMap.put(serviceRegistryName, publishedServiceList);
		}
		return publishedServiceList;
	}

	public void setList(List<ServiceRegistry> imageRegistryList) {
		this.serviceRegistryList = imageRegistryList;
	}
	
	public void newRegistry() {
		Integer i = 0;
		String namespace = ServiceRegistryView.IMAGE_REPOSITORY_NAMESPACE;
		String imageURI = namespace + i;
		if(serviceRegistryMap != null) {
			while(serviceRegistryMap.containsKey(namespace + i)) { i++; }
			imageURI = namespace + i;
		} else {
			serviceRegistryMap = new HashMap<String, ServiceRegistry>();
		}
		ServiceRegistry imageRegistry = new ServiceRegistry("Service Registry " + i);
		imageRegistry.setUri(imageURI);
		serviceRegistryMap.put(imageURI, imageRegistry);
		serviceRegistryList.add(imageRegistry);
		service.insert(imageRegistry);
	}
	
	public void selectService(String serviceRegistry) {
        this.selectedRegistry = serviceRegistry;
		Map<String, Object> options = new HashMap<String, Object>();
        options.put("modal", true);
        options.put("minWidth", "400");
        options.put("minHeight", "300");
        options.put("headerElement", "customheader");
        options.put("widgetVar", "imageDeploymentDialog");
        options.put("id", "imageDeploymentDialog");
        PrimeFaces.current().dialog().openDynamic("serviceDeploymentDialog", options, null);
	}
	
	public void setSelectedProfile(ServiceProfile selectedProfile) {
		this.selectedProfile = selectedProfile;
	}
	
	public ServiceProfile getSelectedProfile() {
		return selectedProfile;
	}
	
	public void deploy() {
		try {
			ServiceRegistry imageRegistry = serviceRegistryMap.get(selectedRegistry);
			kubeService.deploy(imageRegistry.getConfig(), selectedProfile);
			serviceRegistryServiceMap.put(selectedRegistry, null);
			serviceRegistryAppMap.put(selectedRegistry, null);
			FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO, "Success", "Service " + selectedProfile.getName() + " deployed.");
	        FacesContext.getCurrentInstance().addMessage(null, message);
		} catch (Exception e) {
			FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Error deploying service " + selectedProfile.getName() + ": " + e.getMessage());
	        FacesContext.getCurrentInstance().addMessage(null, message);
	        logger.error(e);
		}
		PrimeFaces.current().dialog().closeDynamic(null);
	}
}