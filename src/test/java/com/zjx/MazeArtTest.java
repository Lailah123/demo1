package com.zjx;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.List;

import org.junit.jupiter.api.Test;

/**
 * {@link MazeArt} 的单元测试。
 *
 * <p>生成算法带种子可复现，测试直接断言生成结果的确定性与求解结果的正确性，
 * 保证测试封闭、可复现。</p>
 */
class MazeArtTest {

    /** 手工构造的最小迷宫（5×5），S 与 E 之间存在唯一通路。 */
    private static final String MIN_MAZE =
            "#####\n"
            + "#S#E#\n"
            + "# # #\n"
            + "#   #\n"
            + "#####\n";

    /** {@link #MIN_MAZE} 求解后的期望结果：* 标注唯一最短路径。 */
    private static final String MIN_MAZE_SOLVED =
            "#####\n"
            + "#S#E#\n"
            + "#*#*#\n"
            + "#***#\n"
            + "#####\n";

    @Test
    void generateIsDeterministicWithSameSeed() {
        String first = MazeArt.generate(21, 11, 42L);
        String second = MazeArt.generate(21, 11, 42L);
        assertEquals(first, second, "相同种子应生成完全相同的迷宫");
    }

    @Test
    void generateDiffersWithDifferentSeeds() {
        String first = MazeArt.generate(21, 11, 1L);
        String second = MazeArt.generate(21, 11, 2L);
        assertNotEquals(first, second, "不同种子应生成不同的迷宫");
    }

    @Test
    void generateProducesExactDimensions() {
        String maze = MazeArt.generate(21, 11, 7L);
        List<String> lines = toLines(maze);
        assertEquals(11, lines.size(), "行数应等于 height");
        for (String line : lines) {
            assertEquals(21, line.length(), "每行宽度应等于 width");
        }
    }

    @Test
    void generatePlacesStartAndEndCorrectly() {
        String maze = MazeArt.generate(21, 11, 7L);
        List<String> lines = toLines(maze);
        assertEquals(MazeArt.START, lines.get(1).charAt(1), "起点 S 应位于 (1, 1)");
        assertEquals(MazeArt.END, lines.get(9).charAt(19), "终点 E 应位于 (height-2, width-2)");
    }

    @Test
    void generateKeepsBorderWallsIntact() {
        String maze = MazeArt.generate(21, 11, 7L);
        List<String> lines = toLines(maze);
        for (int c = 0; c < 21; c++) {
            assertEquals(MazeArt.WALL, lines.get(0).charAt(c), "首行应全为墙");
            assertEquals(MazeArt.WALL, lines.get(10).charAt(c), "末行应全为墙");
        }
        for (int r = 0; r < 11; r++) {
            assertEquals(MazeArt.WALL, lines.get(r).charAt(0), "首列应全为墙");
            assertEquals(MazeArt.WALL, lines.get(r).charAt(20), "末列应全为墙");
        }
    }

    @Test
    void generateProducesPerfectMazeWhereAllCellsReachable() {
        // 完美迷宫性质：从 S 出发可到达所有通路格（无孤立区域）
        String maze = MazeArt.generate(31, 21, 123L);
        assertTrue(allPathCellsReachable(maze), "从 S 出发应能到达所有通路格");
    }

    @Test
    void generateRejectsInvalidSizes() {
        assertThrows(IllegalArgumentException.class, () -> MazeArt.generate(4, 11),
                "偶数宽度应被拒绝");
        assertThrows(IllegalArgumentException.class, () -> MazeArt.generate(21, 10),
                "偶数高度应被拒绝");
        assertThrows(IllegalArgumentException.class, () -> MazeArt.generate(3, 11),
                "小于下限的宽度应被拒绝");
        assertThrows(IllegalArgumentException.class, () -> MazeArt.generate(203, 11),
                "大于上限的宽度应被拒绝");
        assertThrows(IllegalArgumentException.class, () -> MazeArt.generate(21, 203),
                "大于上限的高度应被拒绝");
    }

    @Test
    void solveFindsShortestPathOnMinimalMaze() {
        assertEquals(MIN_MAZE_SOLVED, MazeArt.solve(MIN_MAZE),
                "唯一通路应被 * 完整标注，S 与 E 保持原样");
    }

    @Test
    void solveOnlyAddsRouteMarks() {
        String maze = MazeArt.generate(31, 15, 99L);
        String solved = MazeArt.solve(maze);
        assertEquals(maze, solved.replace('*', ' '),
                "求解只应把部分通路格替换为 *，不应改动墙与起终点");
    }

    @Test
    void solveMarksConnectedPathFromStartToEnd() {
        String solved = MazeArt.solve(MazeArt.generate(31, 15, 99L));
        List<String> lines = toLines(solved);
        assertEquals(MazeArt.START, lines.get(1).charAt(1), "求解结果应保留起点 S");
        assertTrue(lines.get(13).contains(String.valueOf(MazeArt.END)), "求解结果应保留终点 E");
        assertTrue(pathReachable(solved), "S 应能沿通路（含 * 格）到达 E");
        assertTrue(solved.indexOf('*') >= 0, "求解结果应至少包含一个路径标记 *");
    }

