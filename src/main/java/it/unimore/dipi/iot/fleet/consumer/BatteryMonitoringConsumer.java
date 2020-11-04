package it.unimore.dipi.iot.fleet.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.unimore.dipi.iot.fleet.message.ControlMessage;
import it.unimore.dipi.iot.fleet.message.TelemetryMessage;
import it.unimore.dipi.iot.fleet.resource.BatterySensorResource;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Simple MQTT Consumer using the library Eclipse Paho
 *
 * @author Marco Picone, Ph.D. - picone.m@gmail.com
 * @project mqtt-playground
 * @created 14/10/2020 - 09:19
 */
public class BatteryMonitoringConsumer {

    private final static Logger logger = LoggerFactory.getLogger(BatteryMonitoringConsumer.class);

    private static final double ALARM_BATTERY_LEVEL = 2.0;

    private static final String CONTROL_TOPIC = "control";

    //IP Address of the target MQTT Broker
    private static String BROKER_ADDRESS = "127.0.0.1";

    //PORT of the target MQTT Broker
    private static int BROKER_PORT = 1883;

    //E.g. fleet/vehicle/e0c7433d-8457-4a6b-8084-595d500076cc/telemetry/battery
    private static final String TARGET_TOPIC = "fleet/vehicle/+/telemetry/battery";

    private static final String ALARM_MESSAGE_CONTROL_TYPE = "battery_alarm_message";

    private static ObjectMapper mapper;

    private static boolean isAlarmNotified = false;

    public static void main(String [ ] args) {

    	logger.info("MQTT Consumer Tester Started ...");

        try{

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

            Map<String, Double> batteryHistoryMap = new HashMap<>();
            mapper = new ObjectMapper();

            //Subscribe to the target topic #. In that case the consumer will receive (if authorized) all the message
            //passing through the broker
            client.subscribe(TARGET_TOPIC, (topic, msg) -> {

                Optional<TelemetryMessage> telemetryMessageOptional = parseTelemetryMessagePayload(msg);

                if(telemetryMessageOptional.isPresent() && telemetryMessageOptional.get().getType().equals(BatterySensorResource.RESOURCE_TYPE)){

                    Double newBatteryLevel = (Double)telemetryMessageOptional.get().getDataValue();
                    logger.info("New Battery Telemetry Data Received ! Battery Level: {}", newBatteryLevel);

                    //If is the first value
                    if(!batteryHistoryMap.containsKey(topic)){
                        logger.info("New Battery Level Saved for: {}", topic);
                        batteryHistoryMap.put(topic, newBatteryLevel);
                        isAlarmNotified = false;
                    }
                    else {
                        if(isBatteryLevelAlarm(batteryHistoryMap.get(topic), newBatteryLevel) && !isAlarmNotified){
                            logger.info("BATTERY LEVEL ALARM DETECTED ! Sending Control Notification ...");
                            isAlarmNotified = true;

                            //Incoming Topic = fleet/vehicle/fa18f676-8198-4e9f-90e0-c50a5e419b94/telemetry/battery
                            String controlTopic = String.format("%s/%s", topic.replace("/telemetry/battery", ""), CONTROL_TOPIC);
                            publishControlMessage(client, controlTopic, new ControlMessage(ALARM_MESSAGE_CONTROL_TYPE, new HashMap<>(){
                                {
                                    put("charging_station_id", "cs00001");
                                    put("charging_station_lat", 44.79503800000001);
                                    put("charging_station_lng", 10.32686911666667);
                                }
                            }));
                        }
                    }

                }

            });

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private static boolean isBatteryLevelAlarm(Double originalValue, Double newValue){
        return originalValue - newValue >= ALARM_BATTERY_LEVEL;
    }

    private static Optional<TelemetryMessage> parseTelemetryMessagePayload(MqttMessage mqttMessage){

        try{

            if(mqttMessage == null)
                return Optional.empty();

            byte[] payloadByteArray = mqttMessage.getPayload();
            String payloadString = new String(payloadByteArray);

            TelemetryMessage telemetryMessage = (TelemetryMessage) mapper.readValue(payloadString, TelemetryMessage.class);

            return Optional.of(telemetryMessage);

        }catch (Exception e){
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
