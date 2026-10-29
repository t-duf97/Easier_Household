package be.duft.easier_household.config;

import be.duft.easier_household.integration.mqtt.MqttConnection;
import be.duft.easier_household.integration.mqtt.MqttProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Active la connexion MQTT avec {@code mqtt.enabled=true}. Sans elle, l'application démarre sans broker.
 */
@Configuration
@ConditionalOnBooleanProperty("mqtt.enabled")
@EnableConfigurationProperties(MqttProperties.class)
public class MqttConfig {

    @Bean
    public MqttConnection mqttConnection(MqttProperties properties) {
        return new MqttConnection(properties);
    }
}
