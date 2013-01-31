package robot;
import lejos.nxt.SensorPort;
import sensors.Bumper;
import sensors.Head;
import sensors.LightSensor;
import sensors.Sensor;
import actors.Engine;


public class Platform {

    public static final SensorPort LEFT_TOUCH_PORT = SensorPort.S1;
    public static final SensorPort RIGHT_TOUCH_PORT = SensorPort.S4;
    public static final SensorPort ULTRASONIC_PORT = SensorPort.S2; 
    public static final SensorPort LIGHT_PORT = SensorPort.S3;
    
    public static final Head HEAD = new Head();
    public static final LightSensor LIGHT_SENSOR = new LightSensor(LIGHT_PORT);
    public static final Bumper LEFT_BUMPER = new Bumper(LEFT_TOUCH_PORT);
    public static final Bumper RIGHT_BUMPER = new Bumper(RIGHT_TOUCH_PORT);
    
    public static final Sensor<?> SENSORS[] = new Sensor[] {
            HEAD, LIGHT_SENSOR, LEFT_BUMPER, RIGHT_BUMPER };
    
    public static final Engine ENGINE = Engine.getInstance();    
    
}
