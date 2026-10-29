package be.duft.easier_household.integration.mqtt;

import com.hivemq.client.mqtt.MqttClient;
import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.mqtt5.Mqtt5AsyncClient;
import com.hivemq.client.mqtt.mqtt5.Mqtt5ClientBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.SmartLifecycle;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;

/**
 * Connexion unique de l'application au broker MQTT.
 * <p>
 * Se reconnecte seule (broker absent au démarrage, redémarré, réseau coupé) et rejoue
 * tous les abonnements à chaque connexion : un abonnement peut donc être enregistré
 * avant que le broker soit joignable.
 */
@Slf4j
public class MqttConnection implements SmartLifecycle {

    private final Mqtt5AsyncClient client;
    private final MqttProperties properties;
    private final Map<String, BiConsumer<String, String>> subscriptions = new ConcurrentHashMap<>();
    private volatile boolean running;

    public MqttConnection(MqttProperties properties) {
        this.properties = properties;
        Mqtt5ClientBuilder builder = MqttClient.builder()
                .useMqttVersion5()
                .identifier(properties.clientId())
                .serverHost(properties.host())
                .serverPort(properties.port())
                .automaticReconnectWithDefaultConfig()
                .addConnectedListener(context -> {
                    log.info("MQTT connected to {}:{}", properties.host(), properties.port());
                    subscriptions.forEach(this::sendSubscribe);
                })
                .addDisconnectedListener(context -> {
                    if (!running) {
                        context.getReconnector().reconnect(false);
                        return;
                    }
                    log.warn("MQTT broker {}:{} unreachable ({}), retrying",
                            properties.host(), properties.port(), context.getCause().getMessage());
                });
        if (properties.username() != null) {
            builder = builder.simpleAuth()
                    .username(properties.username())
                    .password(properties.password() == null ? new byte[0] : properties.password().getBytes(StandardCharsets.UTF_8))
                    .applySimpleAuth();
        }
        this.client = builder.buildAsync();
    }

    /**
     * Abonne {@code handler(topic, payload)} au filtre (jokers MQTT {@code +} et {@code #} acceptés).
     * Une exception levée par le handler est journalisée, sans couper la connexion.
     */
    public void subscribe(String topicFilter, BiConsumer<String, String> handler) {
        subscriptions.put(topicFilter, handler);
        if (isConnected()) {
            sendSubscribe(topicFilter, handler);
        }
    }

    /**
     * Publie en QoS 1. Échoue immédiatement si le broker n'est pas joignable : un message
     * n'est jamais mis en attente pour être livré plus tard.
     */
    public CompletableFuture<Void> publish(String topic, String payload) {
        if (!isConnected()) {
            return CompletableFuture.failedFuture(new IllegalStateException("MQTT broker not connected"));
        }
        return client.publishWith()
                .topic(topic)
                .payload(payload.getBytes(StandardCharsets.UTF_8))
                .qos(MqttQos.AT_LEAST_ONCE)
                .send()
                .thenApply(result -> null);
    }

    public boolean isConnected() {
        return client.getState().isConnected();
    }

    @Override
    public void start() {
        running = true;
        client.connectWith().cleanStart(true).send();
    }

    @Override
    public void stop() {
        running = false;
        client.disconnect()
                .orTimeout(5, TimeUnit.SECONDS)
                .exceptionally(error -> null)
                .join();
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    private void sendSubscribe(String topicFilter, BiConsumer<String, String> handler) {
        client.subscribeWith()
                .topicFilter(topicFilter)
                .qos(MqttQos.AT_LEAST_ONCE)
                .callback(publish -> {
                    String topic = publish.getTopic().toString();
                    try {
                        handler.accept(topic, new String(publish.getPayloadAsBytes(), StandardCharsets.UTF_8));
                    } catch (RuntimeException e) {
                        log.error("MQTT handler failed for topic {}", topic, e);
                    }
                })
                .send()
                .whenComplete((ack, error) -> {
                    if (error != null) {
                        log.warn("MQTT subscribe to {} failed on {}:{}", topicFilter, properties.host(), properties.port(), error);
                    }
                });
    }
}
