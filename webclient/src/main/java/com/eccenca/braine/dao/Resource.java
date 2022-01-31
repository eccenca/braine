package com.eccenca.braine.dao;

import java.io.Serializable;

public class Resource implements Serializable, Comparable<Resource> {
 
    /**
	 * 
	 */
	private static final long serialVersionUID = 1116918349945342385L;

	private String name;
    
    private String uri;
     
    private String description;
	
	public Resource(String name) {
        this.name = name;
	}
     
    public Resource(String name, String description) {
        this.name = name;
        this.description = description;
    }
    
    public Resource(String name, String description, String uri) {
        this.name = name;
        this.description = description;
        this.uri = uri;
    }
 
    public String getName() {
        return name;
    }
 
    public void setName(String name) {
        this.name = name;
    }
    
    public String getDescription() {
		return description;
	}
    
    public void setDescription(String description) {
		this.description = description;
	}
    
    public void setUri(String uri) {
		this.uri = uri;
	}
    
    public String getUri() {
		return uri;
	}
  
    //Eclipse Generated hashCode and equals
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((description == null) ? 0 : description.hashCode());
        return result;
    }
 
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Resource other = (Resource) obj;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        if (description == null) {
            if (other.description != null)
                return false;
        } else if (!description.equals(other.description))
            return false;
        return true;
    }
 
    @Override
    public String toString() {
        return name;
    }
 
    public int compareTo(Resource document) {
        return this.getName().compareTo(document.getName());
    }
}