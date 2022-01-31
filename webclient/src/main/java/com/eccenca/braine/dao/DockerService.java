package com.eccenca.braine.dao;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.BuildImageResultCallback;
import com.github.dockerjava.api.model.BuildResponseItem;
import com.github.dockerjava.api.model.Image;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.core.DockerClientImpl;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;
import com.github.dockerjava.transport.DockerHttpClient;

@Named
@SessionScoped
public class DockerService {

	private static final Logger logger = LogManager.getLogger(DockerService.class);
	
	@Inject
    private ImageService imageService;
		
	public List<Image> getList() {
		try(DockerClient client = getClient()) {
			return client.listImagesCmd().exec();
		} catch (IOException e) {
			logger.error("Error retrieving registered images.", e);
		}
		return null;
	}
	
	public void remove(String imageRepoId) {
		try(DockerClient client = getClient()) {
			client.removeImageCmd(imageRepoId).exec();
		} catch (IOException e) {
			logger.error("Error removing image: " + imageRepoId, e);
		}
	}
	
	private DockerClient getClient() {
		DockerClientConfig config = null;
		DockerClient dockerClient = null;
		config = DefaultDockerClientConfig.createDefaultConfigBuilder().build();
		DockerHttpClient httpClient = new ApacheDockerHttpClient.Builder()
			    .dockerHost(config.getDockerHost())
			    .maxConnections(100)
			    .connectionTimeout(Duration.ofSeconds(30))
			    .responseTimeout(Duration.ofSeconds(45))
			    .build();
		dockerClient = DockerClientImpl.getInstance(config, httpClient);
		return dockerClient;
	}

	public String deploy(com.eccenca.braine.dao.Image selectedImage) throws InterruptedException, IOException {
		return deploy("", selectedImage);
	}
	
	public String deploy(String imageRegisterIp, com.eccenca.braine.dao.Image selectedImage) throws InterruptedException, IOException {
		logger.info("deploying image " + selectedImage.getName());
		String imageTag = null;
		if(imageRegisterIp == null || imageRegisterIp.isEmpty()) {
			imageTag = getTag(selectedImage.getName());
		} else {
			imageTag = imageRegisterIp + "/" + getTag(selectedImage.getName());
		}
		try(DockerClient client = getClient()) {
			File manifestFile = null;
			String manifestFilePath = selectedImage.getManifestFile();
			if(manifestFilePath == null) {
				manifestFile = imageService.getManifestFileWithContent(selectedImage.getName(), selectedImage.getManifest());
			} else {
				String manifestFileName = selectedImage.getManifestFile();
				String filePath = imageService.getLocalFilePath(selectedImage.getName(), manifestFileName);
				manifestFile = new File(filePath);
			}
			com.github.dockerjava.api.command.BuildImageResultCallback callback = new BuildImageResultCallback() {
		        @Override
		        public void onNext(BuildResponseItem item) {
		        	logger.info(getValue(item.getStream(), "") + " " + getValue(item.getStatus(), ""));
		            super.onNext(item);
		        }
		    };
		    Map<String, String> labels = new HashMap<String, String>();
		    labels.put("BRAINE_ID", selectedImage.getUri());
		    
		    @SuppressWarnings("deprecation")
			String imageId = client.buildImageCmd(manifestFile)
		    		.withTag(imageTag)
		    		.withLabels(labels)
		    		.exec(callback)
		    		.awaitImageId();
		    client.pushImageCmd(imageTag)
		    	.withName(imageTag)
		    	.start()
		    	.awaitCompletion();
		    
			return imageId;
		}
	}
	
	private String getValue(String value, String defaultValue) {
		if(value == null) {
			return defaultValue;
		}
		return value;
	}
	
	private String getTag(String imageName) {
		return "braine/" + imageName.toLowerCase()
				.replace(" ", "_");
	}

	public Image getImage(String uri) {
		try(DockerClient client = getClient()) {
			 Map<String, String> labels = new HashMap<String, String>();
			    labels.put("BRAINE_ID", uri);
			List<Image> images = client.listImagesCmd().withLabelFilter(labels).exec();
			if(images.size() > 0) {
				return images.get(0);
			}
		} catch (IOException e) {
			logger.error("Error retrieving registered image: " + uri, e);
		}
		return null;
	}
}