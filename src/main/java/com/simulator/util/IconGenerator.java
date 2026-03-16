package com.simulator.util;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.*;
import java.util.*;
import java.util.List;

/**
 * Generates the application icon (PNG + multi-resolution ICO) using Java AWT.
 *
 * Run via the build script:
 *   java -cp target/classes com.simulator.util.IconGenerator
 *
 * Output:
 *   src/main/resources/icons/app-icon.png       (256 × 256)
 *   src/main/resources/icons/app-icon-1024.png  (1024 × 1024)
 *   src/main/resources/icons/app-icon.ico       (16/32/48/64/128/256 multi-size)
 */
public class IconGenerator {

    public static void main(String[] args) throws IOException {
        // Accept optional project root as first arg; default to current directory
        String base    = (args.length > 0) ? args[0] : ".";
        Path   iconDir = Paths.get(base, "src", "main", "resources", "icons");
        Files.createDirectories(iconDir);

        // ── Save standalone PNGs ──────────────────────────────────────────────
        saveImage(iconDir, "app-icon.png",      256);
        saveImage(iconDir, "app-icon-1024.png", 1024);

        // ── Build multi-resolution ICO ────────────────────────────────────────
        int[] icoSizes = {16, 32, 48, 64, 128, 256};
        List<byte[]> pngChunks = new ArrayList<>();
        List<Integer>  sizeList   = new ArrayList<>();
        for (int sz : icoSizes) {
            BufferedImage img  = renderIcon(sz);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(img, "PNG", baos);
            pngChunks.add(baos.toByteArray());
            sizeList.add(sz);
        }
        Files.write(iconDir.resolve("app-icon.ico"), buildIco(pngChunks, sizeList));

        System.out.println("[IconGenerator] Icons written to: " + iconDir.toAbsolutePath());
        System.out.println("  app-icon.png  (256×256)");
        System.out.println("  app-icon-1024.png (1024×1024)");
        System.out.println("  app-icon.ico  (16/32/48/64/128/256)");
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Rendering
    // ─────────────────────────────────────────────────────────────────────────

    /** Render the icon at an arbitrary resolution and return a BufferedImage. */
    public static BufferedImage renderIcon(int size) {
        BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D    g   = img.createGraphics();

        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,        RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_RENDERING,           RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING,     RenderingHints.VALUE_COLOR_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,       RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL,      RenderingHints.VALUE_STROKE_PURE);

        float cx = size / 2.0f;
        float cy = size / 2.0f;
        float er = size * 0.28f;   // earth radius

        drawBackground(g, size);
        if (size >= 32) drawStars(g, size);
        drawAtmosphereGlow(g, cx, cy, er);
        drawEarth(g, cx, cy, er, size);
        drawOrbitPath(g, cx, cy, er, size);
        drawSatellite(g, cx, cy, er, size);

        g.dispose();
        return img;
    }

    private static void drawBackground(Graphics2D g, int size) {
        // Deep space gradient: near-black blue top → slightly lighter blue bottom
        GradientPaint bg = new GradientPaint(
                0, 0,    new Color(4,  8, 20),
                0, size, new Color(8, 14, 44));
        g.setPaint(bg);
        // Rounded corners only for ≥ 64 px
        int arc = (size >= 64) ? size / 7 : 0;
        g.fillRoundRect(0, 0, size, size, arc, arc);
    }

    private static void drawStars(Graphics2D g, int size) {
        Random rng      = new Random(42L);
        int    count    = Math.min(60, size / 4);
        float  maxStar  = Math.max(0.6f, size * 0.006f);
        for (int i = 0; i < count; i++) {
            float sx   = rng.nextFloat() * size;
            float sy   = rng.nextFloat() * size;
            float br   = 0.35f + rng.nextFloat() * 0.65f;
            float stsz = rng.nextFloat() * maxStar + 0.4f;
            g.setColor(new Color(br, br, br * 0.9f, br * 0.88f));
            g.fill(new Ellipse2D.Float(sx, sy, stsz, stsz));
        }
    }

