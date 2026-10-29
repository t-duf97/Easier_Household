package be.duft.easier_household.integration.mqtt;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.images.builder.Transferable;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * MqttConnection face à un vrai Mosquitto avec authentification (même image qu'en dev).
 */
@Testcontainers
class MqttConnectionTest {

    private static final String USER = "test-user";
    private static final String PASSWORD = "test-password";

    @Container
    static final GenericContainer<?> MOSQUITTO = new GenericContainer<>("eclipse-mosquitto:2")
            .withCopyToContainer(Transferable.of("""
                    listener 1883
                    allow_anonymous false
                    password_file /mosquitto/config/passwd
                    """), "/mosquitto/config/mosquitto.conf")
            .withCommand("sh", "-c", "mosquitto_passwd -b -c /mosquitto/config/passwd " + USER + " " + PASSWORD
                    + " && chown mosquitto /mosquitto/config/passwd"
                    + " && exec mosquitto -c /mosquitto/config/mosquitto.conf")
            .withExposedPorts(1883)
            .waitingFor(Wait.forLogMessage(".*mosquitto version .* running.*", 1));

    private final List<MqttConnection> connections = new ArrayList<>();

    @AfterEach
    void tearDown() {
        connections.forEach(MqttConnection::stop);
    }

    @Test
    void subscriptionRegisteredBeforeConnecting_receivesMessages() throws Exception {
        MqttConnection subscriber = connection("subscriber", PASSWORD);
        CompletableFuture<String> received = new CompletableFuture<>();
        subscriber.subscribe("zigbee2mqtt/+", (topic, payload) -> received.complete(topic + " " + payload));
        subscriber.start();

        MqttConnection publisher = started("publisher", PASSWORD);
        awaitUntil(subscriber::isConnected);
        Thread.sleep(200);
        publisher.publish("zigbee2mqtt/prise_salon", "{\"state\":\"ON\"}").get(5, TimeUnit.SECONDS);

        assertThat(received.get(5, TimeUnit.SECONDS)).isEqualTo("zigbee2mqtt/prise_salon {\"state\":\"ON\"}");
    }

    @Test
    void failingHandler_doesNotStopLaterMessages() throws Exception {
        MqttConnection subscriber = started("subscriber", PASSWORD);
        CompletableFuture<String> received = new CompletableFuture<>();
        subscriber.subscribe("test/robust", (topic, payload) -> {
            if (payload.equals("boom")) throw new IllegalStateException("handler failure");
            received.complete(payload);
        });
        MqttConnection publisher = started("publisher", PASSWORD);
        Thread.sleep(200);

        publisher.publish("test/robust", "boom").get(5, TimeUnit.SECONDS);
        publisher.publish("test/robust", "ok").get(5, TimeUnit.SECONDS);

        assertThat(received.get(5, TimeUnit.SECONDS)).isEqualTo("ok");
        assertThat(subscriber.isConnected()).isTrue();
    }

    @Test
    void wrongPassword_neverConnectsAndPublishFailsFast() throws Exception {
        MqttConnection intruder = connection("intruder", "wrong-password");
        intruder.start();
        Thread.sleep(1_500);

        assertThat(intruder.isConnected()).isFalse();
        assertThatThrownBy(() -> intruder.publish("zigbee2mqtt/prise_salon/set", "{\"state\":\"ON\"}").join())
                .hasCauseInstanceOf(IllegalStateException.class);
    }

    private MqttConnection started(String clientId, String password) throws InterruptedException {
        MqttConnection connection = connection(clientId, password);
        connection.start();
        awaitUntil(connection::isConnected);
        return connection;
    }

    private MqttConnection connection(String clientId, String password) {
        MqttConnection connection = new MqttConnection(new MqttProperties(
                true, MOSQUITTO.getHost(), MOSQUITTO.getMappedPort(1883), clientId, USER, password));
        connections.add(connection);
        return connection;
    }

    private static void awaitUntil(BooleanSupplier condition) throws InterruptedException {
        long deadline = System.nanoTime() + Duration.ofSeconds(10).toNanos();
        while (!condition.getAsBoolean()) {
            assertThat(System.nanoTime()).as("condition not met within 10 s").isLessThan(deadline);
            Thread.sleep(50);
        }
    }
}
