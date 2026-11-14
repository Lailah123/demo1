package com.zjx;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.List;
import java.util.Random;

/**
 * ASCII 迷宫生成与求解工具类。
 *
 * <p>使用深度优先回溯（递归回溯）算法生成「完美迷宫」——任意两个通路格子之间
 * 有且仅有一条路径，无环、无孤立区域。迷宫以多行文本表示：{@code #} 为墙、
 * 空格为通路，入口固定在左上角内侧标记 {@code S}，出口固定在右下角内侧标记 {@code E}。</p>
 *
 * <p>求解使用广度优先搜索（BFS），保证返回 {@code S → E} 的最短路径，并用
 * {@code *} 标注在原迷宫上。</p>
 *
 * <p>风格与 {@link LetterArt}、{@link TextTable} 一致：纯 JDK 实现、零外部依赖、
 * 工具类禁止实例化。</p>
 *
 * <p>使用示例：</p>
 * <pre>
 * String maze = MazeArt.generate(21, 11);
 * System.out.println(maze);
 * System.out.println(MazeArt.solve(maze));
 * </pre>
 */
public final class MazeArt {

    /** 迷宫字符：墙。 */
    public static final char WALL = '#';

    /** 迷宫字符：通路。 */
    public static final char PATH = ' ';

    /** 迷宫字符：入口标记。 */
    public static final char START = 'S';

    /** 迷宫字符：出口标记。 */
    public static final char END = 'E';

    /** 迷宫字符：求解后的路径标记。 */
    public static final char ROUTE = '*';

    /** 尺寸下限（宽高相同），保证迷宫至少有 2×2 个通路格子。 */
    public static final int MIN_SIZE = 5;

    /** 尺寸上限（宽高相同），防止输出体积失控。 */
    public static final int MAX_SIZE = 201;

    private MazeArt() {
        // 工具类，禁止实例化
    }

    /**
     * 生成一个随机完美迷宫，使用随机种子。
     *
     * @param width  迷宫宽度（字符数），须为 [5, 201] 内的奇数
     * @param height 迷宫高度（行数），须为 [5, 201] 内的奇数
     * @return 以换行结尾的多行迷宫文本
     * @throws IllegalArgumentException 当宽高非法时
     */
    public static String generate(int width, int height) {
        return generate(width, height, new Random().nextLong());
    }

    /**
     * 生成一个可复现的完美迷宫（固定种子）。
     *
     * @param width  迷宫宽度（字符数），须为 [5, 201] 内的奇数
     * @param height 迷宫高度（行数），须为 [5, 201] 内的奇数
     * @param seed   随机种子，相同种子生成完全相同的迷宫
     * @return 以换行结尾的多行迷宫文本
     * @throws IllegalArgumentException 当宽高非法时
     */
    public static String generate(int width, int height, long seed) {
        checkSize("宽度", width);
        checkSize("高度", height);

        Random random = new Random(seed);
        char[][] grid = new char[height][width];
        for (char[] row : grid) {
            Arrays.fill(row, WALL);
        }

        // 深度优先回溯：从 (1, 1) 出发，每次向两格外的未访问格子打穿中间的墙
        Deque<int[]> stack = new ArrayDeque<int[]>();
        grid[1][1] = PATH;
        stack.push(new int[] {1, 1});
        while (!stack.isEmpty()) {
            int[] cell = stack.peek();
            int row = cell[0];
            int col = cell[1];
            List<int[]> neighbors = new ArrayList<int[]>();
            collectUnvisited(neighbors, grid, row - 2, col, row - 1, col);
            collectUnvisited(neighbors, grid, row + 2, col, row + 1, col);
            collectUnvisited(neighbors, grid, row, col - 2, row, col - 1);
            collectUnvisited(neighbors, grid, row, col + 2, row, col + 1);
            if (neighbors.isEmpty()) {
                stack.pop();
            } else {
                int[] next = neighbors.get(random.nextInt(neighbors.size()));
                grid[next[2]][next[3]] = PATH; // 打通中间的墙
                grid[next[0]][next[1]] = PATH; // 打通目标格
                stack.push(next);
            }
        }

        grid[1][1] = START;
        grid[height - 2][width - 2] = END;
        return toText(grid);
    }

