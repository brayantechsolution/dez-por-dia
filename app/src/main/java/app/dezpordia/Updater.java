package app.dezpordia;

import android.app.NotificationManager;
import android.app.WallpaperManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * Coloca a próxima palavra no fundo da TELA DE BLOQUEIO.
 * Só a tela de bloqueio muda: nada aparece por cima de outro app e não há notificação.
 */
public final class Updater {

    public enum Result { SHOWN, OFF, REST, FOCUS, ERROR }

    private Updater() {}

    public static synchronized Result tick(Context context, boolean force) {
        SharedPreferences p = Prefs.get(context);
        Prefs.ensureStart(p);
        LocalDate today = LocalDate.now();

        if (!force) {
            if (!p.getBoolean(Prefs.ENABLED, false)) return Result.OFF;
            if (Prefs.isRestDay(p, today) || Prefs.isQuietTime(p, LocalTime.now())) {
                clearLock(context);
                return Result.REST;
            }
            if (p.getBoolean(Prefs.PAUSE_FOCUS, true) && inFocusMode(context)) {
                return Result.FOCUS;
            }
        }

        long day = today.toEpochDay();
        boolean sameDay = p.getLong(Prefs.LAST_DAY, -1) == day;
        int pointer = sameDay ? p.getInt(Prefs.POINTER, 0) : 0;
        int index = pointer % Words.PER_DAY;
        int seen = Math.max(Prefs.seenToday(p, today), Math.min(pointer + 1, Words.PER_DAY));

        List<Words.Word> words = Words.forDay(context, Prefs.studyDayIndex(p, today));
        try {
            Words.Word word = words.get(index);
            Bitmap bitmap = WallpaperPainter.paint(context, word, index + 1, seen);
            WallpaperManager.getInstance(context)
                    .setBitmap(bitmap, null, true, WallpaperManager.FLAG_LOCK);
            bitmap.recycle();
            LockCard.show(context, word, index + 1);
        } catch (Exception e) {
            return Result.ERROR;
        }

        p.edit()
                .putLong(Prefs.LAST_DAY, day)
                .putInt(Prefs.POINTER, pointer + 1)
                .putInt(Prefs.SEEN, seen)
                .putBoolean(Prefs.LOCK_SET, true)
                .commit();
        WidgetProvider.refresh(context);
        return Result.SHOWN;
    }

    /** Devolve a tela de bloqueio ao fundo normal do celular. */
    public static void clearLock(Context context) {
        LockCard.cancel(context);
        SharedPreferences p = Prefs.get(context);
        if (!p.getBoolean(Prefs.LOCK_SET, false)) return;
        try {
            WallpaperManager.getInstance(context).clear(WallpaperManager.FLAG_LOCK);
        } catch (Exception ignored) {
            // segue mesmo assim
        }
        p.edit().putBoolean(Prefs.LOCK_SET, false).commit();
        WidgetProvider.refresh(context);
    }

    /** Modo foco / não perturbe ligado (inclui modos de direção, sono, reunião). */
    static boolean inFocusMode(Context context) {
        NotificationManager nm = context.getSystemService(NotificationManager.class);
        return nm != null
                && nm.getCurrentInterruptionFilter() != NotificationManager.INTERRUPTION_FILTER_ALL
                && nm.getCurrentInterruptionFilter() != NotificationManager.INTERRUPTION_FILTER_UNKNOWN;
    }
}
