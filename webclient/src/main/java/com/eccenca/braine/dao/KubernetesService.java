package com.eccenca.braine.dao;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.enterprise.context.SessionScoped;
import javax.inject.Named;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.google.common.io.CharSource;

import io.kubernetes.client.common.KubernetesObject;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.apis.AppsV1Api;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.V1DeleteOptions;
import io.kubernetes.client.openapi.models.V1Deployment;
import io.kubernetes.client.openapi.models.V1DeploymentList;
import io.kubernetes.client.openapi.models.V1Pod;
import io.kubernetes.client.openapi.models.V1PodList;
import io.kubernetes.client.openapi.models.V1Service;
import io.kubernetes.client.openapi.models.V1ServiceList;
import io.kubernetes.client.openapi.models.V1StatefulSet;
import io.kubernetes.client.openapi.models.V1StatefulSetList;
import io.kubernetes.client.openapi.models.V1Status;
import io.kubernetes.client.util.ClientBuilder;
import io.kubernetes.client.util.KubeConfig;
import io.kubernetes.client.util.Yaml;

@Named
@SessionScoped
public class KubernetesService {

	private static final Logger logger = LogManager.getLogger(KubernetesService.class);
		
	public List<ServiceRegistry> getRegistryList(File configFile) {
		try {
			List<ServiceRegistry> serviceRegistries = new ArrayList<ServiceRegistry>();
			FileReader configFileReader = new FileReader(configFile);
			KubeConfig config = KubeConfig.loadKubeConfig(configFileReader);
			ArrayList<Object> contexts = config.getContexts();
			for(Object o : contexts) {
				LinkedHashMap<?, ?> context = (LinkedHashMap<?, ?>) o;
				String name = (String) context.get("name");
				ServiceRegistry serviceRegistry = new ServiceRegistry(name);
				serviceRegistry.setUri(name);
				String configYAML = Files.readString(configFile.toPath());
				ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
				Map<String, Object> yamlMap = mapper.readValue(configYAML, Map.class);
				Object contextValue = yamlMap.get("current-context");
				if(!name.equals(contextValue)) {
					yamlMap.put("current-context", name);
				}
				String newConfigYAML = mapper.writeValueAsString(yamlMap);
				serviceRegistry.setConfig(newConfigYAML);
				serviceRegistries.add(serviceRegistry);
			}
		    return serviceRegistries;
		} catch (IOException e) {
			logger.error("Error retrieving registered contexts.", e);
		}
		return null;
	}
	
	public List<V1Service> getServiceList(String config) throws ApiException {
		try(Reader configReader = CharSource.wrap(config).openStream();) { 
			KubeConfig configFile = KubeConfig.loadKubeConfig(configReader);
			ApiClient client = ClientBuilder.kubeconfig(configFile).build();
			CoreV1Api api = getAPI(client);
			V1ServiceList serviceList = api.listServiceForAllNamespaces(false, null, null, null, 10, "true", null, null, null, false);
			return serviceList.getItems();			
		} catch (IOException e) {
			logger.error("Error retrieving services.", e);
		}
		return null;
	}
	
	public List<V1Service> getServiceList(String config, String appSelector, String appName) throws ApiException {
		try(Reader configReader = CharSource.wrap(config).openStream();) { 
			KubeConfig configFile = KubeConfig.loadKubeConfig(configReader);
			ApiClient client = ClientBuilder.kubeconfig(configFile).build();
			CoreV1Api api = getAPI(client);
			String labelSelector = appSelector + "= " + appName;
			V1ServiceList serviceList = api.listServiceForAllNamespaces(false, null, null, labelSelector , 10, "true", null, null, null, false);
			return serviceList.getItems();			
		} catch (IOException e) {
			logger.error("Error retrieving services.", e);
		}
		return null;
	}
	
