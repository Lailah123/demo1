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

### 使用示例

```java
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
```