    private static void drawAtmosphereGlow(Graphics2D g, float cx, float cy, float er) {
        // Concentric semi-transparent blue circles simulate radial atmosphere glow
        float[] radiiF = {2.55f, 2.20f, 1.90f, 1.68f, 1.52f};
        float[] alphaF = {0.045f, 0.07f, 0.10f, 0.13f, 0.09f};
        for (int i = 0; i < radiiF.length; i++) {
            float gr = er * radiiF[i];
            g.setColor(new Color(0.38f, 0.67f, 1.0f, alphaF[i]));
            g.fill(new Ellipse2D.Float(cx - gr, cy - gr, gr * 2, gr * 2));
        }
        // Thin bright limb highlight
        float limb = er * 1.46f;
        g.setColor(new Color(160, 210, 255, 55));
        g.setStroke(new BasicStroke(Math.max(0.5f, er * 0.04f)));
        g.draw(new Ellipse2D.Float(cx - limb, cy - limb, limb * 2, limb * 2));
    }

    private static void drawEarth(Graphics2D g, float cx, float cy, float er, int size) {
        // Simulate radial gradient with many concentric discs (center → edge)
        int steps = Math.max(12, size / 12);
        Color centerColor = new Color(85, 165, 255);
        Color midColor    = new Color(30, 100, 210);
        Color edgeColor   = new Color(4,  22,  55);

        for (int i = steps; i >= 0; i--) {
            float t      = (float) i / steps;           // 1 = outermost, 0 = center
            float radius = er * (float)(i + 1) / (steps + 1);
            Color c = (t > 0.5f)
                    ? lerpColor(midColor,    edgeColor,   (t - 0.5f) * 2.0f)
                    : lerpColor(centerColor, midColor,     t         * 2.0f);
            g.setColor(c);
            g.fill(new Ellipse2D.Float(cx - radius, cy - radius, radius * 2, radius * 2));
        }

        // Simple continent hints (large sizes only)
        if (size >= 64) {
            g.setColor(new Color(25, 95, 50, 70));
            g.fill(new Ellipse2D.Float(cx - er * 0.28f, cy - er * 0.35f, er * 0.42f, er * 0.28f));
            g.fill(new Ellipse2D.Float(cx + er * 0.04f, cy - er * 0.12f, er * 0.32f, er * 0.45f));
            g.fill(new Ellipse2D.Float(cx - er * 0.50f, cy + er * 0.10f, er * 0.26f, er * 0.32f));
        }

        // Atmosphere ring (bright thin edge)
        g.setColor(new Color(183, 224, 255, 65));
        g.setStroke(new BasicStroke(Math.max(0.5f, er * 0.055f)));
        g.draw(new Ellipse2D.Float(cx - er, cy - er, er * 2, er * 2));
    }

    private static void drawOrbitPath(Graphics2D g, float cx, float cy, float er, int size) {
        float orx = er * 1.88f;
        float ory = er * 1.33f;

        float dashLen = Math.max(1.5f, size * 0.035f);
        float gapLen  = Math.max(0.8f, size * 0.015f);
        g.setColor(new Color(130, 195, 255, 85));
        g.setStroke(new BasicStroke(
                Math.max(0.6f, size * 0.007f),
                BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND,
                0, new float[]{dashLen, gapLen}, 0));
        g.draw(new Ellipse2D.Float(cx - orx, cy - ory, orx * 2, ory * 2));
    }

