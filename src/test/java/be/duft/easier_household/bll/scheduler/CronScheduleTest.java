package be.duft.easier_household.bll.scheduler;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class CronScheduleTest {

    private static final LocalDateTime WEDNESDAY_0715 = LocalDateTime.of(2026, 10, 28, 7, 15, 42);

    @Test
    void fiveFieldExpression_matchesItsMinuteOnWeekdays() {
        assertThat(CronSchedule.matchesMinute("15 7 * * MON-FRI", WEDNESDAY_0715)).isTrue();
        assertThat(CronSchedule.matchesMinute("15 7 * * MON-FRI", WEDNESDAY_0715.plusMinutes(1))).isFalse();
        assertThat(CronSchedule.matchesMinute("15 7 * * MON-FRI", WEDNESDAY_0715.plusDays(3))).as("samedi").isFalse();
    }

    @Test
    void stepExpression_matchesEveryQuarterHour() {
        assertThat(CronSchedule.matchesMinute("*/15 * * * *", WEDNESDAY_0715)).isTrue();
        assertThat(CronSchedule.matchesMinute("*/15 * * * *", WEDNESDAY_0715.withMinute(30))).isTrue();
        assertThat(CronSchedule.matchesMinute("*/15 * * * *", WEDNESDAY_0715.withMinute(31))).isFalse();
    }

    @Test
    void sixFieldSpringExpression_isAccepted() {
        assertThat(CronSchedule.isValid("0 15 7 * * MON-FRI")).isTrue();
        assertThat(CronSchedule.matchesMinute("0 15 7 * * MON-FRI", WEDNESDAY_0715)).isTrue();
    }

    @Test
    void invalidExpressions_areRejected() {
        assertThat(CronSchedule.isValid("tous les matins")).isFalse();
        assertThat(CronSchedule.isValid("61 7 * * *")).isFalse();
        assertThat(CronSchedule.isValid(null)).isFalse();
    }
}
