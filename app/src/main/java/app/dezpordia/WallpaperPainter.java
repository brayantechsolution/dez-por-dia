package app.dezpordia;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.hardware.display.DisplayManager;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.DisplayMetrics;
import android.view.Display;

/** Desenha a imagem do fundo da tela de bloqueio com a palavra e a frase. */
public final class WallpaperPainter {

    static final int NAVY = 0xFF0F1B2D;
    static final int LIME = 0xFFC6F432;
    static final int GREY = 0xFFA9B4C4;
    static final int LINE = 0xFF2A3A52;
    static final int WHITE = 0xFFFFFFFF;

    /** Um fundo escuro diferente para cada palavra do dia (todos com bom contraste). */
    static final int[] GROUNDS = {
            0xFF0F1B2D, // azul-marinho
            0xFF0E2A2A, // verde-petróleo
            0xFF2A1630, // ameixa
            0xFF142616, // verde-floresta
            0xFF2D1418, // vinho
            0xFF18213F, // azul-ardósia
    };

    private WallpaperPainter() {}

    public static Bitmap paint(Context context, Words.Word word, int number, int seen, boolean reviewRound) {
        int[] size = screenSize(context);
        int width = size[0];
        int height = size[1];
        float u = width / 390f;
        float x = 28 * u;
        int maxW = (int) (width - 56 * u);

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        int ground = GROUNDS[(number - 1 + GROUNDS.length) % GROUNDS.length];
        canvas.drawColor(ground);

        Typeface bold = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD);
        Typeface regular = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL);

        // Selo "PALAVRA 2 DE 10"
        TextPaint chipText = paint(12 * u, ground, bold);
        chipText.setLetterSpacing(0.08f);
        String chip = (reviewRound ? "REVISÃO NO PASSADO · " : "PALAVRA ") + number + " DE " + Words.PER_DAY;
        float chipH = 28 * u;
        float chipW = chipText.measureText(chip) + 24 * u;

        // Palavra grande, reduzida se não couber
        TextPaint wordPaint = paint(56 * u, LIME, bold);
        while (wordPaint.measureText(word.en) > maxW && wordPaint.getTextSize() > 20 * u) {
            wordPaint.setTextSize(wordPaint.getTextSize() * 0.92f);
        }

        // Passado do verbo ao lado da palavra (ask   asked). Se não couber, desce para a linha de baixo.
        boolean hasPast = !word.past.isEmpty();
        TextPaint pastPaint = paint(24 * u, WHITE, bold);
        TextPaint pastLabel = paint(10 * u, GREY, bold);
        pastLabel.setLetterSpacing(0.1f);
        String pastTag = "PASSADO";
        float gap = 16 * u;
        boolean pastInline = false;
        if (hasPast) {
            float stackedW = pastLabel.measureText(pastTag) + 8 * u;
            while (stackedW + pastPaint.measureText(word.past) > maxW && pastPaint.getTextSize() > 14 * u) {
                pastPaint.setTextSize(pastPaint.getTextSize() * 0.92f);
            }
            float need = gap + Math.max(pastPaint.measureText(word.past), pastLabel.measureText(pastTag));
            while (wordPaint.measureText(word.en) + need > maxW && wordPaint.getTextSize() > 40 * u) {
                wordPaint.setTextSize(wordPaint.getTextSize() * 0.92f);
            }
            pastInline = wordPaint.measureText(word.en) + need <= maxW;
        }
        Paint.FontMetrics pfm = pastPaint.getFontMetrics();
        float pastLineH = hasPast && !pastInline ? (pfm.descent - pfm.ascent) + 4 * u : 0;

        Paint.FontMetrics wfm = wordPaint.getFontMetrics();
        float wordH = wfm.descent - wfm.ascent;

        StaticLayout translation = layout(word.pt, paint(18 * u, GREY, regular), maxW);
        StaticLayout sentenceEn = layout(word.sentenceEn, paint(24 * u, WHITE, bold), maxW);
        StaticLayout sentencePt = layout(word.sentencePt, paint(17 * u, GREY, regular), maxW);
        StaticLayout review = word.review.isEmpty() ? null
                : layout("Revisão: " + word.review, paint(14 * u, LIME, regular), maxW);

        float total = chipH + 22 * u + wordH + pastLineH + 6 * u + translation.getHeight() + 20 * u
                + 1 * u + 20 * u + sentenceEn.getHeight() + 8 * u + sentencePt.getHeight()
                + (review != null ? 14 * u + review.getHeight() : 0) + 22 * u + 6 * u;

        // Fica abaixo do relógio e acima dos atalhos da tela de bloqueio
        float y = Math.max(height * 0.36f, height * 0.85f - total);

        // Ilustração da palavra, no espaço entre o relógio e o texto
        if (!word.emoji.isEmpty()) {
            float top = height * 0.25f;
            float bottom = y - 16 * u;
            float area = bottom - top;
            if (area > 60 * u) {
                float cx = width / 2f;
                float cy = top + area / 2f;
                float emojiSize = Math.min(area * 0.58f, width * 0.42f);

                // Halo suave atrás da ilustração, sem passar do espaço livre
                Paint halo = new Paint(Paint.ANTI_ALIAS_FLAG);
                halo.setColor(WHITE);
                halo.setAlpha(18);
                canvas.drawCircle(cx, cy, Math.min(emojiSize * 0.78f, area / 2f), halo);

                Paint emoji = new Paint(Paint.ANTI_ALIAS_FLAG);
                emoji.setTextSize(emojiSize);
                emoji.setTextAlign(Paint.Align.CENTER);
                Paint.FontMetrics efm = emoji.getFontMetrics();
                canvas.drawText(word.emoji, cx, cy - (efm.ascent + efm.descent) / 2f, emoji);
            }
        }

        Paint fill = new Paint(Paint.ANTI_ALIAS_FLAG);
        fill.setColor(LIME);
        canvas.drawRoundRect(x, y, x + chipW, y + chipH, chipH / 2, chipH / 2, fill);
        Paint.FontMetrics cfm = chipText.getFontMetrics();
        canvas.drawText(chip, x + 12 * u, y + chipH / 2 - (cfm.ascent + cfm.descent) / 2, chipText);
        y += chipH + 22 * u;

        float base = y - wfm.ascent;
        canvas.drawText(word.en, x, base, wordPaint);
        if (hasPast && pastInline) {
            float px = x + wordPaint.measureText(word.en) + gap;
            canvas.drawText(word.past, px, base, pastPaint);
            canvas.drawText(pastTag, px, base - pastPaint.getTextSize() * 0.75f - 6 * u, pastLabel);
        }
        y += wordH;
        if (hasPast && !pastInline) {
            float pb = y + 4 * u - pfm.ascent;
            canvas.drawText(pastTag, x, pb, pastLabel);
            canvas.drawText(word.past, x + pastLabel.measureText(pastTag) + 8 * u, pb, pastPaint);
            y += pastLineH;
        }
        y += 6 * u;

        y = draw(canvas, translation, x, y) + 20 * u;

        fill.setColor(LINE);
        canvas.drawRect(x, y, x + maxW, y + Math.max(1f, u), fill);
        y += 1 * u + 20 * u;

        y = draw(canvas, sentenceEn, x, y) + 8 * u;
        y = draw(canvas, sentencePt, x, y);
        if (review != null) y = draw(canvas, review, x + 0, y + 14 * u);
        y += 22 * u;

        // Bolinhas de progresso do dia
        for (int i = 0; i < Words.PER_DAY; i++) {
            fill.setColor(i < seen ? LIME : LINE);
            float dx = x + i * 30 * u;
            canvas.drawRoundRect(dx, y, dx + 24 * u, y + 6 * u, 3 * u, 3 * u, fill);
        }
        return bitmap;
    }

    private static TextPaint paint(float size, int color, Typeface typeface) {
        TextPaint p = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        p.setTextSize(size);
        p.setColor(color);
        p.setTypeface(typeface);
        return p;
    }

    private static StaticLayout layout(String text, TextPaint paint, int width) {
        return StaticLayout.Builder.obtain(text, 0, text.length(), paint, width)
                .setAlignment(Layout.Alignment.ALIGN_NORMAL)
                .setLineSpacing(0f, 1.15f)
                .setIncludePad(false)
                .build();
    }

    private static float draw(Canvas canvas, StaticLayout layout, float x, float y) {
        canvas.save();
        canvas.translate(x, y);
        layout.draw(canvas);
        canvas.restore();
        return y + layout.getHeight();
    }

    @SuppressWarnings("deprecation")
    private static int[] screenSize(Context context) {
        DisplayMetrics m = new DisplayMetrics();
        DisplayManager dm = context.getSystemService(DisplayManager.class);
        Display display = dm != null ? dm.getDisplay(Display.DEFAULT_DISPLAY) : null;
        if (display != null) {
            display.getRealMetrics(m);
        } else {
            m = context.getResources().getDisplayMetrics();
        }
        int w = Math.min(m.widthPixels, m.heightPixels);
        int h = Math.max(m.widthPixels, m.heightPixels);
        if (w <= 0 || h <= 0) {
            w = 1080;
            h = 2400;
        }
        return new int[]{w, h};
    }
}
