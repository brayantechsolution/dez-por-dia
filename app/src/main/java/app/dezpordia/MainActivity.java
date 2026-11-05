package app.dezpordia;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

public class MainActivity extends Activity {

    private static final int INK = 0xFF0F1B2D;
    private static final int MUTED = 0xFF5A6270;
    private static final int GROUND = 0xFFF4F5F0;
    private static final int CARD = 0xFFFFFFFF;
    private static final int LIME = 0xFFC6F432;
    private static final int LINE = 0xFFEEF0EA;
    private static final int TRACK = 0xFFE1E4DA;
    private static final int NAVY_GREY = 0xFFA9B4C4;

    private static final int[] HOURS = {1, 2, 3};

    private SharedPreferences prefs;
    private LinearLayout content;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = Prefs.get(this);
        Prefs.ensureStart(prefs);

        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundColor(GROUND);
        scroll.setFillViewport(true);
        scroll.setClipToPadding(false);

        content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(dp(24), dp(24), dp(24), dp(32));
        scroll.addView(content);

        scroll.setOnApplyWindowInsetsListener((v, insets) -> {
            v.setPadding(insets.getSystemWindowInsetLeft(), insets.getSystemWindowInsetTop(),
                    insets.getSystemWindowInsetRight(), insets.getSystemWindowInsetBottom());
            return insets;
        });
        setContentView(scroll);
    }

    @Override
    protected void onResume() {
        super.onResume();
        render();
    }

    private void render() {
        content.removeAllViews();
        LocalDate today = LocalDate.now();
        int dayIndex = Prefs.studyDayIndex(prefs, today);
        int seen = Prefs.seenToday(prefs, today);
        boolean restDay = Prefs.isRestDay(prefs, today);
        boolean enabled = prefs.getBoolean(Prefs.ENABLED, false);

        // Cabeçalho
        String date = today.format(DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM",
                Locale.forLanguageTag("pt-BR")));
        content.addView(text(capitalize(date), 14, MUTED, false));
        content.addView(text(restDay ? "Hoje é dia de descanso" : "Suas 10 palavras de hoje",
                28, INK, true), margins(dp(4), 0));

        // Progresso do dia
        ProgressBar bar = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        bar.setMax(Words.PER_DAY);
        bar.setProgress(seen);
        bar.setProgressTintList(ColorStateList.valueOf(INK));
        bar.setProgressBackgroundTintList(ColorStateList.valueOf(TRACK));
        LinearLayout.LayoutParams barLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(10));
        barLp.topMargin = dp(18);
        content.addView(bar, barLp);
        content.addView(text(restDay
                        ? "Sem frases hoje. Estas são as palavras do próximo dia útil."
                        : seen + " de 10 já apareceram na sua tela", 13, MUTED, false),
                margins(dp(6), 0));

        // Lista das 10 palavras
        LinearLayout list = card(CARD);
        list.setPadding(dp(16), dp(4), dp(16), dp(4));
        List<Words.Word> words = Words.forDay(this, dayIndex);
        for (int i = 0; i < words.size(); i++) {
            if (i > 0) list.addView(divider());
            list.addView(wordRow(i + 1, words.get(i), !restDay && i < seen));
        }
        content.addView(list, margins(dp(18), 0));

        // Meta de 6 meses
        int learned = Math.min(1300, dayIndex * Words.PER_DAY + seen);
        LinearLayout goal = card(INK);
        goal.setPadding(dp(18), dp(16), dp(18), dp(18));
        LinearLayout goalTop = new LinearLayout(this);
        goalTop.setGravity(Gravity.CENTER_VERTICAL);
        TextView goalLabel = text("Meta de 6 meses", 13, NAVY_GREY, false);
        goalTop.addView(goalLabel, new LinearLayout.LayoutParams(0,
                LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        goalTop.addView(text(formatNumber(learned) + " de 1.300", 20, CARD, true));
        goal.addView(goalTop);
        ProgressBar goalBar = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        goalBar.setMax(1300);
        goalBar.setProgress(learned);
        goalBar.setProgressTintList(ColorStateList.valueOf(LIME));
        goalBar.setProgressBackgroundTintList(ColorStateList.valueOf(0xFF2A3A52));
        LinearLayout.LayoutParams gLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(8));
        gLp.topMargin = dp(12);
        goal.addView(goalBar, gLp);
        content.addView(goal, margins(dp(16), 0));

        // Botões
        Button toggle = button(enabled ? "Desativar e voltar o fundo normal"
                : "Ativar na tela de bloqueio", !enabled);
        toggle.setOnClickListener(v -> toggle());
        content.addView(toggle, margins(dp(24), 0));

        Button next = button("Mostrar a próxima palavra agora", false);
        next.setOnClickListener(v -> showNext());
        content.addView(next, margins(dp(10), 0));

        // Regras para não incomodar
        content.addView(text("NÃO INCOMODAR", 12, MUTED, true), margins(dp(28), dp(8)));
        LinearLayout rules = card(CARD);
        rules.setPadding(dp(16), dp(6), dp(16), dp(6));
        rules.addView(toggleRow("Pausar no modo foco",
                "Em reunião, direção ou não perturbe, a troca espera", Prefs.PAUSE_FOCUS));
        rules.addView(divider());
        rules.addView(toggleRow("Horário de descanso",
                "Das 22h às 7h a tela de bloqueio volta ao normal", Prefs.QUIET));
        rules.addView(divider());
        rules.addView(toggleRow("Só dias úteis",
                "Segunda a sexta. Fim de semana é descanso", Prefs.WEEKDAYS_ONLY));
        rules.addView(divider());
        rules.addView(intervalRow());
        content.addView(rules);

        TextView how = text("A frase vai só no fundo da tela de bloqueio. Ela aparece quando "
                + "você liga a tela, nunca por cima de outro app, e não manda notificação. "
                + "Ao desativar, a tela de bloqueio volta a usar o mesmo fundo da tela inicial.",
                13, MUTED, false);
        how.setLineSpacing(0, 1.2f);
        content.addView(how, margins(dp(16), 0));
    }

    private void toggle() {
        boolean enable = !prefs.getBoolean(Prefs.ENABLED, false);
        prefs.edit().putBoolean(Prefs.ENABLED, enable).apply();
        if (enable) {
            Scheduler.schedule(this);
            runUpdate(false);
        } else {
            Scheduler.cancel(this);
            new Thread(() -> {
                Updater.clearLock(getApplicationContext());
                runOnUiThread(() -> {
                    toast("Desativado. A tela de bloqueio voltou ao normal.");
                    render();
                });
            }).start();
        }
    }

    private void showNext() {
        runUpdate(true);
    }

    private void runUpdate(boolean force) {
        new Thread(() -> {
            Updater.Result r = Updater.tick(getApplicationContext(), force);
            runOnUiThread(() -> {
                switch (r) {
                    case SHOWN:
                        toast("Pronto! Bloqueie e ligue a tela para ver.");
                        break;
                    case REST:
                        toast("Agora é horário de descanso. A próxima frase vem depois.");
                        break;
                    case FOCUS:
                        toast("Modo foco ligado. A frase espera ele desligar.");
                        break;
                    case ERROR:
                        toast("Não consegui trocar o fundo neste celular.");
                        break;
                    default:
                        break;
                }
                render();
            });
        }).start();
    }

    // ---------- Pedaços da tela ----------

    private View wordRow(int number, Words.Word word, boolean seen) {
        LinearLayout row = new LinearLayout(this);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setMinimumHeight(dp(56));
        row.setPadding(0, dp(8), 0, dp(8));

        TextView num = text(String.valueOf(number), 13, MUTED, false);
        row.addView(num, new LinearLayout.LayoutParams(dp(28), LinearLayout.LayoutParams.WRAP_CONTENT));

        LinearLayout col = new LinearLayout(this);
        col.setOrientation(LinearLayout.VERTICAL);
        col.addView(text(word.en, 17, INK, true));
        col.addView(text(word.pt, 13, MUTED, false));
        row.addView(col, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

        TextView mark = new TextView(this);
        mark.setGravity(Gravity.CENTER);
        mark.setTextSize(13);
        mark.setTypeface(Typeface.DEFAULT_BOLD);
        GradientDrawable circle = new GradientDrawable();
        circle.setShape(GradientDrawable.OVAL);
        if (seen) {
            circle.setColor(LIME);
            mark.setText("✓");
            mark.setTextColor(INK);
            mark.setContentDescription("já apareceu");
        } else {
            circle.setColor(0x00000000);
            circle.setStroke(dp(2), 0xFFB7BDB0);
            mark.setContentDescription("ainda vai aparecer");
        }
        mark.setBackground(circle);
        row.addView(mark, new LinearLayout.LayoutParams(dp(24), dp(24)));
        return row;
    }

    private View toggleRow(String title, String subtitle, String key) {
        LinearLayout row = new LinearLayout(this);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setMinimumHeight(dp(64));
        row.setPadding(0, dp(8), 0, dp(8));

        LinearLayout col = new LinearLayout(this);
        col.setOrientation(LinearLayout.VERTICAL);
        col.addView(text(title, 15, INK, true));
        col.addView(text(subtitle, 12, MUTED, false));
        row.addView(col, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

        Switch sw = new Switch(this);
        sw.setChecked(prefs.getBoolean(key, true));
        sw.setContentDescription(title);
        sw.setOnCheckedChangeListener((b, checked) -> {
            prefs.edit().putBoolean(key, checked).apply();
            if (Prefs.WEEKDAYS_ONLY.equals(key)) render();
        });
        row.addView(sw);
        row.setOnClickListener(v -> sw.toggle());
        return row;
    }

    private View intervalRow() {
        LinearLayout row = new LinearLayout(this);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setMinimumHeight(dp(56));

        row.addView(text("Trocar a frase a cada", 15, INK, true),
                new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

        Spinner spinner = new Spinner(this);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, new String[]{"1 hora", "2 horas", "3 horas"});
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        int current = prefs.getInt(Prefs.INTERVAL_HOURS, 2);
        for (int i = 0; i < HOURS.length; i++) if (HOURS[i] == current) spinner.setSelection(i);
        spinner.setContentDescription("Intervalo entre as frases");
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                int hours = HOURS[position];
                if (hours == prefs.getInt(Prefs.INTERVAL_HOURS, 2)) return;
                prefs.edit().putInt(Prefs.INTERVAL_HOURS, hours).apply();
                if (prefs.getBoolean(Prefs.ENABLED, false)) Scheduler.schedule(MainActivity.this);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
        row.addView(spinner);
        return row;
    }

    private TextView text(String s, float sp, int color, boolean bold) {
        TextView t = new TextView(this);
        t.setText(s);
        t.setTextSize(sp);
        t.setTextColor(color);
        if (bold) t.setTypeface(Typeface.DEFAULT_BOLD);
        return t;
    }

    private LinearLayout card(int color) {
        LinearLayout c = new LinearLayout(this);
        c.setOrientation(LinearLayout.VERTICAL);
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(color);
        bg.setCornerRadius(dp(20));
        c.setBackground(bg);
        return c;
    }

    private View divider() {
        View v = new View(this);
        v.setBackgroundColor(LINE);
        v.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 1));
        return v;
    }

    private Button button(String label, boolean primary) {
        Button b = new Button(this);
        b.setText(label);
        b.setAllCaps(false);
        b.setTextSize(16);
        b.setTypeface(Typeface.DEFAULT_BOLD);
        b.setTextColor(INK);
        b.setMinHeight(dp(52));
        b.setStateListAnimator(null);
        GradientDrawable bg = new GradientDrawable();
        bg.setCornerRadius(dp(14));
        if (primary) {
            bg.setColor(LIME);
        } else {
            bg.setColor(CARD);
            bg.setStroke(dp(1), 0xFFD3D7CC);
        }
        b.setBackground(bg);
        return b;
    }

    private LinearLayout.LayoutParams margins(int top, int bottom) {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.topMargin = top;
        lp.bottomMargin = bottom;
        return lp;
    }

    private int dp(int v) {
        return Math.round(v * getResources().getDisplayMetrics().density);
    }

    private void toast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
    }

    private static String capitalize(String s) {
        return s.isEmpty() ? s : Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    private static String formatNumber(int n) {
        return String.format(Locale.forLanguageTag("pt-BR"), "%,d", n);
    }
}
