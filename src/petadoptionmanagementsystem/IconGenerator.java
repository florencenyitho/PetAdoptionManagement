package petadoptionmanagementsystem;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;

/**
 * Generates all application icons programmatically using Java2D.
 * No external image files needed — crisp at any size.
 */
public class IconGenerator {

    // ── Tab / header icons ────────────────────────────────────────────────────

    public static ImageIcon pets(int size) {
        return new ImageIcon(drawPaw(size, new Color(52, 152, 219)));
    }

    public static ImageIcon adopters(int size) {
        return new ImageIcon(drawPerson(size, new Color(155, 89, 182)));
    }

    public static ImageIcon adoptions(int size) {
        return new ImageIcon(drawHeart(size, new Color(231, 76, 60)));
    }

    public static ImageIcon reports(int size) {
        return new ImageIcon(drawChart(size, new Color(39, 174, 96)));
    }

    public static ImageIcon header(int size) {
        return new ImageIcon(drawPaw(size, Color.WHITE));
    }

    // ── Button icons ──────────────────────────────────────────────────────────

    /** Plus sign — Register / Add */
    public static ImageIcon add(int size) {
        return new ImageIcon(drawPlus(size, Color.WHITE));
    }

    /** Pencil — Update / Edit */
    public static ImageIcon edit(int size) {
        return new ImageIcon(drawPencil(size, Color.WHITE));
    }

    /** Trash bin — Delete */
    public static ImageIcon delete(int size) {
        return new ImageIcon(drawTrash(size, Color.WHITE));
    }

    /** X circle — Clear */
    public static ImageIcon clear(int size) {
        return new ImageIcon(drawXCircle(size, Color.WHITE));
    }

    /** Circular arrows — Refresh */
    public static ImageIcon refresh(int size) {
        return new ImageIcon(drawRefresh(size, Color.WHITE));
    }

    /** Handshake / checkmark — Record Adoption */
    public static ImageIcon record(int size) {
        return new ImageIcon(drawCheckShield(size, Color.WHITE));
    }

    /** Magnifier — Search */
    public static ImageIcon search(int size) {
        return new ImageIcon(drawMagnifier(size, Color.WHITE));
    }

    // ── Drawing: tab icons ────────────────────────────────────────────────────

    private static BufferedImage drawPaw(int size, Color color) {
        BufferedImage img = transparent(size);
        Graphics2D g = setup(img);
        g.setColor(color);
        float s = size / 32f;
        g.fill(new Ellipse2D.Float(7*s, 14*s, 18*s, 15*s));   // main pad
        g.fill(new Ellipse2D.Float(3*s,  6*s,  8*s,  9*s));   // toe 1
        g.fill(new Ellipse2D.Float(12*s, 3*s,  8*s,  9*s));   // toe 2
        g.fill(new Ellipse2D.Float(21*s, 6*s,  8*s,  9*s));   // toe 3
        g.dispose();
        return img;
    }

    private static BufferedImage drawPerson(int size, Color color) {
        BufferedImage img = transparent(size);
        Graphics2D g = setup(img);
        g.setColor(color);
        float s = size / 32f;
        g.fill(new Ellipse2D.Float(10*s, 2*s, 12*s, 12*s));   // head
        GeneralPath body = new GeneralPath();
        body.moveTo(4*s, 30*s);
        body.curveTo(4*s, 18*s, 28*s, 18*s, 28*s, 30*s);
        body.closePath();
        g.fill(body);
        g.dispose();
        return img;
    }

    private static BufferedImage drawHeart(int size, Color color) {
        BufferedImage img = transparent(size);
        Graphics2D g = setup(img);
        g.setColor(color);
        float s = size / 32f;
        GeneralPath heart = new GeneralPath();
        heart.moveTo(16*s, 28*s);
        heart.curveTo(16*s, 28*s,  2*s, 18*s,  2*s, 10*s);
        heart.curveTo( 2*s,  4*s,  8*s,  2*s, 16*s,  8*s);
        heart.curveTo(24*s,  2*s, 30*s,  4*s, 30*s, 10*s);
        heart.curveTo(30*s, 18*s, 16*s, 28*s, 16*s, 28*s);
        heart.closePath();
        g.fill(heart);
        g.dispose();
        return img;
    }

    private static BufferedImage drawChart(int size, Color color) {
        BufferedImage img = transparent(size);
        Graphics2D g = setup(img);
        float s = size / 32f;
        int[][] bars = {{4, 14, 6, 16}, {13, 6, 6, 24}, {22, 20, 6, 10}};
        Color[] cols = {color.brighter(), color, color.brighter()};
        for (int i = 0; i < bars.length; i++) {
            g.setColor(cols[i]);
            g.fill(new RoundRectangle2D.Float(
                bars[i][0]*s, bars[i][1]*s, bars[i][2]*s, bars[i][3]*s, 3*s, 3*s));
        }
        g.setColor(color.darker());
        g.setStroke(new BasicStroke(1.5f * s));
        g.draw(new Line2D.Float(2*s, 30*s, 30*s, 30*s));
        g.dispose();
        return img;
    }

    // ── Drawing: button icons ─────────────────────────────────────────────────

    private static BufferedImage drawPlus(int size, Color color) {
        BufferedImage img = transparent(size);
        Graphics2D g = setup(img);
        g.setColor(color);
        float s    = size / 16f;
        float w    = 2.5f * s;
        float half = size / 2f;
        g.fill(new RoundRectangle2D.Float(half - w/2, 1*s, w, size - 2*s, w, w));  // vertical
        g.fill(new RoundRectangle2D.Float(1*s, half - w/2, size - 2*s, w, w, w));  // horizontal
        g.dispose();
        return img;
    }

