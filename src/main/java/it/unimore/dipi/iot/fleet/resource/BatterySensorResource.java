package it.unimore.dipi.iot.fleet.resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

/**
 * @author Marco Picone, Ph.D. - picone.m@gmail.com
 * @project mqtt-demo-fleet-monitoring
 * @created 04/11/2020 - 14:39
 */
public class BatterySensorResource extends SmartObjectResource<Double> {

    private static final Logger logger = LoggerFactory.getLogger(BatterySensorResource.class);

    private static final double MIN_BATTERY_LEVEL = 50.0;

    private static final double MAX_BATTERY_LEVEL = 70.0;

    private static final double MIN_BATTERY_LEVEL_CONSUMPTION = 0.1;

    private static final double MAX_BATTERY_LEVEL_CONSUMPTION = 1.0;

    private static final long UPDATE_PERIOD = 5000; //5 Seconds

    private static final long TASK_DELAY_TIME = 5000; //Seconds before starting the periodic update task

    public static final String RESOURCE_TYPE = "iot:sensor:battery";

    private double updatedBatteryLevel;

    private Random random = null;

    private Timer updateTimer = null;

    public BatterySensorResource() {
        super(UUID.randomUUID().toString(), BatterySensorResource.RESOURCE_TYPE);
        init();
    }

    public BatterySensorResource(String id, String type) {
        super(id, type);
        init();
    }

    /**
     * Init internal random battery level in th range [MIN_BATTERY_LEVEL, MAX_BATTERY_LEVEL]
     */
    private void init(){

        try{

            this.random = new Random(System.currentTimeMillis());
            this.updatedBatteryLevel = MIN_BATTERY_LEVEL + this.random.nextDouble()*(MAX_BATTERY_LEVEL - MIN_BATTERY_LEVEL);

            startPeriodicEventValueUpdateTask();

        }catch (Exception e){
            logger.error("Error init Battery Resource Object ! Msg: {}", e.getLocalizedMessage());
        }

    }

    private void startPeriodicEventValueUpdateTask(){

        try{

            logger.info("Starting periodic Update Task with Period: {} ms", UPDATE_PERIOD);

            this.updateTimer = new Timer();
            this.updateTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    updatedBatteryLevel = updatedBatteryLevel - (MIN_BATTERY_LEVEL_CONSUMPTION + MAX_BATTERY_LEVEL_CONSUMPTION * random.nextDouble());
                    //logger.info("Updated Battery Level: {}", updatedBatteryLevel);
                    //TODO Check if battery level <= 0.0 !

                    notifyUpdate(updatedBatteryLevel);

                }
            }, TASK_DELAY_TIME, UPDATE_PERIOD);

        }catch (Exception e){
            logger.error("Error executing periodic resource value ! Msg: {}", e.getLocalizedMessage());
        }
    }

    @Override
    public Double loadUpdatedValue() {
        return this.updatedBatteryLevel;
    }

    public static void main(String[] args) {

        BatterySensorResource batterySensorResource = new BatterySensorResource();
        logger.info("New {} Resource Created with Id: {} ! Battery Level: {}",
                batterySensorResource.getType(),
                batterySensorResource.getId(),
                batterySensorResource.loadUpdatedValue());

        //Add Resource Listener
        batterySensorResource.addDataListener(new ResourceDataListener<Double>() {
            @Override
            public void onDataChanged(SmartObjectResource<Double> resource, Double updatedValue) {
                if(resource != null && updatedValue != null)
                    logger.info("Device: {} -> New Battery Level Received: {}", resource.getId(), updatedValue);
                else
                    logger.error("onDataChanged Callback -> Null Resource or Updated Value !");
            }
        });

    }

}
