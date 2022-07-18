package com.eccenca.braine.view;

import java.io.Serializable;
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

import com.eccenca.braine.dao.Workflow;
import com.eccenca.braine.dao.WorkflowService;

@Named
@ViewScoped
public class WorkflowView implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -7797576003564796840L;
	
	private static final String NAMESPACE = "https://data.braine-project.eu/itops/workflow/";

	protected static final Logger logger = LogManager.getLogger();
	
	@Inject
    private WorkflowService service;
	
	private Map<String, Workflow> map = new HashMap<String, Workflow>();
	private List<Workflow> list;
	
	public WorkflowView() {
	}
	
	@PostConstruct
	public void init() {
		list = service.list();
		for(Workflow workflow : list) {
			map.put(workflow.getUri(), workflow);
		}
	}
	
	public void onRowEdit(RowEditEvent<Workflow> event) {
		Workflow object = event.getObject();
		service.update(object);
        FacesMessage msg = new FacesMessage("Workflow Updated", object.getName());
        FacesContext.getCurrentInstance().addMessage(null, msg);
    }
	
	public void remove(String uri) {
		try {
			service.delete(uri);
			Workflow object = map.get(uri);
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
			Workflow object = map.get(uri);
			service.update(object);
			FacesMessage message = new FacesMessage("Successful", "Workflow " + object.getName() + " was updated.");
	        FacesContext.getCurrentInstance().addMessage(null, message);
		} catch (Exception e) {
			logger.error(e);
		}
	}
	
	public List<Workflow> getList() {
		return list;
	}
	
	public void setList(List<Workflow> profileList) {
		this.list = profileList;
	}
	
	public void newObject() {
		Integer i = 0;
		String namespace = WorkflowView.NAMESPACE;
		String uri = namespace + i;
		if(map != null) {
			while(map.containsKey(namespace + i)) { i++; }
			uri = namespace + i;
		} else {
			map = new HashMap<String, Workflow>();
		}
		Workflow profile = new Workflow("Workflow " + i);
		profile.setUri(uri);
		map.put(uri, profile);
		list.add(profile);
		service.insert(profile);
	}
}