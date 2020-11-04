package it.unimore.dipi.iot.fleet.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.unimore.dipi.iot.fleet.message.ControlMessage;
import it.unimore.dipi.iot.fleet.message.TelemetryMessage;
import it.unimore.dipi.iot.fleet.model.GpsLocationDescriptor;
import it.unimore.dipi.iot.fleet.model.TrafficEventDescriptor;
import it.unimore.dipi.iot.fleet.resource.GpsGpxSensorResource;
import it.unimore.dipi.iot.fleet.utils.GpsUtils;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Traffic Monitoring for active fleet vehicles
 *
 * @author Marco Picone, Ph.D. - picone.m@gmail.com
 * @project mqtt-demo-fleet-monitoring
 * @created 04/11/2020 - 19:40
 */
public class TrafficMonitoringConsumer {

    private final static Logger logger = LoggerFactory.getLogger(TrafficMonitoringConsumer.class);

    private static final double ALARM_BATTERY_LEVEL = 2.0;

    private static final String CONTROL_TOPIC = "control";

    //IP Address of the target MQTT Broker
    private static String BROKER_ADDRESS = "127.0.0.1";

    //PORT of the target MQTT Broker
    private static int BROKER_PORT = 1883;

    //E.g. fleet/vehicle/e0c7433d-8457-4a6b-8084-595d500076cc/telemetry/gps
    private static final String TARGET_TOPIC = "fleet/vehicle/+/telemetry/gps";

    private static final String ALARM_MESSAGE_CONTROL_TYPE = "traffic_alarm_message";

    private static ObjectMapper mapper;

    private static boolean isAlarmNotified = false;

    private static List<TrafficEventDescriptor> trafficEventList;

    //Km threshold to notify a vehicle close to a traffic alert
    private static double TRAFFIC_EVENT_DISTANCE_ALERT_THRESHOLD = 2;

    public static void main(String [ ] args) {

    	logger.info("MQTT Consumer Tester Started ...");

        try{

            initDemoTrafficEvent();

            //Generate a random MQTT client ID using the UUID class
            String clientId = UUID.randomUUID().toString();

            //Represents a persistent data store, used to store outbound and inbound messages while they
            //are in flight, enabling delivery to the QoS specified. In that case use a memory persistence.
            //When the application stops all the temporary data will be deleted.
            MqttClientPersistence persistence = new MemoryPersistence();

            //The the persistence is not passed to the constructor the default file persistence is used.
            //In case of a file-based storage the same MQTT client UUID should be used
            IMqttClient client = new MqttClient(
                    String.format("tcp://%s:%d", BROKER_ADDRESS, BROKER_PORT), //Create the URL from IP and PORT
                    clientId,
                    persistence);

            //Define MQTT Connection Options such as reconnection, persistent/clean session and connection timeout
            //Authentication option can be added -> See AuthProducer example
            MqttConnectOptions options = new MqttConnectOptions();
            options.setAutomaticReconnect(true);
            options.setCleanSession(true);
            options.setConnectionTimeout(10);

            //Connect to the target broker
            client.connect(options);

            logger.info("Connected ! Client Id: {}", clientId);

            mapper = new ObjectMapper();

            //Subscribe to the target topic #. In that case the consumer will receive (if authorized) all the message
            //passing through the broker
            logger.info("Subscribing to topic: {}", TARGET_TOPIC);

            client.subscribe(TARGET_TOPIC, (topic, msg) -> {

                //logger.info("Received Data (Topic: {}) -> Data: {}", topic, new String(msg.getPayload()));

                Optional<TelemetryMessage<GpsLocationDescriptor>> telemetryMessageOptional = parseTelemetryMessagePayload(msg);

                if(telemetryMessageOptional.isPresent() && telemetryMessageOptional.get().getType().equals(GpsGpxSensorResource.RESOURCE_TYPE)){

                    GpsLocationDescriptor gpsLocationDescriptor = telemetryMessageOptional.get().getDataValue();
                    List<TrafficEventDescriptor> trafficEventDescriptorList = getAvailableTrafficEvents(
                            gpsLocationDescriptor.getLatitude(),
                            gpsLocationDescriptor.getLongitude());

                    //TODO Improve handling isAlarmNotified Flag
                    if(trafficEventDescriptorList.size() > 0 && !isAlarmNotified){

                        String targetTopic = String.format("%s/%s", topic.replace("/telemetry/gps", ""), CONTROL_TOPIC);

                        logger.info("Relevant Traffic Event Detected ! Sending Control to: {}", targetTopic);

                        ControlMessage controlMessage = new ControlMessage();
                        controlMessage.setType(ALARM_MESSAGE_CONTROL_TYPE);
                        controlMessage.setTimestamp(System.currentTimeMillis());
                        controlMessage.setMetadata(new HashMap<>(){
                            {
                                put("event_list", trafficEventDescriptorList);
                            }
                        });

                        publishControlMessage(client, targetTopic, controlMessage);

                        isAlarmNotified = true;
                    }
                }
            });

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private static void initDemoTrafficEvent() {
        trafficEventList = new ArrayList<>();
        trafficEventList.add(new TrafficEventDescriptor(TrafficEventDescriptor.JAM_TRAFFIC_EVENT,
                44.79503800000001,
                10.32686911666667,
                System.currentTimeMillis()));
    }

    private static List<TrafficEventDescriptor> getAvailableTrafficEvents(double latitude, double longitude){

        if(trafficEventList != null)
            return trafficEventList.stream().filter(trafficEventDescriptor -> {
                if(trafficEventDescriptor != null && GpsUtils.distance(
                        latitude,
                        longitude,
                        trafficEventDescriptor.getLatitude(),
                        trafficEventDescriptor.getLongitude(),
                        "K") <= TRAFFIC_EVENT_DISTANCE_ALERT_THRESHOLD)
                    return true;
                else
                    return false;
            }).collect(Collectors.toList());
        else
            return new ArrayList<>();
    }

    private static boolean isBatteryLevelAlarm(Double originalValue, Double newValue){
        return originalValue - newValue >= ALARM_BATTERY_LEVEL;
    }

    private static Optional<TelemetryMessage<GpsLocationDescriptor>> parseTelemetryMessagePayload(MqttMessage mqttMessage){

        try{

            if(mqttMessage == null)
                return Optional.empty();

            byte[] payloadByteArray = mqttMessage.getPayload();
            String payloadString = new String(payloadByteArray);

            return Optional.ofNullable(mapper.readValue(payloadString, new TypeReference<TelemetryMessage<GpsLocationDescriptor>>() {}));

        }catch (Exception e){
            e.printStackTrace();
            return Optional.empty();
        }
    }

    private static void publishControlMessage(IMqttClient mqttClient, String topic, ControlMessage controlMessage) throws MqttException, JsonProcessingException {

        new Thread(new Runnable() {
            @Override
            public void run() {

               try{

                   logger.info("Sending to topic: {} -> Data: {}", topic, controlMessage);

                   if(mqttClient != null && mqttClient.isConnected() && controlMessage != null && topic != null){

                       String messagePayload = mapper.writeValueAsString(controlMessage);

                       MqttMessage mqttMessage = new MqttMessage(messagePayload.getBytes());
                       mqttMessage.setQos(0);

                       mqttClient.publish(topic, mqttMessage);

                       logger.info("Data Correctly Published to topic: {}", topic);

                   }
                   else
                       logger.error("Error: Topic or Msg = Null or MQTT Client is not Connected !");

               }catch (Exception e){
                   e.printStackTrace();
               }

            }
        }).start();
    }
}
