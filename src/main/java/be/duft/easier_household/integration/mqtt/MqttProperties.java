package be.duft.easier_household.integration.mqtt;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * Connexion au broker MQTT ({@code mqtt.*}). {@code username}/{@code password} absents = connexion anonyme.
 */
@ConfigurationProperties("mqtt")
public record MqttProperties(
        boolean enabled,
        @DefaultValue("localhost") String host,
        @DefaultValue("1883") int port,
        @DefaultValue("easier-household-api") String clientId,
        String username,
        String password) {

    @Override
    public String toString() {
        return "MqttProperties[enabled=" + enabled + ", host=" + host + ", port=" + port
                + ", clientId=" + clientId + ", username=" + username + ", password=***]";
    }
}
