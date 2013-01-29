package sensors;

public class Head implements Sensor<Integer> {	

    @Override
    public void poll() {
        // TODO Might be used to allow synchronisation with main loop
    }

    @Override
    public Integer getValue() {
        // TODO maybe just return the last sensor reading
        return null;
    }
}
