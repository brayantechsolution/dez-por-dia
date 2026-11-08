package app.dezpordia;

import android.content.Context;
import android.content.SharedPreferences;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;

/** Configurações e progresso do usuário. */
public final class Prefs {

    public static final String ENABLED = "enabled";
    public static final String START_DAY = "start_day";
    public static final String INTERVAL_HOURS = "interval_hours";
    public static final String QUIET = "quiet";
    public static final String WEEKDAYS_ONLY = "weekdays_only";
    public static final String PAUSE_FOCUS = "pause_focus";
    public static final String LAST_DAY = "last_day";
    public static final String POINTER = "pointer";
    public static final String SEEN = "seen";
    public static final String LOCK_SET = "lock_set";
    /** Dias pulados (ou voltados) à mão pela pessoa. */
    public static final String DAY_OFFSET = "day_offset";
    public static final int TOTAL_DAYS = 130;

    public static final int QUIET_START = 22;
    public static final int QUIET_END = 7;

    private Prefs() {}

    public static SharedPreferences get(Context context) {
        return context.getSharedPreferences("dez_por_dia", Context.MODE_PRIVATE);
    }

    /** Garante que exista uma data de início. */
    public static void ensureStart(SharedPreferences p) {
        if (!p.contains(START_DAY)) {
            p.edit().putLong(START_DAY, LocalDate.now().toEpochDay()).apply();
        }
    }

    public static boolean isWeekday(LocalDate d) {
        DayOfWeek w = d.getDayOfWeek();
        return w != DayOfWeek.SATURDAY && w != DayOfWeek.SUNDAY;
    }

    /** Quantos dias de estudo já passaram antes de hoje (0 no primeiro dia). */
    public static int studyDayIndex(SharedPreferences p, LocalDate today) {
        LocalDate start = LocalDate.ofEpochDay(p.getLong(START_DAY, today.toEpochDay()));
        boolean weekdaysOnly = p.getBoolean(WEEKDAYS_ONLY, true);
        int count = 0;
        for (LocalDate d = start; d.isBefore(today); d = d.plusDays(1)) {
            if (!weekdaysOnly || isWeekday(d)) count++;
        }
        return Math.max(0, count + p.getInt(DAY_OFFSET, 0));
    }

    /**
     * Muda o dia de estudo de hoje para {@code targetIndex} (0 = dia 1) e recomeça as 10 palavras.
     * Usado pelos botões "dia anterior" e "próximo dia".
     */
    public static void setStudyDay(SharedPreferences p, LocalDate today, int targetIndex) {
        int target = Math.max(0, Math.min(TOTAL_DAYS - 1, targetIndex));
        int current = studyDayIndex(p, today);
        int offset = p.getInt(DAY_OFFSET, 0) + (target - current);
        p.edit()
                .putInt(DAY_OFFSET, offset)
                .putLong(LAST_DAY, today.toEpochDay())
                .putInt(POINTER, 0)
                .putInt(SEEN, 0)
                .commit();
    }

    /** Hoje é dia de descanso (fim de semana com "só dias úteis" ligado)? */
    public static boolean isRestDay(SharedPreferences p, LocalDate today) {
        return p.getBoolean(WEEKDAYS_ONLY, true) && !isWeekday(today);
    }

    /** Está no horário de descanso (22h às 7h)? */
    public static boolean isQuietTime(SharedPreferences p, LocalTime now) {
        if (!p.getBoolean(QUIET, true)) return false;
        int h = now.getHour();
        return h >= QUIET_START || h < QUIET_END;
    }

    /** Quantas palavras de hoje já apareceram na tela. */
    public static int seenToday(SharedPreferences p, LocalDate today) {
        return p.getLong(LAST_DAY, -1) == today.toEpochDay() ? p.getInt(SEEN, 0) : 0;
    }
}
