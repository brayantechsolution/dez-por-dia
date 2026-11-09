package app.dezpordia;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Icon;
import android.os.Build;

/**
 * Cartão silencioso na tela de bloqueio com os botões "Já vi" e "Próxima".
 * Sem som, sem vibrar e sem pular por cima de outro app: só fica parado na tela de bloqueio.
 */
public final class LockCard {

    private static final String CHANNEL = "tela_bloqueio_v2";
    private static final String OLD_CHANNEL = "tela_bloqueio";
    private static final int ID = 1;

    private LockCard() {}

    public static void show(Context context, Words.Word word, int number) {
        if (!canPost(context)) return;
        NotificationManager nm = context.getSystemService(NotificationManager.class);
        if (nm == null) return;
        createChannel(nm);

        Intent open = new Intent(context, MainActivity.class)
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent openPi = PendingIntent.getActivity(context, 0, open,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

        Notification n = new Notification.Builder(context, CHANNEL)
                .setSmallIcon(R.drawable.ic_stat_dez)
                .setContentTitle((word.past.isEmpty() ? word.en : word.en + " (" + word.past + ")")
                        + " · " + word.pt)
                .setContentText((word.pastRound ? "Revisão no passado " : "Palavra ")
                        + number + " de " + Words.PER_DAY)
                .setVisibility(Notification.VISIBILITY_PUBLIC)
                .setCategory(Notification.CATEGORY_STATUS)
                .setOnlyAlertOnce(true)
                .setShowWhen(false)
                .setContentIntent(openPi)
                .setDeleteIntent(action(context, ActionReceiver.ACTION_SEEN, 1))
                .addAction(new Notification.Action.Builder(
                        Icon.createWithResource(context, R.drawable.ic_stat_dez),
                        "Meu fundo", action(context, ActionReceiver.ACTION_SEEN, 2)).build())
                .addAction(new Notification.Action.Builder(
                        Icon.createWithResource(context, R.drawable.ic_stat_dez),
                        "Próxima", action(context, ActionReceiver.ACTION_NEXT, 3)).build())
                .build();
        nm.notify(ID, n);
    }

    public static void cancel(Context context) {
        NotificationManager nm = context.getSystemService(NotificationManager.class);
        if (nm != null) nm.cancel(ID);
    }

    static boolean canPost(Context context) {
        return Build.VERSION.SDK_INT < 33
                || context.checkSelfPermission("android.permission.POST_NOTIFICATIONS")
                == PackageManager.PERMISSION_GRANTED;
    }

    private static PendingIntent action(Context context, String action, int code) {
        Intent i = new Intent(context, ActionReceiver.class).setAction(action);
        return PendingIntent.getBroadcast(context, code, i,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private static void createChannel(NotificationManager nm) {
        if (nm.getNotificationChannel(OLD_CHANNEL) != null) nm.deleteNotificationChannel(OLD_CHANNEL);
        if (nm.getNotificationChannel(CHANNEL) != null) return;
        // IMPORTANCE_DEFAULT aparece na tela de bloqueio (o Android esconde os de importância baixa).
        // Continua sem som e sem vibrar, e não pula por cima de outros apps (isso só o HIGH faz).
        NotificationChannel ch = new NotificationChannel(CHANNEL, "Palavra na tela de bloqueio",
                NotificationManager.IMPORTANCE_DEFAULT);
        ch.setDescription("Botões Já vi e Próxima. Silencioso: sem som e sem vibrar.");
        ch.setSound(null, null);
        ch.enableVibration(false);
        ch.enableLights(false);
        ch.setShowBadge(false);
        ch.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
        nm.createNotificationChannel(ch);
    }
}
