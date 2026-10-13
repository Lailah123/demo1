# demo1

一个极简 Java 示例项目，演示问候输出、常用工具类与单元测试。

## 环境要求

- JDK 8+
- Maven 3.6+

## 构建与测试

```bash
# 运行单元测试
mvn test

# 打包
mvn package
```

## 功能模块

| 类 | 功能 |
| --- | --- |
| `Test1` | 基础问候输出 |
| `Greeter` | 多语言问候（中/英/日/韩/西/法） |
| `Calculator` | 四则运算、阶乘、快速幂 |
| `StringUtils` | 反转、回文判断、单词计数、标题化 |
| `FizzBuzz` | 经典 FizzBuzz 序列 |
| `NumberUtils` | 斐波那契、质数判断、最大公约数 |
| `AsciiArt` | 图片转 ASCII 字符画（纯文本 / 彩色 HTML / 终端 ANSI） |
| `LetterArt` | 字母转字符艺术字（A-Z、0-9 与常用标点，5 行点阵 banner） |
| `TextTable` | 文本表格渲染（中英文混排对齐、逐列对齐方式） |
| `Expression` | 数学表达式求值（运算符、常量与常用函数，零依赖解析器） |
| `Plotter` | ASCII 函数绘图（终端字符曲线、坐标轴、自动 y 轴范围） |

### 使用示例

```java
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

// 多语言问候
String hi = Greeter.greet("张三", Greeter.Language.CHINESE); // 你好，张三！

// 计算器
double ratio = Calculator.divide(10.0, 4.0);   // 2.5
long fact = Calculator.factorial(5);            // 120

// 字符串工具
boolean pal = StringUtils.isPalindrome("A man, a plan, a canal: Panama"); // true
String title = StringUtils.titleCase("hello world");                      // Hello World

// FizzBuzz
List<String> seq = FizzBuzz.upTo(15);

// 数字工具
long fib = NumberUtils.fibonacci(10);  // 55
boolean prime = NumberUtils.isPrime(97); // true

// 图片转 ASCII 字符画（纯文本 / 彩色 HTML / 终端 ANSI）
try {
    String ascii = AsciiArt.toAscii(new File("photo.png"), 80);
    String html  = AsciiArt.toHtml(new File("photo.png"), 80);
    String ansi  = AsciiArt.toAnsi(new File("photo.png"), 80);
} catch (IOException e) {
    // 文件读取失败时的处理
}

// 字母转字符艺术字：一行文本渲染为固定 5 行大字
System.out.println(LetterArt.render("HI"));
// #   # #####
// #   #   #
// #####   #
// #   #   #
// #   # #####

List<String> bannerLines = LetterArt.renderLines("OK", 3); // 逐行返回，不含换行符，字距 3
Set<Character> font = LetterArt.supportedCharacters();     // 受支持字符清单

// 文本表格（中英文混排也能对齐）
List<String[]> tableRows = Arrays.asList(
        new String[] {"张三", "北京", "28"},
        new String[] {"Li Lei", "Shanghai", "9"});
String table = TextTable.render(
        new String[] {"姓名", "城市", "年龄"},
        tableRows, TextTable.Align.LEFT, TextTable.Align.LEFT, TextTable.Align.RIGHT);

// 数学表达式求值：编译一次即可反复代入不同的 x
Expression decay = Expression.parse("sin(x) * exp(-x / 5)");
double value = decay.evaluate(2.0);        // 0.609...

// ASCII 函数绘图：在终端里画出曲线
String curve = Plotter.plot("sin(x)", -6.5, 6.5, 50, 13);
```

`Plotter.plot("sin(x)", -6.5, 6.5, 50, 13)` 的输出：

```text
                         |
      /--                |   /---
    /-   \               |  /    \
   /      \              | /      \
  /        \             |/        \
  |         \            /          |            /
-/-----------\-----------|----------\-----------/-
*             |         /|           \          |
              \        / |            \        /
               \      /  |             \      /
                \    /   |              \   /-
                 \---    |               \--
                         |
```

`LetterArt` 支持的字符为 A-Z、0-9、空格与常用标点（`. , ! ? ' " - _ + = : ; ( ) [ ] /`），
小写字母会自动转为大写；传入不支持的字符时抛出 `IllegalArgumentException`。

`Expression` 支持 `+ - * / %`、乘方 `^`（右结合，且 `-2^2` 等于 `-4`）、常量 `pi` 与 `e`、
变量 `x`，以及 `sin cos tan asin acos atan sinh cosh tanh sqrt cbrt abs exp ln lg
floor ceil round signum` 和双参数函数 `pow atan2 hypot min max`；标识符不区分大小写，
乘号不可省略（`2x` 需写成 `2 * x`）。

`Plotter` 把曲线画在 `width × height` 的字符网格上，列对应 x 轴、行对应 y 轴，
用 `-`、`/`、`\`、`|` 表示走向；坐标轴落在画布内时会一并画出并标出原点 `+`。
不指定 y 轴范围时按区间内的函数值自动推导；函数值超出范围或为 `NaN` / 无穷大
（如 `1/x` 在 `x = 0` 处）的位置会断开，曲线自然出现缺口。