	public Collection<String> getAppList(String config, String appSelector) throws ApiException {
		Set<String> apps = new HashSet<String>();
		List<V1Service> serviceList = getServiceList(config);
		for(V1Service service : serviceList) {
			Map<String, String> labels = service.getMetadata().getLabels();
			if(labels != null) {
				String label = labels.get(appSelector);
				if(label != null) {
					apps.add(label);
				}
			}
		}
		List<V1Deployment> deploymentList = getDeploymentList(config);
		for(V1Deployment deployment : deploymentList) {
			Map<String, String> labels = deployment.getMetadata().getLabels();
			if(labels != null) {
				String label = labels.get(appSelector);
				if(label != null) {
					apps.add(label);
				}
			}
		}
		List<V1Pod> podList = getPodList(config);
		for(V1Pod pod : podList) {
			Map<String, String> labels = pod.getMetadata().getLabels();
			if(labels != null) {
				String label = labels.get(appSelector);
				if(label != null) {
					apps.add(label);
				}
			}
		}
		return apps;
	}
	
	public List<V1Deployment> getDeploymentList(String config) {
		try(Reader configReader = CharSource.wrap(config).openStream();) { 
			KubeConfig configFile = KubeConfig.loadKubeConfig(configReader);
			ApiClient client = ClientBuilder.kubeconfig(configFile).build();
			AppsV1Api apiV1 = getApps(client);
			V1DeploymentList deploymentList = apiV1.listDeploymentForAllNamespaces(false, null, null, null, 10, null, null, null, 10, false);
			return deploymentList.getItems();
		} catch (IOException | ApiException e) {
			logger.error("Error retrieving deployments.", e);
		}
		return null;
	}
	
	public List<V1Deployment> getDeploymentList(String config, String appSelector, String appLabel) {
		try(Reader configReader = CharSource.wrap(config).openStream();) { 
			KubeConfig configFile = KubeConfig.loadKubeConfig(configReader);
			ApiClient client = ClientBuilder.kubeconfig(configFile).build();
			AppsV1Api apiV1 = getApps(client);
			String labelSelector = appSelector + "= " + appLabel;
			V1DeploymentList deploymentList = apiV1.listDeploymentForAllNamespaces(false, null, null, labelSelector, 10, null, null, null, 10, false);
			return deploymentList.getItems();
		} catch (IOException | ApiException e) {
			logger.error("Error retrieving deployments.", e);
		}
		return null;
	}
	
	public List<V1StatefulSet> getStatefulsetList(String config, String appSelector, String appLabel) {
		try(Reader configReader = CharSource.wrap(config).openStream();) { 
			KubeConfig configFile = KubeConfig.loadKubeConfig(configReader);
			ApiClient client = ClientBuilder.kubeconfig(configFile).build();
			AppsV1Api apiV1 = getApps(client);
			String labelSelector = appSelector + "= " + appLabel;
			V1StatefulSetList statefulsetList = apiV1.listStatefulSetForAllNamespaces(false, null, null, labelSelector, 10, null, null, null, 10, false);
			return statefulsetList.getItems();
		} catch (IOException | ApiException e) {
			logger.error("Error retrieving statefusets.", e);
		}
		return null;
	}
	
	public List<V1Pod> getPodList(String config, String appSelector, String appLabel) {
		try(Reader configReader = CharSource.wrap(config).openStream();) { 
			KubeConfig configFile = KubeConfig.loadKubeConfig(configReader);
			ApiClient client = ClientBuilder.kubeconfig(configFile).build();
			CoreV1Api api = getAPI(client);
			String labelSelector = appSelector + "= " + appLabel;
			V1PodList podList = api.listPodForAllNamespaces(false, null, null, labelSelector, 10, null, null, null, 10, false);
			return podList.getItems();
		} catch (IOException | ApiException e) {
			logger.error("Error retrieving pods.", e);
		}
		return null;
	}
	
	public List<V1Pod> getPodList(String config) {
		try(Reader configReader = CharSource.wrap(config).openStream();) { 
			KubeConfig configFile = KubeConfig.loadKubeConfig(configReader);
			ApiClient client = ClientBuilder.kubeconfig(configFile).build();
			CoreV1Api api = getAPI(client);
			V1PodList podList = api.listPodForAllNamespaces(false, null, null, null, 10, null, null, null, 10, false);
			return podList.getItems();
		} catch (IOException | ApiException e) {
			logger.error("Error retrieving pods.", e);
		}
		return null;
	}
	
