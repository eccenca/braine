package com.eccenca.braine.view;

import java.io.Serializable;
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
import org.primefaces.event.RowEditEvent;

import com.eccenca.braine.dao.ProcessingUnit;
import com.eccenca.braine.dao.ProcessingUnitService;
import com.eccenca.braine.dao.ServiceProfile;
import com.eccenca.braine.dao.ServiceProfileService;

@Named
@ViewScoped
public class ServiceProfileView implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -7797576003564796840L;
	
	private static final String NAMESPACE = "https://data.braine-project.eu/itops/service-profile/";

	protected static final Logger logger = LogManager.getLogger();
	
	private static final String UNKNOWN = "Unknown";
	
	@Inject
    private ServiceProfileService service;
	
	@Inject
    private ProcessingUnitService processingUnitService;
	
	private Map<String, ServiceProfile> map = new HashMap<String, ServiceProfile>();
	private Map<String, ProcessingUnit> cpuMap = new HashMap<String, ProcessingUnit>();
	private List<ProcessingUnit> cpuList = new ArrayList<ProcessingUnit>();
	private List<ServiceProfile> list;
	
	public ServiceProfileView() {
	}
	
	@PostConstruct
	public void init() {
		list = service.list();
		for(ServiceProfile profile : list) {
			map.put(profile.getUri(), profile);
		}
		cpuList = processingUnitService.list();
		for(ProcessingUnit cpu: cpuList) {
			cpuMap.put(cpu.getUri(), cpu);
		}
	}
	
	public Map<String, ProcessingUnit> getCpuMap() {
		return cpuMap;
	}

	public String getProcessingUnit(String key) {
		if(cpuMap.containsKey(key)) {
			return cpuMap.get(key).getName();
		} else {
			return UNKNOWN;
		}
	}
	
	public List<ProcessingUnit> getProcessingUnits() {
		return cpuList;
	}
	
	public void onRowEdit(RowEditEvent<ServiceProfile> event) {
		ServiceProfile object = event.getObject();
		service.update(object);
        FacesMessage msg = new FacesMessage("ServiceProfile Updated", object.getName());
        FacesContext.getCurrentInstance().addMessage(null, msg);
    }
	
	public void remove(String uri) {
		try {
			service.delete(uri);
			ServiceProfile object = map.get(uri);
			list.remove(object);
			map.remove(uri);
			FacesMessage message = new FacesMessage("Successful", object.getName() + " was removed.");
	        FacesContext.getCurrentInstance().addMessage(null, message);
		} catch (Exception e) {
			logger.error(e);
		}
	}
	
	public void update(String uri) {
		try {
			ServiceProfile object = map.get(uri);
			service.update(object);
			FacesMessage message = new FacesMessage("Successful", "ServiceProfile " + object.getName() + " was updated.");
	        FacesContext.getCurrentInstance().addMessage(null, message);
		} catch (Exception e) {
			logger.error(e);
		}
	}
	
	public List<ServiceProfile> getList() {
		return list;
	}
	
	public void setList(List<ServiceProfile> profileList) {
		this.list = profileList;
	}
	
	public void newObject() {
		Integer i = 0;
		String namespace = ServiceProfileView.NAMESPACE;
		String uri = namespace + i;
		if(map != null) {
			while(map.containsKey(namespace + i)) { i++; }
			uri = namespace + i;
		} else {
			map = new HashMap<String, ServiceProfile>();
		}
		ServiceProfile profile = new ServiceProfile("ServiceProfile " + i);
		profile.setUri(uri);
		map.put(uri, profile);
		list.add(profile);
		service.insert(profile);
	}
}