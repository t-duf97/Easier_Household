package be.duft.easier_household.bll.scheduler;

import org.springframework.scheduling.support.CronExpression;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * Expressions cron des triggers TIME. Accepte le format classique à 5 champs
 * (minute heure jour mois jour-de-semaine, ex. {@code 15 7 * * MON-FRI}) et le format Spring à 6 champs
 * (secondes en tête). Le moteur évalue à la minute : les secondes de l'expression sont ignorées.
 */
public final class CronSchedule {

    private CronSchedule() {
    }

    public static boolean isValid(String expression) {
        return expression != null && CronExpression.isValidExpression(normalize(expression));
    }

    /**
     * Vrai si l'expression a une occurrence dans la minute qui contient {@code at}.
     */
    public static boolean matchesMinute(String expression, LocalDateTime at) {
        LocalDateTime minuteStart = at.truncatedTo(ChronoUnit.MINUTES);
        LocalDateTime next = CronExpression.parse(normalize(expression)).next(minuteStart.minusNanos(1));
        return next != null && next.isBefore(minuteStart.plusMinutes(1));
    }

    private static String normalize(String expression) {
        String trimmed = expression.trim();
        return trimmed.split("\\s+").length == 5 ? "0 " + trimmed : trimmed;
    }
}
