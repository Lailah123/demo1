package com.zjx;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import javax.imageio.ImageIO;

/**
 * 图片转 ASCII 字符画工具类。
 *
 * <p>基于 JDK 内置的 {@link ImageIO} 与 {@link BufferedImage} 实现，零外部依赖。
 * 支持纯文本、彩色 HTML、终端 ANSI 三种输出形态。</p>
 *
 * <p>算法流程：读取图片 → 按目标宽度降采样（校正字符 2:1 高宽比）→
 * 计算每个采样块的平均亮度 → 将亮度映射到字符梯度。</p>
 */
public final class AsciiArt {

    /** 从深到浅的字符梯度：亮度越低，映射到越靠左的密集字符。 */
    private static final String RAMP = "@%#*+=-:. ";

    /** 输出宽度上限，防止超大图导致内存与性能问题。 */
    private static final int MAX_WIDTH = 200;

    /** 等宽 ASCII 字符的高宽比约为 2:1（字符高度约为宽度的两倍）。 */
    private static final double CHAR_ASPECT_RATIO = 2.0;

    private AsciiArt() {
        // 工具类，禁止实例化
    }

    /**
     * 将图片转换为黑白 ASCII 字符画。
     *
     * @param image 源图片，不能为 {@code null}
     * @param width 输出字符宽度，取值范围 [1, {@value #MAX_WIDTH}]
     * @return 以换行分隔的 ASCII 字符画
     * @throws IllegalArgumentException 当图片为 {@code null} 或宽度非法时
     */
    public static String toAscii(BufferedImage image, int width) {
        return render(image, width, Renderer.BW);
    }

    /**
     * 将图片文件转换为黑白 ASCII 字符画。
     *
     * @param file  图片文件，不能为 {@code null}
     * @param width 输出字符宽度
     * @return 以换行分隔的 ASCII 字符画
     * @throws IOException              当文件读取失败时
     * @throws IllegalArgumentException 当文件为 {@code null}、格式无法识别或宽度非法时
     */
    public static String toAscii(File file, int width) throws IOException {
        return toAscii(readImage(file), width);
    }

    /**
     * 将网络图片转换为黑白 ASCII 字符画。
     *
     * @param url   图片地址，不能为 {@code null}
     * @param width 输出字符宽度
     * @return 以换行分隔的 ASCII 字符画
     * @throws IOException              当图片读取失败时
     * @throws IllegalArgumentException 当地址为 {@code null}、格式无法识别或宽度非法时
     */
    public static String toAscii(URL url, int width) throws IOException {
        return toAscii(readImage(url), width);
    }

    /**
     * 将输入流中的图片转换为黑白 ASCII 字符画。
     *
     * @param in    图片输入流，不能为 {@code null}
     * @param width 输出字符宽度
     * @return 以换行分隔的 ASCII 字符画
     * @throws IOException              当图片读取失败时
     * @throws IllegalArgumentException 当输入流为 {@code null}、格式无法识别或宽度非法时
     */
    public static String toAscii(InputStream in, int width) throws IOException {
        return toAscii(readImage(in), width);
    }

    /**
     * 将图片转换为彩色 HTML 字符画（每个字符用 {@code <span>} 着色，整体以 {@code <pre>} 包裹）。
     *
     * @param image 源图片，不能为 {@code null}
     * @param width 输出字符宽度
     * @return 可直接嵌入网页的 HTML 片段
     * @throws IllegalArgumentException 当图片为 {@code null} 或宽度非法时
     */
    public static String toHtml(BufferedImage image, int width) {
        return "<pre>" + render(image, width, Renderer.HTML) + "</pre>";
    }

    /**
     * 将图片文件转换为彩色 HTML 字符画。
     *
     * @param file  图片文件，不能为 {@code null}
     * @param width 输出字符宽度
     * @return 可直接嵌入网页的 HTML 片段
     * @throws IOException              当文件读取失败时
     * @throws IllegalArgumentException 当文件为 {@code null}、格式无法识别或宽度非法时
     */
    public static String toHtml(File file, int width) throws IOException {
        return toHtml(readImage(file), width);
    }

    /**
     * 将图片转换为终端彩色 ANSI 字符画（使用 24 位真彩转义序列）。
     *
     * @param image 源图片，不能为 {@code null}
     * @param width 输出字符宽度
     * @return 带 ANSI 颜色转义序列的字符串，末尾附带复位序列
     * @throws IllegalArgumentException 当图片为 {@code null} 或宽度非法时
     */
    public static String toAnsi(BufferedImage image, int width) {
        return render(image, width, Renderer.ANSI) + "\u001b[0m";
    }

    /**
     * 将图片文件转换为终端彩色 ANSI 字符画。
     *
     * @param file  图片文件，不能为 {@code null}
     * @param width 输出字符宽度
     * @return 带 ANSI 颜色转义序列的字符串，末尾附带复位序列
     * @throws IOException              当文件读取失败时
     * @throws IllegalArgumentException 当文件为 {@code null}、格式无法识别或宽度非法时
     */
    public static String toAnsi(File file, int width) throws IOException {
        return toAnsi(readImage(file), width);
    }

