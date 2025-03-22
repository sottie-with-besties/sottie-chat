package com.sottie.sottiechat.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketTransportRegistration;

@Configuration
@RequiredArgsConstructor
@EnableWebSocketMessageBroker
@EnableConfigurationProperties(RabbitConfigProperties.class)
public class StompWebSocketConfig implements WebSocketMessageBrokerConfigurer {
    private final WebSocketInterceptor interceptor;
    private final RabbitConfigProperties properties;
    private static final int STOMP_PORT = 61613;

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/chat")
                .setAllowedOriginPatterns("*");
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.setPathMatcher(new AntPathMatcher("."));
        registry.setApplicationDestinationPrefixes("/pub");
        registry.enableStompBrokerRelay("/queue", "/topic", "/exchange", "/amq/queue")
                .setRelayHost(properties.getHost())
                .setRelayPort(STOMP_PORT)
                .setSystemLogin(properties.getUsername())
                .setSystemPasscode(properties.getPassword())
                .setClientLogin(properties.getUsername())
                .setClientPasscode(properties.getPassword())
                .setVirtualHost("/")
                .setSystemHeartbeatSendInterval(30000) // Heartbeat 전송 주기
                .setSystemHeartbeatReceiveInterval(30000); // Heartbeat 수신 주기
    }

    @Override
    public void configureWebSocketTransport(WebSocketTransportRegistration registry) {
        registry.setMessageSizeLimit(128 * 1024); // 메시지 전송 크기 제한
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(interceptor);
    }
}