	public void removeApp(String config, String appSelector, String appLabel) {
		removeDeployments(config, appSelector, appLabel);
		removeServices(config, appSelector, appLabel);
		removeStatefulsets(config, appSelector, appLabel);
		removePods(config, appSelector, appLabel);
	}
	
	public void removeDeployments(String config, String appSelector, String appLabel) {
		try(Reader configReader = CharSource.wrap(config).openStream();) {
			List<V1Deployment> deployments = getDeploymentList(config, appSelector, appLabel);
			KubeConfig configFile = KubeConfig.loadKubeConfig(configReader);
			ApiClient client = ClientBuilder.kubeconfig(configFile).build();
			AppsV1Api apiV1 = getApps(client);
			for(V1Deployment deployment :deployments) {
			    V1Status deleteResult = apiV1.deleteNamespacedDeployment(
			    	deployment.getMetadata().getName(),
			    	deployment.getMetadata().getNamespace(),
		            null,
		            null,
		            null,
		            null,
		            null,
		            new V1DeleteOptions());
			}
		} catch (IOException | ApiException e) {
			logger.error("Error removing deployment from application: " + appLabel, e);
		}
	}
	
	public void removeDeployment(String config, String namespace, String name) {
		try(Reader configReader = CharSource.wrap(config).openStream();) {
			KubeConfig configFile = KubeConfig.loadKubeConfig(configReader);
			ApiClient client = ClientBuilder.kubeconfig(configFile).build();
			AppsV1Api apiV1 = getApps(client);
		    V1Status deleteResult = apiV1.deleteNamespacedDeployment(
		    	name,
		    	namespace,
	            null,
	            null,
	            null,
	            null,
	            null,
	            new V1DeleteOptions());
		} catch (IOException | ApiException e) {
			logger.error("Error removing deployment: " + name, e);
		}
	}
	
	public void removeStatefulsets(String config, String appSelector, String appLabel) {
		try(Reader configReader = CharSource.wrap(config).openStream();) {
			List<V1StatefulSet> statefulsets = getStatefulsetList(config, appSelector, appLabel);
			KubeConfig configFile = KubeConfig.loadKubeConfig(configReader);
			ApiClient client = ClientBuilder.kubeconfig(configFile).build();
			AppsV1Api apiV1 = getApps(client);
			for(V1StatefulSet statefulset : statefulsets) {
			    V1Status deleteResult = apiV1.deleteNamespacedStatefulSet(
			    	statefulset.getMetadata().getName(),
			    	statefulset.getMetadata().getNamespace(),
		            null,
		            null,
		            null,
		            null,
		            null,
		            new V1DeleteOptions());
			}
		} catch (IOException | ApiException e) {
			logger.error("Error removing statefulset from application: " + appLabel, e);
		}
	}
	
	public void removeStatefulset(String config, String namespace, String name) {
		try(Reader configReader = CharSource.wrap(config).openStream();) {
			KubeConfig configFile = KubeConfig.loadKubeConfig(configReader);
			ApiClient client = ClientBuilder.kubeconfig(configFile).build();
			AppsV1Api apiV1 = getApps(client);
		    V1Status deleteResult = apiV1.deleteNamespacedStatefulSet(
		    	name,
		    	namespace,
	            null,
	            null,
	            null,
	            null,
	            null,
	            new V1DeleteOptions());
		} catch (IOException | ApiException e) {
			logger.error("Error removing statefulset: " + name, e);
		}
	}
	