    private static void drawSatellite(Graphics2D g, float cx, float cy, float er, int size) {
        float orx = er * 1.88f;
        float ory = er * 1.33f;

        // Position on the orbit ellipse (~upper-right quadrant)
        double angle = -Math.PI * 0.32;
        float satX = (float)(cx + Math.cos(angle) * orx);
        float satY = (float)(cy + Math.sin(angle) * ory);

        // Glow halo
        float haloR = er * 0.28f;
        g.setColor(new Color(142, 217, 255, 50));
        g.setStroke(new BasicStroke(1.0f));
        g.fill(new Ellipse2D.Float(satX - haloR, satY - haloR, haloR * 2, haloR * 2));

        float bw = size * 0.065f;   // body width
        float bh = size * 0.038f;   // body height
        float pw = bw  * 0.9f;      // panel width
        float ph = bh  * 0.55f;     // panel height

        // Solar panels (blue rectangles left + right)
        g.setColor(new Color(70, 160, 255, 200));
        g.fillRect((int)(satX - bw / 2 - pw), (int)(satY - ph / 2), (int)pw, (int)ph);
        g.fillRect((int)(satX + bw / 2),       (int)(satY - ph / 2), (int)pw, (int)ph);

        // Panel dividers (only at larger sizes)
        if (size >= 48) {
            g.setColor(new Color(0, 80, 160, 140));
            float sw = Math.max(0.5f, size * 0.003f);
            g.setStroke(new BasicStroke(sw));
            float lpMid = satX - bw / 2 - pw / 2;
            float rpMid = satX + bw / 2 + pw / 2;
            g.drawLine((int)lpMid, (int)(satY - ph / 2), (int)lpMid, (int)(satY + ph / 2));
            g.drawLine((int)rpMid, (int)(satY - ph / 2), (int)rpMid, (int)(satY + ph / 2));
        }

        // Satellite body (white rounded rect)
        g.setColor(new Color(242, 251, 255));
        int arcR = Math.max(1, (int)(bh / 3));
        g.fillRoundRect((int)(satX - bw / 2), (int)(satY - bh / 2), (int)bw, (int)bh, arcR, arcR);

        // Body highlight stripe
        if (size >= 48) {
            g.setColor(new Color(100, 175, 255, 120));
            g.fillRoundRect(
                    (int)(satX - bw / 2 + bw * 0.15f),
                    (int)(satY - bh / 2),
                    (int)(bw * 0.25f), (int)bh, arcR, arcR);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  ICO Encoder  (RFC-format, PNG-inside-ICO, Windows Vista+)
    // ─────────────────────────────────────────────────────────────────────────

    private static byte[] buildIco(List<byte[]> pngImages, List<Integer> sizes)
            throws IOException {
        int count      = sizes.size();
        int headerSize = 6 + 16 * count;   // ICONDIR + N × ICONDIRENTRY

        ByteBuffer header = ByteBuffer.allocate(headerSize);
        header.order(ByteOrder.LITTLE_ENDIAN);
        // ICONDIR
        header.putShort((short) 0);       // reserved
        header.putShort((short) 1);       // type = 1 (ICO)
        header.putShort((short) count);   // image count

        int offset = headerSize;
        for (int i = 0; i < count; i++) {
            int sz  = sizes.get(i);
            int len = pngImages.get(i).length;
            header.put((byte)(sz >= 256 ? 0 : sz));  // width  (0 ≡ 256)
            header.put((byte)(sz >= 256 ? 0 : sz));  // height
            header.put((byte) 0);                    // palette size (0 = full colour)
            header.put((byte) 0);                    // reserved
            header.putShort((short) 1);              // colour planes
            header.putShort((short) 32);             // bits per pixel
            header.putInt(len);                      // byte size of image data
            header.putInt(offset);                   // offset into file
            offset += len;
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream(offset);
        out.write(header.array());
        for (byte[] chunk : pngImages) out.write(chunk);
        return out.toByteArray();
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Helpers
    // ─────────────────────────────────────────────────────────────────────────

    private static Color lerpColor(Color a, Color b, float t) {
        t = Math.max(0f, Math.min(1f, t));
        return new Color(
                clamp((int)(a.getRed()   + (b.getRed()   - a.getRed())   * t)),
                clamp((int)(a.getGreen() + (b.getGreen() - a.getGreen()) * t)),
                clamp((int)(a.getBlue()  + (b.getBlue()  - a.getBlue())  * t)));
    }

    private static int clamp(int v) { return Math.max(0, Math.min(255, v)); }

    private static void saveImage(Path dir, String name, int size) throws IOException {
        BufferedImage img = renderIcon(size);
        ImageIO.write(img, "PNG", dir.resolve(name).toFile());
        System.out.println("[IconGenerator] Saved: " + name + " (" + size + "×" + size + ")");
    }
}


