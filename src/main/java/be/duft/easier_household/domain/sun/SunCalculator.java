package be.duft.easier_household.domain.sun;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;

import static java.lang.Math.*;

/**
 * Heures de lever et de coucher du soleil, par l'équation du lever de soleil (approximation NOAA),
 * précise à une ou deux minutes près aux latitudes habitées. Java pur : ni Spring, ni dépendance.
 * <p>
 * {@code date} est la date locale du lieu ; la longitude est positive à l'est de Greenwich.
 * Le résultat est vide pendant une nuit ou un jour polaire (le soleil ne se lève ou ne se couche pas).
 */
public final class SunCalculator {

    private static final double J2000 = 2451545.0;
    private static final double UNIX_EPOCH_JULIAN_DAY = 2440587.5;
    private static final long EPOCH_DAY_OF_J2000 = 10957;
    /** Réfraction atmosphérique et rayon apparent du disque solaire. */
    private static final double SUN_ALTITUDE_AT_HORIZON = -0.833;
    private static final double EARTH_AXIAL_TILT = 23.4397;

    private SunCalculator() {
    }

    public static Optional<Instant> sunrise(LocalDate date, double latitude, double longitude) {
        return event(date, latitude, longitude, true);
    }

    public static Optional<Instant> sunset(LocalDate date, double latitude, double longitude) {
        return event(date, latitude, longitude, false);
    }

    private static Optional<Instant> event(LocalDate date, double latitude, double longitude, boolean rise) {
        double daysSinceJ2000 = date.toEpochDay() - EPOCH_DAY_OF_J2000;
        double meanSolarNoon = daysSinceJ2000 - longitude / 360.0;

        double meanAnomaly = normalizeDegrees(357.5291 + 0.98560028 * meanSolarNoon);
        double m = toRadians(meanAnomaly);
        double equationOfCenter = 1.9148 * sin(m) + 0.0200 * sin(2 * m) + 0.0003 * sin(3 * m);
        double eclipticLongitude = toRadians(normalizeDegrees(meanAnomaly + equationOfCenter + 180 + 102.9372));

        double solarTransit = J2000 + meanSolarNoon + 0.0053 * sin(m) - 0.0069 * sin(2 * eclipticLongitude);
        double sinDeclination = sin(eclipticLongitude) * sin(toRadians(EARTH_AXIAL_TILT));
        double cosDeclination = cos(asin(sinDeclination));

        double lat = toRadians(latitude);
        double cosHourAngle = (sin(toRadians(SUN_ALTITUDE_AT_HORIZON)) - sin(lat) * sinDeclination)
                / (cos(lat) * cosDeclination);
        if (cosHourAngle < -1 || cosHourAngle > 1) {
            return Optional.empty();
        }

        double hourAngleInDays = toDegrees(acos(cosHourAngle)) / 360.0;
        double julianDay = rise ? solarTransit - hourAngleInDays : solarTransit + hourAngleInDays;
        return Optional.of(Instant.ofEpochMilli(round((julianDay - UNIX_EPOCH_JULIAN_DAY) * 86_400_000L)));
    }

    private static double normalizeDegrees(double degrees) {
        double normalized = degrees % 360;
        return normalized < 0 ? normalized + 360 : normalized;
    }
}
