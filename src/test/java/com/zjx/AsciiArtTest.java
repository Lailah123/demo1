package com.zjx;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import javax.imageio.ImageIO;

import org.junit.jupiter.api.Test;

/**
 * {@link AsciiArt} 的单元测试。
 *
 * <p>不依赖真实图片文件，使用代码生成的合成 {@link BufferedImage}，
 * 保证测试封闭、可复现。</p>
 */
class AsciiArtTest {

    /** 生成纯色 RGB 图片。 */
    private static BufferedImage solid(int width, int height, int rgb) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                image.setRGB(x, y, rgb);
            }
        }
        return image;
    }

    /** 去掉末尾换行后按行切分。 */
    private static String[] lines(String art) {
        if (art.endsWith("\n")) {
            art = art.substring(0, art.length() - 1);
        }
        return art.split("\n");
    }

    @Test
    void solidBlackMapsToDarkestChar() {
        String[] rows = lines(AsciiArt.toAscii(solid(20, 20, 0x000000), 10));
        assertEquals(5, rows.length, "20x20 图在宽度 10 下应为 5 行（2:1 高宽比）");
        for (String row : rows) {
            for (char c : row.toCharArray()) {
                assertEquals('@', c, "纯黑像素应映射到最深字符");
            }
        }
    }

    @Test
    void solidWhiteMapsToLightestChar() {
        String[] rows = lines(AsciiArt.toAscii(solid(20, 20, 0xFFFFFF), 10));
        for (String row : rows) {
            for (char c : row.toCharArray()) {
                assertEquals(' ', c, "纯白像素应映射到最浅字符（空格）");
            }
        }
    }

    @Test
    void outputWidthMatchesTarget() {
        String[] rows = lines(AsciiArt.toAscii(solid(30, 30, 0x808080), 12));
        for (String row : rows) {
            assertEquals(12, row.length(), "每行宽度应等于目标宽度");
        }
    }

    @Test
    void squareImageKeepsAspectRatio() {
        // 100x100 方形图，宽度 20 → 行数 = round(100 * 20 / (2 * 100)) = 10
        String[] rows = lines(AsciiArt.toAscii(solid(100, 100, 0x000000), 20));
        assertEquals(10, rows.length, "方形图应保持 2:1 字符高宽比");
    }

    @Test
    void gradientDarkToLight() {
        BufferedImage image = new BufferedImage(40, 20, BufferedImage.TYPE_INT_RGB);
        for (int y = 0; y < 20; y++) {
            for (int x = 0; x < 40; x++) {
                image.setRGB(x, y, x < 20 ? 0x000000 : 0xFFFFFF);
            }
        }
        String[] rows = lines(AsciiArt.toAscii(image, 8));
        for (String row : rows) {
            assertEquals('@', row.charAt(0), "最左列应为最暗字符");
            assertEquals(' ', row.charAt(row.length() - 1), "最右列应为最亮字符");
        }
    }

    @Test
    void rejectsNullImage() {
        assertThrows(IllegalArgumentException.class,
                () -> AsciiArt.toAscii((BufferedImage) null, 10));
        assertThrows(IllegalArgumentException.class,
                () -> AsciiArt.toHtml((BufferedImage) null, 10));
        assertThrows(IllegalArgumentException.class,
                () -> AsciiArt.toAnsi((BufferedImage) null, 10));
    }

    @Test
    void rejectsNonPositiveWidth() {
        assertThrows(IllegalArgumentException.class, () -> AsciiArt.toAscii(solid(10, 10, 0), 0));
        assertThrows(IllegalArgumentException.class, () -> AsciiArt.toAscii(solid(10, 10, 0), -1));
    }

    @Test
    void rejectsTooLargeWidth() {
        assertThrows(IllegalArgumentException.class, () -> AsciiArt.toAscii(solid(10, 10, 0), 10000));
    }

    @Test
    void htmlWrapsInPreWithColoredSpans() {
        String html = AsciiArt.toHtml(solid(20, 20, 0x000000), 10);
        assertTrue(html.startsWith("<pre>"), "HTML 输出应以 <pre> 开头");
        assertTrue(html.endsWith("</pre>"), "HTML 输出应以 </pre> 结尾");
        assertTrue(html.contains("<span style=\"color:#000000\">"), "黑色像素应对应 #000000 着色");
    }

    @Test
    void transparentPixelsCompositeToWhite() {
        // TYPE_INT_ARGB 初始为全透明（alpha=0），应合成到白色背景 → 最浅字符
        BufferedImage transparent = new BufferedImage(20, 20, BufferedImage.TYPE_INT_ARGB);
        String[] rows = lines(AsciiArt.toAscii(transparent, 10));
        for (String row : rows) {
            for (char c : row.toCharArray()) {
                assertEquals(' ', c, "透明像素应合成到白色背景，映射到最浅字符");
            }
        }
    }

    @Test
    void rejectsNullFileUrlStream() {
        assertThrows(IllegalArgumentException.class, () -> AsciiArt.toAscii((File) null, 10));
        assertThrows(IllegalArgumentException.class, () -> AsciiArt.toAscii((URL) null, 10));
        assertThrows(IllegalArgumentException.class, () -> AsciiArt.toAscii((InputStream) null, 10));
        assertThrows(IllegalArgumentException.class, () -> AsciiArt.toHtml((File) null, 10));
        assertThrows(IllegalArgumentException.class, () -> AsciiArt.toHtml((URL) null, 10));
        assertThrows(IllegalArgumentException.class, () -> AsciiArt.toHtml((InputStream) null, 10));
        assertThrows(IllegalArgumentException.class, () -> AsciiArt.toAnsi((File) null, 10));
        assertThrows(IllegalArgumentException.class, () -> AsciiArt.toAnsi((URL) null, 10));
        assertThrows(IllegalArgumentException.class, () -> AsciiArt.toAnsi((InputStream) null, 10));
    }

    @Test
    void rejectsUnrecognizedImageStream() {
        byte[] notAnImage = "这不是图片内容".getBytes(StandardCharsets.UTF_8);
        assertThrows(IllegalArgumentException.class,
                () -> AsciiArt.toAscii(new ByteArrayInputStream(notAnImage), 10));
    }

    @Test
    void oneByOneImageProducesSingleChar() {
        assertEquals("@\n", AsciiArt.toAscii(solid(1, 1, 0x000000), 1),
                "1x1 图在宽度 1 下应输出单个最深字符");
    }

    @Test
    void widthOneProducesSingleColumn() {
        String[] rows = lines(AsciiArt.toAscii(solid(10, 10, 0x000000), 1));
        assertEquals(1, rows.length, "10x10 图在宽度 1 下应为 1 行");
        assertEquals("@", rows[0], "宽度 1 应输出单列最深字符");
    }

    @Test
    void streamOverloadsMatchBufferedImageOutput() throws Exception {
        BufferedImage image = solid(20, 20, 0x000000);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ImageIO.write(image, "png", out);

        assertEquals(AsciiArt.toAscii(image, 10),
                AsciiArt.toAscii(new ByteArrayInputStream(out.toByteArray()), 10));
        assertEquals(AsciiArt.toHtml(image, 10),
                AsciiArt.toHtml(new ByteArrayInputStream(out.toByteArray()), 10));
        assertEquals(AsciiArt.toAnsi(image, 10),
                AsciiArt.toAnsi(new ByteArrayInputStream(out.toByteArray()), 10));
    }

    @Test
    void ansiContainsColorEscapeAndReset() {
        String ansi = AsciiArt.toAnsi(solid(20, 20, 0x000000), 10);
        assertTrue(ansi.contains("\u001b[38;2;0;0;0m"), "黑色像素应包含 ANSI 真彩转义");
        assertTrue(ansi.endsWith("\u001b[0m"), "ANSI 输出末尾应包含复位序列");
    }
}