	public void removeServices(String config, String appSelector, String appLabel) {
		try(Reader configReader = CharSource.wrap(config).openStream();) {
			List<V1Service> services = getServiceList(config, appSelector, appLabel);
			KubeConfig configFile = KubeConfig.loadKubeConfig(configReader);
			ApiClient client = ClientBuilder.kubeconfig(configFile).build();
			CoreV1Api api = getAPI(client);
			for(V1Service service :services) {
			    V1Status deleteResult = api.deleteNamespacedService(
			    	service.getMetadata().getName(),
		            service.getMetadata().getNamespace(),
		            null,
		            null,
		            null,
		            null,
		            null,
		            new V1DeleteOptions());
			}
		} catch (IOException | ApiException e) {
			logger.error("Error removing service from application: " + appLabel, e);
		}
	}
	
	public void removePods(String config, String appSelector, String appLabel) {
		try(Reader configReader = CharSource.wrap(config).openStream();) {
			List<V1Pod> pods = getPodList(config, appSelector, appLabel);
			KubeConfig configFile = KubeConfig.loadKubeConfig(configReader);
			ApiClient client = ClientBuilder.kubeconfig(configFile).build();
			CoreV1Api api = getAPI(client);
			for(V1Pod pod :pods) {
			    V1Pod deleteResult = api.deleteNamespacedPod(
		    		pod.getMetadata().getName(),
		    		pod.getMetadata().getNamespace(),
		            null,
		            null,
		            null,
		            null,
		            null,
		            new V1DeleteOptions());
			}
		} catch (IOException | ApiException e) {
			logger.error("Error removing pod from application: " + appLabel, e);
		}
	}
	
	private CoreV1Api getAPI(ApiClient client) throws IOException {
		CoreV1Api api = new CoreV1Api(client);
	    return api;
	}
	
	private AppsV1Api getApps(ApiClient client) throws IOException {
		AppsV1Api api = new AppsV1Api(client);
	    return api;
	}
	
	public void deploy(String config, com.eccenca.braine.dao.ServiceProfile selectedServiceProfile) throws Exception {
		logger.info("deploying service " + selectedServiceProfile.getName());
		try(Reader configReader = CharSource.wrap(config).openStream();) {
			KubeConfig configFile = KubeConfig.loadKubeConfig(configReader);
			ApiClient client = ClientBuilder.kubeconfig(configFile).build();
			AppsV1Api api = getApps(client);
			String manifest = selectedServiceProfile.getManifest();
			io.kubernetes.client.common.KubernetesObject object = (KubernetesObject) Yaml.load(manifest);
			assertName(object);
		    if(object instanceof V1Deployment) {
		    	V1Deployment yaml = (V1Deployment) object;
		    	removeDeployment(config, "default", object.getMetadata().getName());
		    	V1Deployment createResult = api.createNamespacedDeployment("default", yaml, null, null, null);
		    } else if(object instanceof V1StatefulSet) {
		    	V1StatefulSet yaml = (V1StatefulSet) object;
		    	removeStatefulset(config, "default", object.getMetadata().getName());
		    	V1StatefulSet createResult = api.createNamespacedStatefulSet("default", yaml, null, null, null);
		    } else {
		    	throw new Exception("Invalid Manifest. Deployment type " + object.getKind() +  " not supported.");
		    }
		}
	}

	private void assertName(io.kubernetes.client.common.KubernetesObject yaml) throws Exception {
		if(yaml.getMetadata().getName() == null) {
			throw new Exception("Empty name: assign a name to the " + yaml.getKind());
		}
	}

	public V1Service getService(String uri) {
		try {
			ApiClient client = io.kubernetes.client.util.Config.defaultClient();
			CoreV1Api api = getAPI(client);
			Map<String, String> labels = new HashMap<String, String>();
			labels.put("BRAINE_ID", uri);
			V1ServiceList serviceList = api.listServiceForAllNamespaces(false, null, null, "BRAINE_ID=" + uri, 10, "true", null, null, null, false);
			List<V1Service> services = serviceList.getItems();
			if(services.size() > 0) {
				return services.get(0);
			}
		} catch (IOException | ApiException e) {
			logger.error("Error retrieving registered image: " + uri, e);
		}
		return null;
	}
}