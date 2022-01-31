import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;
import com.github.dockerjava.transport.DockerHttpClient;
import com.github.dockerjava.transport.DockerHttpClient.Request;
import com.github.dockerjava.transport.DockerHttpClient.Response;

public class DockerTest {
	public static void main(String[] args) throws MalformedURLException, URISyntaxException, InterruptedException {
		DockerClientConfig config = DefaultDockerClientConfig.createDefaultConfigBuilder()
			    .build();
		
//		DockerClientConfig config = DefaultDockerClientConfig.createDefaultConfigBuilder()
//	    .withDockerHost("http://172.30.101.1:5000")
//	    .withDockerTlsVerify(false)
//	    .withRegistryUrl("http://172.30.101.1:5000")
//	    .build();
		
//		DockerClientConfig config = DefaultDockerClientConfig.createDefaultConfigBuilder().build();
//		URL url = new URL("tcp://127.0.0.1:2375");
//		DockerHttpClient httpClient = new ApacheDockerHttpClient.Builder()
//			    .dockerHost(config.getDockerHost())
//			    .maxConnections(100)
//			    .connectionTimeout(Duration.ofSeconds(30))
//			    .responseTimeout(Duration.ofSeconds(45))
//			    .build();
//		
//		System.out.println(config.getDockerHost());
//		
//		DockerClient dockerClient = DockerClientImpl.getInstance(config, httpClient);
//		List<Image> images = dockerClient.listImagesCmd().exec();
//		for(Image image : images) {
//			System.out.println(image.getId() + " " + (image.getRepoTags() != null? image.getRepoTags()[0] : ""));
//		}
//		
//		File manifestFile = new File("/Users/edgardmarx/Repos/braine/images/hello_world/Dockerfile");
//		com.github.dockerjava.api.command.BuildImageResultCallback callback = new BuildImageResultCallback() {
//	        @Override
//	        public void onNext(BuildResponseItem item) {
//	        	System.out.println(item.getStream() + item.getStatus());
//	            super.onNext(item);
//	        }
//	    };
//	    Map<String, String> labels = new HashMap<String, String>();
//	    String imageId = dockerClient.buildImageCmd(manifestFile)
//    		.withTag("braine/hello_world_2")
//    		.withLabels(labels)
//    		.exec(callback)
//    		.awaitImageId();
//	    
//	    dockerClient.pushImageCmd("braine/hello_world_2")
//	    	.start()
//	    	.awaitCompletion();
	    
	    
	    DockerHttpClient httpClient2 = new ApacheDockerHttpClient.Builder()
			    .dockerHost(URI.create("http://172.30.101.1:5000"))
			    .maxConnections(100)
			    .connectionTimeout(Duration.ofSeconds(30))
			    .responseTimeout(Duration.ofSeconds(45))
			    .build();
	    Request request = Request.builder()
	    	    .method(Request.Method.GET)
	    	    .path("/v2/_catalog")
	    	    .build();
    	try (Response response = httpClient2.execute(request)) {
    		try {
				System.out.println(org.apache.commons.io.IOUtils.toString(response.getBody(), StandardCharsets.UTF_8));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	}
	}
}