    /**
     * 求解迷宫，返回用 {@code *} 标注最短路径的迷宫文本。
     *
     * <p>BFS 逐层扩展，首次到达出口即最短路径；路径标记不含起终点本身，
     * {@code S} 与 {@code E} 在结果中保持原样，便于观察路径端点。</p>
     *
     * @param maze 迷宫文本（来自 {@link #generate(int, int)}）
     * @return 用 {@code *} 标注了最短路径的迷宫文本
     * @throws IllegalArgumentException 当迷宫格式非法或无解时
     */
    public static String solve(String maze) {
        char[][] grid = parse(maze);
        int height = grid.length;
        int width = grid[0].length;

        int[] start = findMark(grid, START);
        int[] end = findMark(grid, END);
        if (start == null || end == null) {
            throw new IllegalArgumentException("迷宫必须同时包含起点 S 与终点 E");
        }

        int[][] prev = new int[height][width];
        for (int[] row : prev) {
            Arrays.fill(row, -1);
        }
        boolean[][] visited = new boolean[height][width];
        Deque<int[]> queue = new ArrayDeque<int[]>();
        visited[start[0]][start[1]] = true;
        queue.add(start);
        while (!queue.isEmpty()) {
            int[] cell = queue.poll();
            if (cell[0] == end[0] && cell[1] == end[1]) {
                break;
            }
            int[][] dirs = { {-1, 0}, {1, 0}, {0, -1}, {0, 1} };
            for (int[] d : dirs) {
                int nr = cell[0] + d[0];
                int nc = cell[1] + d[1];
                if (nr < 0 || nr >= height || nc < 0 || nc >= width) {
                    continue;
                }
                if (visited[nr][nc] || grid[nr][nc] == WALL) {
                    continue;
                }
                visited[nr][nc] = true;
                prev[nr][nc] = cell[0] * width + cell[1];
                queue.add(new int[] {nr, nc});
            }
        }

        if (!visited[end[0]][end[1]]) {
            throw new IllegalArgumentException("迷宫无解：S 与 E 之间不存在通路");
        }

        // 回溯标记路径（S 与 E 本身保持原样）
        int cur = end[0] * width + end[1];
        int startIdx = start[0] * width + start[1];
        while (cur != startIdx) {
            int r = cur / width;
            int c = cur % width;
            if (grid[r][c] == PATH) {
                grid[r][c] = ROUTE;
            }
            cur = prev[r][c];
        }
        return toText(grid);
    }

    /**
     * 校验迷宫文本格式：非空、矩形、只含合法字符。
     */
    private static char[][] parse(String maze) {
        if (maze == null) {
            throw new IllegalArgumentException("迷宫不能为 null");
        }
        String[] lines = maze.split("\n", -1);
        List<String> rows = new ArrayList<String>();
        for (String line : lines) {
            if (!line.isEmpty()) {
                rows.add(line);
            }
        }
        if (rows.size() < MIN_SIZE) {
            throw new IllegalArgumentException("迷宫行数不足，至少为 " + MIN_SIZE);
        }
        int width = rows.get(0).length();
        if (width < MIN_SIZE) {
            throw new IllegalArgumentException("迷宫列数不足，至少为 " + MIN_SIZE);
        }
        char[][] grid = new char[rows.size()][width];
        for (int r = 0; r < rows.size(); r++) {
            String line = rows.get(r);
            if (line.length() != width) {
                throw new IllegalArgumentException("迷宫第 " + (r + 1) + " 行宽度不一致");
            }
            for (int c = 0; c < width; c++) {
                char ch = line.charAt(c);
                if (ch != WALL && ch != PATH && ch != START && ch != END && ch != ROUTE) {
                    throw new IllegalArgumentException(
                            "迷宫第 " + (r + 1) + " 行第 " + (c + 1) + " 列含非法字符: '" + ch + "'");
                }
                grid[r][c] = ch;
            }
        }
        return grid;
    }

    /** 找到指定标记（S 或 E）的坐标，找不到返回 {@code null}。 */
    private static int[] findMark(char[][] grid, char mark) {
        for (int r = 0; r < grid.length; r++) {
            for (int c = 0; c < grid[r].length; c++) {
                if (grid[r][c] == mark) {
                    return new int[] {r, c};
                }
            }
        }
        return null;
    }

    /** 尺寸校验：范围 [5, 201] 且为奇数。 */
    private static void checkSize(String name, int size) {
        if (size < MIN_SIZE || size > MAX_SIZE) {
            throw new IllegalArgumentException(
                    name + "须在 [" + MIN_SIZE + ", " + MAX_SIZE + "] 范围内，收到: " + size);
        }
        if (size % 2 == 0) {
            throw new IllegalArgumentException(name + "须为奇数，收到: " + size);
        }
    }

    /** 若 (cellRow, cellCol) 是边界内未访问的格子，连同中间墙坐标一起加入候选。 */
    private static void collectUnvisited(List<int[]> out, char[][] grid,
            int cellRow, int cellCol, int wallRow, int wallCol) {
        if (cellRow < 1 || cellRow >= grid.length - 1
                || cellCol < 1 || cellCol >= grid[0].length - 1) {
            return;
        }
        if (grid[cellRow][cellCol] == WALL) {
            out.add(new int[] {cellRow, cellCol, wallRow, wallCol});
        }
    }

    private static String toText(char[][] grid) {
        StringBuilder sb = new StringBuilder();
        for (char[] row : grid) {
            sb.append(row).append('\n');
        }
        return sb.toString();
    }
}