    @Test
    void solveResultIsIdempotent() {
        // solve() 的输出可再次作为输入（* 视为通路），且最短路径不变
        String maze = MazeArt.generate(9, 9, 5L);
        String solved = MazeArt.solve(maze);
        assertNotNull(MazeArt.solve(solved), "含 * 的迷宫应可再次求解");
        assertEquals(solved, MazeArt.solve(solved), "再次求解应得到相同结果");
    }

    @Test
    void solveRejectsMazeWithoutMarks() {
        String noMarks = "#####\n"
                + "#   #\n"
                + "#   #\n"
                + "#   #\n"
                + "#####\n";
        assertThrows(IllegalArgumentException.class, () -> MazeArt.solve(noMarks),
                "缺少 S 或 E 应被拒绝");
    }

    @Test
    void solveRejectsUnsolvableMaze() {
        String blocked = "#######\n"
                + "#S#  E#\n"
                + "# #####\n"
                + "#     #\n"
                + "#######\n";
        assertThrows(IllegalArgumentException.class, () -> MazeArt.solve(blocked),
                "S 与 E 被墙隔断时应报无解");
    }

    @Test
    void solveRejectsMalformedMaze() {
        assertThrows(IllegalArgumentException.class, () -> MazeArt.solve(null),
                "null 迷宫应被拒绝");
        assertThrows(IllegalArgumentException.class, () -> MazeArt.solve("#####\n#S E#\n"),
                "行数不足应被拒绝");
        assertThrows(IllegalArgumentException.class, () -> MazeArt.solve("#####\n#S E#\n#####\n"),
                "行数不足应被拒绝");
        assertThrows(IllegalArgumentException.class,
                () -> MazeArt.solve("#######\n#S  E#\n#   # #\n#     #\n#######\n"),
                "行宽不一致应被拒绝");
        assertThrows(IllegalArgumentException.class,
                () -> MazeArt.solve("#####\n#SxE#\n#   #\n#   #\n#####\n"),
                "非法字符应被拒绝");
    }

    // ------------------------------------------------------------------

    /** 按行切分并去掉末尾换行产生的空元素（输出约定与 {@code LetterArt} 一致，以换行结尾）。 */
    private static List<String> toLines(String text) {
        List<String> lines = new ArrayList<String>(Arrays.asList(text.split("\n", -1)));
        if (!lines.isEmpty() && lines.get(lines.size() - 1).isEmpty()) {
            lines.remove(lines.size() - 1);
        }
        return lines;
    }

    /** 从 S 出发洪泛填充，判断是否可到达所有非墙格子。 */
    private static boolean allPathCellsReachable(String maze) {
        boolean[][] seen = flood(maze, cell -> cell != MazeArt.WALL);
        for (int r = 0; r < seen.length; r++) {
            for (int c = 0; c < seen[r].length; c++) {
                char ch = mazeLines(maze).get(r).charAt(c);
                if (ch != MazeArt.WALL && !seen[r][c]) {
                    return false;
                }
            }
        }
        return true;
    }

    /** 从 S 出发洪泛填充，判断能否到达 E。 */
    private static boolean pathReachable(String maze) {
        boolean[][] seen = flood(maze, cell -> cell != MazeArt.WALL);
        for (int r = 0; r < seen.length; r++) {
            for (int c = 0; c < seen[r].length; c++) {
                if (mazeLines(maze).get(r).charAt(c) == MazeArt.END) {
                    return seen[r][c];
                }
            }
        }
        return false;
    }

    private interface CellPredicate {
        boolean accept(char cell);
    }

    private static boolean[][] flood(String maze, CellPredicate passable) {
        List<String> lines = mazeLines(maze);
        int height = lines.size();
        int width = lines.get(0).length();
        boolean[][] visited = new boolean[height][width];
        Deque<int[]> queue = new ArrayDeque<int[]>();
        for (int r = 0; r < height; r++) {
            for (int c = 0; c < width; c++) {
                if (lines.get(r).charAt(c) == MazeArt.START) {
                    visited[r][c] = true;
                    queue.add(new int[] {r, c});
                }
            }
        }
        int[][] dirs = { {-1, 0}, {1, 0}, {0, -1}, {0, 1} };
        while (!queue.isEmpty()) {
            int[] cell = queue.poll();
            for (int[] d : dirs) {
                int nr = cell[0] + d[0];
                int nc = cell[1] + d[1];
                if (nr < 0 || nr >= height || nc < 0 || nc >= width || visited[nr][nc]) {
                    continue;
                }
                if (!passable.accept(lines.get(nr).charAt(nc))) {
                    continue;
                }
                visited[nr][nc] = true;
                queue.add(new int[] {nr, nc});
            }
        }
        return visited;
    }

    private static List<String> mazeLines(String maze) {
        return toLines(maze);
    }
}
