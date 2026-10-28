package be.duft.easier_household.bll.scheduler;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SensorSimulatorSchedulerTest {

    @Test
    void nextTemperature_addsStepAndRoundsToTenth() {
        assertThat(SensorSimulatorScheduler.nextTemperature("21.0", 0.34)).isEqualTo(21.3);
        assertThat(SensorSimulatorScheduler.nextTemperature("21.0", -0.46)).isEqualTo(20.5);
    }

    @Test
    void nextTemperature_staysWithinBounds() {
        assertThat(SensorSimulatorScheduler.nextTemperature("24.9", 0.5)).isEqualTo(25.0);
        assertThat(SensorSimulatorScheduler.nextTemperature("18.1", -0.5)).isEqualTo(18.0);
        assertThat(SensorSimulatorScheduler.nextTemperature("40", 0.0)).isEqualTo(25.0);
    }

    @Test
    void nextTemperature_startsFromDefaultWhenValueIsMissingOrUnreadable() {
        assertThat(SensorSimulatorScheduler.nextTemperature(null, 0.0)).isEqualTo(21.0);
        assertThat(SensorSimulatorScheduler.nextTemperature("21°C", 0.0)).isEqualTo(21.0);
    }
}
