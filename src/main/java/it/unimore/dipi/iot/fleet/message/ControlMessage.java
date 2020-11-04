package it.unimore.dipi.iot.fleet.message;

import java.util.Map;

/**
 * @author Marco Picone, Ph.D. - picone.m@gmail.com
 * @project mqtt-demo-fleet-monitoring
 * @created 04/11/2020 - 16:00
 */
public class ControlMessage extends GenericMessage{

    public ControlMessage() {
    }

    public ControlMessage(String type, Map<String, Object> metadata) {
        super(type, metadata);
    }

    public ControlMessage(String type, long timestamp, Map<String, Object> metadata) {
        super(type, timestamp, metadata);
    }
}
