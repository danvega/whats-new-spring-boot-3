package dev.danvega.boot;

import dev.danvega.boot.model.Post;
import dev.danvega.boot.repository.PostRepository;
import dev.danvega.boot.service.JsonPlaceholderService;
import dev.danvega.boot.service.PostService;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.support.WebClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

import java.util.List;

@SpringBootApplication
public class Application {

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

	@Bean
	CommandLineRunner commandLineRunner(PostRepository postRepository, ObservationRegistry observationRegistry) {
		return args -> {
			WebClient client = WebClient.builder().baseUrl("https://jsonplaceholder.typicode.com").build();
			HttpServiceProxyFactory factory = HttpServiceProxyFactory.builder(WebClientAdapter.forClient(client)).build();
			JsonPlaceholderService jps = factory.createClient(JsonPlaceholderService.class);

			List<Post> posts = Observation
					.createNotStarted("json-place-holder.load-posts", observationRegistry)
					.lowCardinalityKeyValue("some-value", "100")
					.observe(jps::loadPosts);

			Observation
					.createNotStarted("post-repository.save-all",observationRegistry)
					.observe(() -> postRepository.saveAll(posts));
		};
	}



}
