# Martian Robots

A Java 21 command-line simulation of robots on a bounded grid. Robots move one at a time, in the order listed in the input. Lost robots leave scents that protect later robots from leaving the world at the same coordinate.

## Run application with Docker

Install and start Docker. Java and Gradle are provided inside the container; neither needs to be installed on your machine.

Save your input as `input.txt` using the format shown below, then run this single command from the repository root. Repository already contains an `input.txt` that can be used:

```sh
./run-app-in-docker.sh input.txt
```

The script accepts any readable input-file path. Relative paths are resolved from your current working directory. It builds the application image without running tests or coverage checks, and streams the file to the application through standard input. The container is removed when it finishes, and the script returns the application's exit code.

The first build needs internet access to download the Java images and Gradle. Unchanged builds reuse Docker's cache. Build messages appear in the terminal. To save the simulation results to a file, run:

```sh
./run-app-in-docker.sh input.txt > output.txt
```

The [Dockerfile](Dockerfile) uses a JDK 21 build stage and a separate JRE 21 runtime stage. The runtime runs as a non-root user and contains the packaged application without Gradle or the source files.

## Build and run application with Gradle

Install a JDK 21 and use the included Gradle wrapper. No separate Gradle installation is required. The first build needs network access to download Gradle and any required dependencies.

From the repository root:

```sh
cd main
./gradlew -q run < ../input.txt
```

## Input and output

The first line gives the largest allowed x and y coordinates. Robots can occupy these coordinates; the lower-left corner is `(0, 0)`. Each robot then has one initial position line and one instruction line. `L` turns left, `R` turns right and `F` moves forward one cell.

Example `input.txt`:

```text
5 3
1 1 E
RFRFRFRF
3 2 N
FRRFLLFFRRFLL
0 3 W
LLFFFLFLFL
```

Output:

```text
1 1 E
3 3 N LOST
2 3 S
```

A lost robot is reported at its last valid position. Remaining instructions for that robot are not executed. A scent marks the square where a robot was lost. Later robots on that square ignore any command that would move them off the grid, whichever direction they face. Robots may share a coordinate.

## Validation and errors

Fixed limits are defined in [AppConstraints.java](main/src/main/java/com/robot/AppConstraints.java): the minimum and maximum coordinates, maximum instructions per robot, and maximum input bytes. Edit those constants and rebuild to change the limits. Validation and error messages use the same definitions.

The application checks the entire input before moving any robots. If the input is invalid, it prints an error message and no results. Input-format errors identify the line, counting the first line as 1.

- Coordinates must be decimal integers between 0 and 50; initial positions must also fit the specified world. Either maximum coordinate can be 0, giving a grid with a single column or row.
- The first line must contain two coordinates. Each robot's position line must contain two coordinates and a direction. Spaces and tabs around or between these values are accepted.
- Directions must be `N`, `E`, `S` or `W`. The supported commands are `L`, `R` and `F`.
- Each robot can have at most 99 commands, with no spaces or tabs on the instruction line.
- An empty instruction line (lines with `L`, `R` and `F`) is allowed and makes the robot do nothing, but ending the file immediately after a position line is an error. Blank lines where coordinates are expected are rejected.
- Input files support both Unix-style and Windows-style line breaks.
- A file without any robots is valid and produces no output.
- Save input files using UTF-8 encoding. Files must be no larger than 1MB, including spaces and line breaks. This application adds the size limit; the exercise does not require it
- There is no validation for file extension. I address this point in the "Choices I made and possible improvements" section

The packaged application's exit codes are `0` for success, `2` for invalid input and `1` for I/O or unexpected execution failures.

## Run tests with Docker

From the repository root, run all tests and generate test and coverage reports with one command:

```sh
./run-app-in-docker.sh --test
```

1. Open `docker-reports/tests/test/index.html` to view test test results
2. Open `docker-reports/jacoco/test/html/index.html` to view coverage

The script returns a nonzero exit code if the build, tests or report generation fail.

## Run tests with Gradle

Run from the project's root directory:

```sh
cd main
./gradlew test
```

1. Open `build/reports/tests/test/index.html` in the browser to view test report
2. Open `build/reports/jacoco/test/html/index.html` in the browser to view test coverage

## Choices I made and possible improvements

All the java classes live under the con.robot package. If the application grew, I would split them into groups based on what they do, such as reading input or moving robots. I also kept the setup for the three supported commands simple. Adding more tools to manage them would make the application harder to understand without much benefit at this size.

I used `int` for coordinates even though the limit of 50 would fit in a `short`. Java promotes `short` values to `int` during arithmetic operations, so storing movement results as `short` would require extra casts. Using `int` keeps coordinate calculations simple, while validation enforces the allowed range. The potential memory savings from `short` are not significant for this small application.

I used Java's built-in `Objects.requireNonNull()` to check that required values passed to constructors , such as a robot's position, are not missing (they can't be null). If a value is missing, it stops that operation immediately and identifies the missing value, making the problem easier to find. It still throws an NullPointerException, which is Java's way of reporting an error. I preferred this built-in check over writing the same check and error-handling code myself each time because it keeps the code shorter and consistent.

The application checks the whole input file before moving any robots and prepares all results before displaying them. This needs more computer memory than handling one robot at a time, but it means a mistake near the end of the file will not leave the user with an incomplete set of results. The file size limit (1MB) helps keep memory use manageable.

With more time, I would consider:

- **File validation.** There is currently no file-extension validation: the application accepts any existing, readable file, regardless of its extension. The application still validates the file's UTF-8 encoding, size and input format. With more time, I would have added file-extension validation, such as requiring only `.txt` files, alongside the existing content checks. This should be added as a future improvement
- **Handling bigger files.** Measure how long larger simulations take and how much memory they use. I would then look at ways to handle more data while still checking the whole input before starting and moving the robots in the correct order
- **Adding more robot commands.** At the moment the current implementation allows adding more instructions by implementing the Instruction interface and adding it to the instructions map in the Main class. If more instructions are needed I would move from having the map store the instructions and use a centralise class that holds all the instructions. The code is assuming that an instruction does not move the robot or it moves one grid only. If a new instruction would require the robot to move more grids then the current functionality could be used as the robot would move one grid at a time which checks if the robot get's lost, but to make things easier the class that holds the instruction, could also store a new variable that controls the number of grids the robot would move
- **Improving the tests.** Improving the main method (Main class) to have 100% coverage and adding comments to some of the unit tests which are not easy to understand and centralise some commomn functions that the tests are using. Just to reduce the duplicated code
- **Making the application easier to use.** Add built-in help and simpler ways to choose input and output files. I would also consider letting users change settings, such as the file size limit, without editing the code. Basically making the application configurable using environment variables or allowing the users to select the application constraints