    private static BufferedImage readImage(File file) throws IOException {
        if (file == null) {
            throw new IllegalArgumentException("图片文件不能为 null");
        }
        BufferedImage image = ImageIO.read(file);
        if (image == null) {
            throw new IllegalArgumentException("无法识别的图片格式: " + file.getName());
        }
        return image;
    }

    private static BufferedImage readImage(URL url) throws IOException {
        if (url == null) {
            throw new IllegalArgumentException("图片地址不能为 null");
        }
        BufferedImage image = ImageIO.read(url);
        if (image == null) {
            throw new IllegalArgumentException("无法识别的图片格式: " + url);
        }
        return image;
    }

    private static BufferedImage readImage(InputStream in) throws IOException {
        if (in == null) {
            throw new IllegalArgumentException("输入流不能为 null");
        }
        BufferedImage image = ImageIO.read(in);
        if (image == null) {
            throw new IllegalArgumentException("无法识别的图片格式");
        }
        return image;
    }

    private static String render(BufferedImage image, int width, Renderer renderer) {
        if (image == null) {
            throw new IllegalArgumentException("图片不能为 null");
        }
        validateWidth(width);

        int iw = image.getWidth();
        int ih = image.getHeight();
        int cols = width;
        int rows = Math.max(1, (int) Math.round(ih * width / (CHAR_ASPECT_RATIO * iw)));

        StringBuilder sb = new StringBuilder((cols + 1) * rows);
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                int x0 = c * iw / cols;
                int x1 = Math.max(x0 + 1, (c + 1) * iw / cols);
                int y0 = r * ih / rows;
                int y1 = Math.max(y0 + 1, (r + 1) * ih / rows);
                int[] rgb = average(image, x0, y0, x1, y1);
                sb.append(renderer.pixel(rgb, rampChar(rgb)));
            }
            sb.append('\n');
        }
        return sb.toString();
    }

    private static int[] average(BufferedImage image, int x0, int y0, int x1, int y1) {
        long rSum = 0L;
        long gSum = 0L;
        long bSum = 0L;
        int count = 0;
        for (int y = y0; y < y1; y++) {
            for (int x = x0; x < x1; x++) {
                int argb = image.getRGB(x, y);
                int alpha = (argb >> 24) & 0xFF;
                int r = (argb >> 16) & 0xFF;
                int g = (argb >> 8) & 0xFF;
                int b = argb & 0xFF;
                // 将透明像素合成到白色背景上，避免透明区域渲染异常
                r = (r * alpha + 255 * (255 - alpha)) / 255;
                g = (g * alpha + 255 * (255 - alpha)) / 255;
                b = (b * alpha + 255 * (255 - alpha)) / 255;
                rSum += r;
                gSum += g;
                bSum += b;
                count++;
            }
        }
        return new int[] {
            (int) (rSum / count),
            (int) (gSum / count),
            (int) (bSum / count)
        };
    }

    private static char rampChar(int[] rgb) {
        // 用整数计算亮度，避免浮点截断误差（三个系数之和恰为 10000）
        int luma = (rgb[0] * 2126 + rgb[1] * 7152 + rgb[2] * 722) / 10000;
        int index = luma * (RAMP.length() - 1) / 255;
        return RAMP.charAt(index);
    }

    private static void validateWidth(int width) {
        if (width <= 0) {
            throw new IllegalArgumentException("输出宽度必须为正整数，收到: " + width);
        }
        if (width > MAX_WIDTH) {
            throw new IllegalArgumentException("输出宽度过大（上限 " + MAX_WIDTH + "），收到: " + width);
        }
    }

    private static String escapeHtml(char ch) {
        switch (ch) {
            case '<':
                return "&lt;";
            case '>':
                return "&gt;";
            case '&':
                return "&amp;";
            default:
                return String.valueOf(ch);
        }
    }

    /** 输出渲染器：把「平均颜色 + 字符」渲染为对应形态的文本片段。 */
    private enum Renderer {
        /** 纯文本，仅输出字符本身。 */
        BW {
            @Override
            String pixel(int[] rgb, char ch) {
                return String.valueOf(ch);
            }
        },
        /** HTML：每个字符用带颜色的 span 包裹。 */
        HTML {
            @Override
            String pixel(int[] rgb, char ch) {
                String color = String.format("#%02x%02x%02x", rgb[0], rgb[1], rgb[2]);
                return "<span style=\"color:" + color + "\">" + escapeHtml(ch) + "</span>";
            }
        },
        /** ANSI：使用 24 位真彩前景色转义序列。 */
        ANSI {
            @Override
            String pixel(int[] rgb, char ch) {
                return "\u001b[38;2;" + rgb[0] + ";" + rgb[1] + ";" + rgb[2] + "m" + ch;
            }
        };

        abstract String pixel(int[] rgb, char ch);
    }
}
