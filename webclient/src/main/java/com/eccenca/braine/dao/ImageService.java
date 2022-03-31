package com.eccenca.braine.dao;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.primefaces.model.file.UploadedFile;
import org.springframework.beans.factory.annotation.Value;

@Named
@SessionScoped
public class ImageService {
	
	protected static final Logger logger = LogManager.getLogger();

	private final static String DATA_TYPE = "https://braine.eccenca.dev/vocabulary/itops#DockerImage";
	private final static TriplesToImageMarshal tripleToImage = new TriplesToImageMarshal();
	private final static ImageToTripleMarshal imageToTriple = new ImageToTripleMarshal();
	private Map<String, String> attrMapping = new HashMap<String, String>();
	{
		attrMapping.put(Image.DESCRIPTION_ATTR, "http://www.w3.org/2000/01/rdf-schema#comment");
		attrMapping.put(Image.LABEL_ATTR, "http://www.w3.org/2000/01/rdf-schema#label");
		attrMapping.put(Image.MANIFEST_ATTR, "https://braine.eccenca.dev/vocabulary/itops#manifest");
		attrMapping.put(Image.MANIFEST_FILE_ATTR, "https://braine.eccenca.dev/vocabulary/itops#manifestFile");
		attrMapping.put(Image.VARIABLE_FILE_ATTR, "https://braine.eccenca.dev/vocabulary/itops#variablesFile");
		attrMapping.put(Image.VARIABLE_ATTR, "https://braine.eccenca.dev/vocabulary/itops#variables");
		attrMapping.put(Image.FILES_ATTR, "https://braine.eccenca.dev/vocabulary/itops#files");
	}

	@Inject
	private SPARQLService sparqlService;

	@Value("${client.file.repo:#{null}}")
	private String fileRepoPath;

	private File fileRepo;

	@PostConstruct
	public void init() {
		fileRepo = new File(fileRepoPath);
		fileRepo.mkdir();
	}

	public List<Image> list() {

		return sparqlService.list(DATA_TYPE, tripleToImage, attrMapping);
	}

	public void setSparqlService(SPARQLService sparqlService) {
		this.sparqlService = sparqlService;
	}

	public SPARQLService getSparqlService() {
		return sparqlService;
	}

	public void update(Image image) {
		sparqlService.update(image, imageToTriple, attrMapping);
	}

	public void insert(Image image) {
		sparqlService.insert(image, imageToTriple, attrMapping);
	}

	public void delete(Image image) {
		sparqlService.delete(image.getUri());
	}

	public void delete(String uri) {
		sparqlService.delete(uri);
	}

	public void save(String imageUri, UploadedFile file) throws IOException {
		File imageDir = new File(getImagePath(imageUri));
		imageDir.mkdir();
		File newFile = new File(getFilePath(imageUri, file.getFileName()));
		newFile.createNewFile();
		FileUtils.copyInputStreamToFile(file.getInputStream(), newFile);
	}

	public void delete(String imageUri, String fileUri) {
		File newFile = new File(getFilePath(imageUri, fileUri));
		newFile.delete();
	}

	public File getManifestFileWithContent(String imageUri, String manifestContent) throws IOException {
		File manifestFile = new File(getFilePath(imageUri, "Dockerfile"));
		FileUtils.writeStringToFile(manifestFile, manifestContent, StandardCharsets.UTF_8);
		return manifestFile;
	}

	public String getImagePath(String imageUri) {
		return fileRepo.getPath() + File.separator + Integer.toString(imageUri.hashCode());
	}

	public String getFilePath(String imageUri, String filePath) {
		return getImagePath(imageUri) + File.separator + filePath;
	}

	public String getLocalFilePath(String imageUri, String filePath) {
		if (filePath.contains(".zip/")) {
			String[] subPaths = filePath.split(".zip/");
			String extractedFilePath = subPaths[0];
			String zipFileName = extractedFilePath + ".zip";
			String insideEntryPath = subPaths[1];
			try {
				File file = new File(getImagePath(imageUri) + File.separator + extractedFilePath + File.separator
						+ insideEntryPath);
				if (!file.exists()) {
					net.lingala.zip4j.core.ZipFile zipFile = new net.lingala.zip4j.core.ZipFile(
							getFilePath(imageUri, zipFileName));
					zipFile.extractAll(getFilePath(imageUri, extractedFilePath));
				}
				return file.getPath();
			} catch (net.lingala.zip4j.exception.ZipException e) {
				logger.error(e);
			}
			return null;
		} else {
			return getFilePath(imageUri, filePath);
		}
	}

	public String getFileContent(String manifestFile) throws IOException {
		if (manifestFile.contains(".zip/")) {
				String[] subPaths = manifestFile.split(".zip/");
				String zipFilePath = subPaths[0] + ".zip";
				String insideEntryPath = subPaths[1];
				// open a zip file for reading
				try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFilePath));){
				// get an enumeration of the ZIP file entries
				ZipEntry zipEntry = zis.getNextEntry();
				while (zipEntry != null) {
					if (!zipEntry.isDirectory() && zipEntry.getName().equals(insideEntryPath)) {
						try (InputStreamReader r = new InputStreamReader(zis);) {
							StringBuilder textBuilder = new StringBuilder();
							try (Reader reader = new BufferedReader(r)) {
								int c = 0;
								while ((c = reader.read()) != -1) {
									textBuilder.append((char) c);
								}
							}
							return textBuilder.toString();
						}
					}
					zipEntry = zis.getNextEntry();
				}
			} catch (IOException e) {
				logger.error(e);
			}
			return null;
		} else {
			File file = new File(manifestFile);
			try {
				return Files.readString(file.toPath());
			} catch (IOException e) {
				logger.error(e);
			}
		}
		return null;
	}

	public Image getImage(String uri) {
		return sparqlService.get(uri, tripleToImage, attrMapping);
	}
}