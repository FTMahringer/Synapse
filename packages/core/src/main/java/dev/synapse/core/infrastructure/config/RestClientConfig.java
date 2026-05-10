package dev.synapse.core.infrastructure.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Configuration
public class RestClientConfig {

    @Bean(destroyMethod = "shutdown")
    public ExecutorService httpClientExecutor(
        @Value("${synapse.http.pool.max-threads:64}") int maxThreads
    ) {
        return Executors.newFixedThreadPool(maxThreads);
    }

    @Bean
    public RestClient pooledRestClient(
        ExecutorService httpClientExecutor,
        @Value("${synapse.http.connect-timeout-seconds:10}") int connectTimeoutSeconds
    ) {
        HttpClient httpClient = HttpClient.newBuilder()
            .executor(httpClientExecutor)
            .connectTimeout(Duration.ofSeconds(connectTimeoutSeconds))
            .version(HttpClient.Version.HTTP_1_1)
            .build();

        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
        return RestClient.builder()
            .requestFactory(requestFactory)
            .build();
    }
}
