package com.reliaquest.api.config;

import io.netty.channel.ChannelOption;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

@Configuration
@EnableRetry
@Slf4j
public class AppConfig {

    @Value("${web-client.config.connection-timeout:500000}")
    private int connectionTimeoutMillis;

    @Value("${web-client.config.read-timeout:200000}")
    private int readTimeoutMillis;

    @Value("${web-client.config.write-timeout:200000}")
    private int writeTimeoutMillis;

    @Value("${web-client.config.response-timeout:200000}")
    private int responseTimeoutMillis;

    @Bean
    public WebClient getWebClient() throws Exception {
        try {
            SslContext sslContext = SslContextBuilder.forClient()
                    .trustManager(InsecureTrustManagerFactory.INSTANCE)
                    .build();
            final ExchangeStrategies strategies = ExchangeStrategies.builder()
                    .codecs(codecs -> codecs.defaultCodecs().maxInMemorySize(-1))
                    .build();
            HttpClient httpClient = HttpClient.create()
                    .secure(t -> t.sslContext(sslContext))
                    .option(ChannelOption.SO_KEEPALIVE, true)
                    .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, connectionTimeoutMillis)
                    .responseTimeout(Duration.ofMillis(responseTimeoutMillis))
                    .doOnConnected(conn -> conn.addHandlerLast(
                                    new ReadTimeoutHandler(readTimeoutMillis, TimeUnit.MILLISECONDS))
                            .addHandlerLast(new WriteTimeoutHandler(writeTimeoutMillis, TimeUnit.MILLISECONDS)));

            return WebClient.builder()
                    .exchangeStrategies(strategies)
                    .clientConnector(new ReactorClientHttpConnector(httpClient))
                    .build();
        } catch (Exception e) {
            log.error("Fatal error, Exception occurred while creating WebClient", e);
            throw e;
        }
    }
}
