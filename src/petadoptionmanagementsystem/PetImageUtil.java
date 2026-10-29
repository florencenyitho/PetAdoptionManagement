package petadoptionmanagementsystem;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

/**
 * Utility for loading and scaling pet photos.
 * Handles missing/corrupt files gracefully with a placeholder icon.
 */
public class PetImageUtil {

    /** Thumbnail size used in the table column. */
    public static final int THUMB_W = 60;
    public static final int THUMB_H = 54;

    /** Preview size shown in the form panel. */
    public static final int PREVIEW_W = 160;
    public static final int PREVIEW_H = 140;

    /** Full-size view in the popup dialog. */
    public static final int FULL_W = 480;
    public static final int FULL_H = 400;

    private PetImageUtil() {}

    /**
     * Loads the image at the given path and scales it to fit within
     * the target width/height while keeping aspect ratio.
     * Returns a placeholder icon if the path is null, empty, or the file
     * can't be read.
     */
    public static ImageIcon load(String path, int targetW, int targetH) {
        if (path != null && !path.isBlank()) {
            try {
                File f = new File(path);
                if (f.exists() && f.isFile()) {
                    BufferedImage src = ImageIO.read(f);
                    if (src != null) {
                        return new ImageIcon(scale(src, targetW, targetH));
                    }
                }
            } catch (Exception ignored) {}
        }
        return placeholder(targetW, targetH);
    }

    /** Thumbnail for the table (60 × 54). */
    public static ImageIcon thumbnail(String path) {
        return load(path, THUMB_W, THUMB_H);
    }

    /** Form preview (160 × 140). */
    public static ImageIcon preview(String path) {
        return load(path, PREVIEW_W, PREVIEW_H);
    }

    /** Full-size popup (480 × 400). */
    public static ImageIcon full(String path) {
        return load(path, FULL_W, FULL_H);
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    /** Scales src to fit inside (maxW × maxH) preserving aspect ratio. */
    private static BufferedImage scale(BufferedImage src, int maxW, int maxH) {
        int srcW = src.getWidth();
        int srcH = src.getHeight();
        double scale = Math.min((double) maxW / srcW, (double) maxH / srcH);
        int w = (int) (srcW * scale);
        int h = (int) (srcH * scale);
        BufferedImage out = new BufferedImage(maxW, maxH, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = out.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                           RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                           RenderingHints.VALUE_ANTIALIAS_ON);
        // Centre the image within the target box
        int x = (maxW - w) / 2;
        int y = (maxH - h) / 2;
        g.setColor(new Color(245, 248, 252));
        g.fillRect(0, 0, maxW, maxH);
        g.drawImage(src, x, y, w, h, null);
        g.dispose();
        return out;
    }

    /** Gray placeholder with a camera icon drawn in the centre. */
    private static ImageIcon placeholder(int w, int h) {
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Background
        g.setColor(new Color(228, 233, 242));
        g.fillRoundRect(0, 0, w, h, 8, 8);

        // Camera body outline
        float s = Math.min(w, h) / 32f;
        g.setColor(new Color(160, 170, 190));
        g.setStroke(new BasicStroke(1.5f * s));
        int bx = (int)(w / 2 - 8 * s), by = (int)(h / 2 - 5 * s);
        int bw = (int)(16 * s),        bh = (int)(11 * s);
        g.drawRoundRect(bx, by, bw, bh, (int)(3 * s), (int)(3 * s));
        // Lens circle
        g.drawOval((int)(w / 2 - 4 * s), (int)(h / 2 - 4 * s),
                   (int)(8 * s), (int)(8 * s));
        // Viewfinder notch
        g.fillRect((int)(w / 2 + 3 * s), (int)(h / 2 - 7 * s),
                   (int)(3 * s), (int)(2 * s));

        g.dispose();
        return new ImageIcon(img);
    }
}
