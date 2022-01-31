package com.eccenca.braine;

import java.util.Arrays;

import javax.faces.webapp.FacesServlet;
import javax.servlet.ServletContext;

import org.primefaces.webapp.filter.FileUploadFilter;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class BraineWebApp {

	public static void main(String[] args) {
		SpringApplication.run(BraineWebApp.class, args);
	}

	@Bean
	ServletRegistrationBean<FacesServlet> jsfServletRegistration(ServletContext servletContext) {
		// spring boot only works if this is set
		servletContext.setInitParameter("com.sun.faces.forceLoadConfiguration", Boolean.TRUE.toString());
		servletContext.setInitParameter("primefaces.THEME", "saga");
		servletContext.setInitParameter("primefaces.UPLOADER", "commons");

		// registration
		ServletRegistrationBean<FacesServlet> srb = new ServletRegistrationBean<FacesServlet>();
		srb.setServlet(new FacesServlet());
		srb.setUrlMappings(Arrays.asList("*.xhtml"));
		srb.setLoadOnStartup(1);
		return srb;
	}

	@Bean
	public FilterRegistrationBean<FileUploadFilter> FileUploadFilter() {
		FilterRegistrationBean<FileUploadFilter> registration = new FilterRegistrationBean<FileUploadFilter>();
		registration.setFilter(new org.primefaces.webapp.filter.FileUploadFilter());
		registration.setName("FileUpload Filter");
		return registration;
	}

}