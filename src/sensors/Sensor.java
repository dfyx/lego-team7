package sensors;

public interface Sensor<Type> {

    void poll();
    
    Type getValue();
    
}