    private static BufferedImage drawPencil(int size, Color color) {
        BufferedImage img = transparent(size);
        Graphics2D g = setup(img);
        g.setColor(color);
        float s = size / 32f;
        // Pencil body
        GeneralPath pencil = new GeneralPath();
        pencil.moveTo(22*s, 3*s);
        pencil.lineTo(29*s, 10*s);
        pencil.lineTo(12*s, 27*s);
        pencil.lineTo(5*s,  27*s);
        pencil.lineTo(5*s,  20*s);
        pencil.closePath();
        g.fill(pencil);
        // Pencil tip
        g.setColor(new Color(255, 220, 150));
        GeneralPath tip = new GeneralPath();
        tip.moveTo(5*s, 20*s);
        tip.lineTo(5*s, 27*s);
        tip.lineTo(12*s, 27*s);
        tip.closePath();
        g.fill(tip);
        g.dispose();
        return img;
    }

    private static BufferedImage drawTrash(int size, Color color) {
        BufferedImage img = transparent(size);
        Graphics2D g = setup(img);
        g.setColor(color);
        float s = size / 32f;
        // Lid
        g.fill(new RoundRectangle2D.Float(4*s, 4*s, 24*s, 4*s, 3*s, 3*s));
        // Handle on lid
        g.fill(new RoundRectangle2D.Float(12*s, 1*s, 8*s, 4*s, 3*s, 3*s));
        // Body
        g.fill(new RoundRectangle2D.Float(6*s, 10*s, 20*s, 19*s, 3*s, 3*s));
        // Lines inside (cutouts using background color)
        g.setColor(new Color(0, 0, 0, 60));
        float lw = 1.8f * s;
        g.fill(new RoundRectangle2D.Float(12*s, 13*s, lw, 13*s, lw, lw));
        g.fill(new RoundRectangle2D.Float(16*s - lw/2, 13*s, lw, 13*s, lw, lw));
        g.fill(new RoundRectangle2D.Float(20*s - lw,   13*s, lw, 13*s, lw, lw));
        g.dispose();
        return img;
    }

    private static BufferedImage drawXCircle(int size, Color color) {
        BufferedImage img = transparent(size);
        Graphics2D g = setup(img);
        float s = size / 32f;
        // Outer circle
        g.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 60));
        g.fill(new Ellipse2D.Float(1*s, 1*s, 30*s, 30*s));
        // X strokes
        g.setColor(color);
        g.setStroke(new BasicStroke(3f * s, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.draw(new Line2D.Float(10*s, 10*s, 22*s, 22*s));
        g.draw(new Line2D.Float(22*s, 10*s, 10*s, 22*s));
        g.dispose();
        return img;
    }

    private static BufferedImage drawRefresh(int size, Color color) {
        BufferedImage img = transparent(size);
        Graphics2D g = setup(img);
        g.setColor(color);
        float s   = size / 32f;
        float cx  = size / 2f;
        float r   = 11 * s;
        float sw  = 3f * s;
        // Arc (270 degrees, leaving a gap at top-right)
        g.setStroke(new BasicStroke(sw, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.draw(new Arc2D.Float(cx - r, cx - r, r*2, r*2, 60, 270, Arc2D.OPEN));
        // Arrow head at end of arc
        GeneralPath arrow = new GeneralPath();
        arrow.moveTo(cx + r - 1*s, cx - 6*s);
        arrow.lineTo(cx + r + 5*s, cx - 1*s);
        arrow.lineTo(cx + r - 3*s, cx + 4*s);
        g.setStroke(new BasicStroke(sw, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.draw(arrow);
        g.dispose();
        return img;
    }

    private static BufferedImage drawCheckShield(int size, Color color) {
        BufferedImage img = transparent(size);
        Graphics2D g = setup(img);
        g.setColor(color);
        float s = size / 32f;
        // Shield shape
        GeneralPath shield = new GeneralPath();
        shield.moveTo(16*s, 2*s);
        shield.lineTo(28*s, 7*s);
        shield.lineTo(28*s, 16*s);
        shield.curveTo(28*s, 24*s, 16*s, 30*s, 16*s, 30*s);
        shield.curveTo(16*s, 30*s,  4*s, 24*s,  4*s, 16*s);
        shield.lineTo(4*s,  7*s);
        shield.closePath();
        g.fill(shield);
        // Checkmark
        g.setColor(new Color(0, 0, 0, 80));
        g.setStroke(new BasicStroke(3f * s, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        GeneralPath check = new GeneralPath();
        check.moveTo(10*s, 16*s);
        check.lineTo(14*s, 21*s);
        check.lineTo(22*s, 11*s);
        g.draw(check);
        g.dispose();
        return img;
    }

    private static BufferedImage drawMagnifier(int size, Color color) {
        BufferedImage img = transparent(size);
        Graphics2D g = setup(img);
        g.setColor(color);
        float s  = size / 32f;
        float sw = 3f * s;
        g.setStroke(new BasicStroke(sw, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        // Circle
        g.draw(new Ellipse2D.Float(4*s, 4*s, 18*s, 18*s));
        // Handle
        g.draw(new Line2D.Float(19*s, 19*s, 28*s, 28*s));
        g.dispose();
        return img;
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private static BufferedImage transparent(int size) {
        return new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
    }

    private static Graphics2D setup(BufferedImage img) {
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,   RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_RENDERING,      RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        return g;
    }
}
