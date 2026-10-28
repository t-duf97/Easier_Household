package be.duft.easier_household.domain.sun;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Valeurs de référence issues des éphémérides publiées, avec une tolérance de 3 minutes.
 */
class SunCalculatorTest {

    private static final double BRUSSELS_LAT = 50.8503;
    private static final double BRUSSELS_LON = 4.3517;
    private static final Duration TOLERANCE = Duration.ofMinutes(3);

    @Test
    @DisplayName("Bruxelles au solstice d'été : lever ~03:29 UTC, coucher ~19:59 UTC")
    void brusselsSummerSolstice() {
        LocalDate date = LocalDate.of(2026, 6, 21);
        assertClose(SunCalculator.sunrise(date, BRUSSELS_LAT, BRUSSELS_LON).orElseThrow(), "2026-06-21T03:29:00Z");
        assertClose(SunCalculator.sunset(date, BRUSSELS_LAT, BRUSSELS_LON).orElseThrow(), "2026-06-21T19:59:00Z");
    }

    @Test
    @DisplayName("Bruxelles au solstice d'hiver : lever ~07:44 UTC, coucher ~15:39 UTC")
    void brusselsWinterSolstice() {
        LocalDate date = LocalDate.of(2026, 12, 21);
        assertClose(SunCalculator.sunrise(date, BRUSSELS_LAT, BRUSSELS_LON).orElseThrow(), "2026-12-21T07:44:00Z");
        assertClose(SunCalculator.sunset(date, BRUSSELS_LAT, BRUSSELS_LON).orElseThrow(), "2026-12-21T15:39:00Z");
    }

    @Test
    @DisplayName("Équateur, méridien de Greenwich, équinoxe : lever ~06:04 UTC, coucher ~18:11 UTC")
    void equatorAtEquinox() {
        LocalDate date = LocalDate.of(2026, 3, 20);
        assertClose(SunCalculator.sunrise(date, 0, 0).orElseThrow(), "2026-03-20T06:04:00Z");
        assertClose(SunCalculator.sunset(date, 0, 0).orElseThrow(), "2026-03-20T18:11:00Z");
    }

    @Test
    @DisplayName("Tromsø : pas de lever en nuit polaire, pas de coucher sous le soleil de minuit")
    void polarNightAndMidnightSun() {
        assertThat(SunCalculator.sunrise(LocalDate.of(2026, 12, 21), 69.65, 18.96)).isEmpty();
        assertThat(SunCalculator.sunset(LocalDate.of(2026, 6, 21), 69.65, 18.96)).isEmpty();
    }

    private static void assertClose(Instant actual, String expected) {
        assertThat(Duration.between(Instant.parse(expected), actual).abs()).isLessThanOrEqualTo(TOLERANCE);
    }
}
