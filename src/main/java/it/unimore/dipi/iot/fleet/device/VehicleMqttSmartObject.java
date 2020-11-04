package it.unimore.dipi.iot.fleet.device;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.unimore.dipi.iot.fleet.message.TelemetryMessage;
import it.unimore.dipi.iot.fleet.resource.BatterySensorResource;
import it.unimore.dipi.iot.fleet.resource.GpsGpxSensorResource;
import it.unimore.dipi.iot.fleet.resource.ResourceDataListener;
import it.unimore.dipi.iot.fleet.resource.SmartObjectResource;
import org.eclipse.paho.client.mqttv3.IMqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * @author Marco Picone, Ph.D. - picone.m@gmail.com
 * @project mqtt-demo-fleet-monitoring
 * @created 04/11/2020 - 16:01
 */
public class VehicleMqttSmartObject {

    private static final Logger logger = LoggerFactory.getLogger(VehicleMqttSmartObject.class);

    private static final String BASIC_TOPIC = "fleet/vehicle";

    private static final String TELEMETRY_TOPIC = "telemetry";

    private static final String EVENT_TOPIC = "event";

    private static final String CONTROL_TOPIC = "control";

    private static final String COMMAND_TOPIC = "command";

    private String vehicleId;

    private ObjectMapper mapper;

    private IMqttClient mqttClient;

    private Map<String, SmartObjectResource> resourceMap;

    public VehicleMqttSmartObject() {
        this.mapper = new ObjectMapper();
    }

    /**
     * Init the vehicle smart object with its ID, the MQTT Client and the Map of managed resources
     * @param vehicleId
     * @param mqttClient
     * @param resourceMap
     */
    public void init(String vehicleId, IMqttClient mqttClient, Map<String, SmartObjectResource> resourceMap){

        this.vehicleId = vehicleId;
        this.mqttClient = mqttClient;
        this.resourceMap = resourceMap;

        logger.info("Vehicle Smart Object correctly created ! Resource Number: {}", resourceMap.keySet().size());
    }

    /**
     * Start vehicle behaviour
     */
    public void start(){

        try{

            if(this.mqttClient != null &&
                this.vehicleId != null  && this.vehicleId.length() > 0 &&
                this.resourceMap != null && resourceMap.keySet().size() > 0){

                logger.info("Starting Vehicle Emulator ....");

                registerToControlChannel();

                registerToAvailableResources();


            }

        }catch (Exception e){
            logger.error("Error Starting the Vehicle Emulator ! Msg: {}", e.getLocalizedMessage());
        }

    }

    private void registerToControlChannel() {
        //TODO Implement
    }

    private void registerToAvailableResources(){
        try{

            this.resourceMap.entrySet().forEach(resourceEntry -> {

                if(resourceEntry.getKey() != null && resourceEntry.getValue() != null){
                    SmartObjectResource smartObjectResource = resourceEntry.getValue();

                    logger.info("Registering to Resource {} (id: {}) notifications ...",
                            smartObjectResource.getType(),
                            smartObjectResource.getId());

                    if(smartObjectResource.getType().equals(GpsGpxSensorResource.RESOURCE_TYPE) || smartObjectResource.getType().equals(BatterySensorResource.RESOURCE_TYPE)){

                        smartObjectResource.addDataListener(new ResourceDataListener() {
                            @Override
                            public void onDataChanged(SmartObjectResource resource, Object updatedValue) {
                                try {
                                    publishTelemetryData(
                                            String.format("%s/%s/%s/%s", BASIC_TOPIC, vehicleId, TELEMETRY_TOPIC, resourceEntry.getKey()),
                                            new TelemetryMessage(smartObjectResource.getType(), updatedValue));
                                } catch (MqttException | JsonProcessingException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                    }

                }
            });

        }catch (Exception e){
            logger.error("Error Registering to Resource ! Msg: {}", e.getLocalizedMessage());
        }
    }

    /**
     * Stop the emulated vehicle
     */
    public void stop(){
        //TODO Implement a proper closing method
    }

    private void publishTelemetryData(String topic, TelemetryMessage telemetryMessage) throws MqttException, JsonProcessingException {

        logger.info("Sending to topic: {} -> Data: {}", topic, telemetryMessage);

        if(this.mqttClient != null && this.mqttClient.isConnected() && telemetryMessage != null && topic != null){

            String messagePayload = mapper.writeValueAsString(telemetryMessage);

            MqttMessage mqttMessage = new MqttMessage(messagePayload.getBytes());
            mqttMessage.setQos(0);

            mqttClient.publish(topic, mqttMessage);

            logger.info("Data Correctly Published to topic: {}", topic);

        }
        else
            logger.error("Error: Topic or Msg = Null or MQTT Client is not Connected !");
    }
}
