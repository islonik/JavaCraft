# MathParser

MathParser is a Java top-down expression parser with a Swing UI.

## Timeline
Initially completed in 2014.

Different updates in 2026.

## Modules
- `parser` - expression parsing/evaluation engine
- `gui` - desktop calculator UI using Swing

## Requirements
- JDK 21+
- Maven 3.9+

## Supported expression features
- Numbers and operators: `+`, `-`, `*`, `/`, `%`, `^`
- Brackets: `( ... )`
- Constants: `e`, `pi`
- Variables with assignment: `x = 10`, then `x * 2`
- Implicit multiplication: `2(3+4)`, `2pi`, `3sin(90)`
- Functions (1 arg): `abs`, `acos`, `asin`, `atan`, `cbrt`, `ceil`, `cos`, `exp`, `factorial`, `floor`, `ln`, `log10`, `round`, `sin`, `sqrt`, `tan`
- Functions (2 args): `pow(x,y)`, `log(base,value)`
- Functions (many args): `min(...)`, `max(...)`, `sum(...)`, `avg(...)`
- Angle units: `DEGREE`, `GRADIAN`, `RADIAN`

## Parser limits and behavior
- Max expression length: `1024`
- Max identifier length: `32`
- `factorial(x)` accepts non-negative integers only
- `factorial(x)` overflow guard: max supported integer input is `12`
- Parser returns a `String` result:
  - numeric value for success
  - parser error message for known parser failures

## Build and test
From repository root:

### Run parser tests
```bash
mvn -pl parser test
```

### Compile GUI tests
```bash
mvn -pl gui test-compile
```

Note:
- GUI runtime tests can be unstable in headless/macOS CI environments because they create real Swing windows.

## Run GUI
Build executable GUI jar (self-contained/fat jar):

```bash
mvn clean -pl gui -am -DskipTests package
```

Run:

```bash
java -jar gui/target/mathparser.jar
```

## Example parser usage
```java
Parser parser = new Parser();
String result = parser.calculate("2(3+4) + sin(90)");
```
