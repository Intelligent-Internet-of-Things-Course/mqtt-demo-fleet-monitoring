package it.unimore.dipi.iot.fleet.process;

import it.unimore.dipi.iot.fleet.device.VehicleMqttSmartObject;
import it.unimore.dipi.iot.fleet.resource.BatterySensorResource;
import it.unimore.dipi.iot.fleet.resource.GpsGpxSensorResource;
import it.unimore.dipi.iot.fleet.resource.SmartObjectResource;
import org.eclipse.paho.client.mqttv3.IMqttClient;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttClientPersistence;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.UUID;

/**
 * @author Marco Picone, Ph.D. - picone.m@gmail.com
 * @project mqtt-demo-fleet-monitoring
 * @created 04/11/2020 - 16:15
 */
public class VehicleSmartObjectProcess {

    private static final Logger logger = LoggerFactory.getLogger(VehicleSmartObjectProcess.class);

    private static String MQTT_BROKER_IP = "127.0.0.1";

    private static int MQTT_BROKER_PORT = 1883;

    public static void main(String[] args) {

        try{

            //Generate Random Vehicle UUID
            String vehicleId = UUID.randomUUID().toString();

            //Create MQTT Client
            MqttClientPersistence persistence = new MemoryPersistence();
            IMqttClient mqttClient = new MqttClient(String.format("tcp://%s:%d",
                    MQTT_BROKER_IP,
                    MQTT_BROKER_PORT),
                    vehicleId,
                    persistence);

            MqttConnectOptions options = new MqttConnectOptions();
            options.setAutomaticReconnect(true);
            options.setCleanSession(true);
            options.setConnectionTimeout(10);

            //Connect to MQTT Broker
            mqttClient.connect(options);

            logger.info("MQTT Client Connected ! Client Id: {}", vehicleId);

            VehicleMqttSmartObject vehicleMqttSmartObject = new VehicleMqttSmartObject();
            vehicleMqttSmartObject.init(vehicleId, mqttClient, new HashMap<String, SmartObjectResource>(){
                {
                    put("gps", new GpsGpxSensorResource());
                    put("battery", new BatterySensorResource());
                }
            });

            vehicleMqttSmartObject.start();

        }catch (Exception e){
            e.printStackTrace();
        }

    }

}
