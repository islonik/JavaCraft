package my.javacraft.mathparser.parser;

/**
 * Types of degrees/angles.
 *
 * @author Nikita Lipatov.
 **/
public interface ParserType {

    /**
     * A degree, usually denoted by °, is a measurement of plane angle, representing 1⁄360 of a full rotation.
     **/
    int DEGREE = 1;

    /**
     * The gradian is a unit of plane angle, equivalent to 1⁄400 of a turn.
     **/
    int GRADUS = 2;

    /**
     * The radian is the standard unit of angular measure, used in many areas of mathematics.
     * An angle's measurement in radians is numerically equal to the length of a corresponding arc of a unit circle, so one radian is just under 57.3 degrees.
     **/
    int RADIAN = 3;
}
