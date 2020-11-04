package it.unimore.dipi.iot.fleet.resource;

import io.jenetics.jpx.GPX;
import io.jenetics.jpx.Track;
import io.jenetics.jpx.TrackSegment;
import io.jenetics.jpx.WayPoint;
import it.unimore.dipi.iot.fleet.model.GpsLocationDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Marco Picone, Ph.D. - picone.m@gmail.com
 * @project mqtt-demo-fleet-monitoring
 * @created 04/11/2020 - 15:03
 */
public class GpsGpxSensorResource extends SmartObjectResource<GpsLocationDescriptor> {

    private static final Logger logger = LoggerFactory.getLogger(GpsGpxSensorResource.class);

    public static final String RESOURCE_TYPE = "iot:sensor:gps";

    private static final String GPX_FILE_NAME = "tracks/demo.gpx";

    private static final long UPDATE_PERIOD = 1000; //5 Seconds

    private static final long TASK_DELAY_TIME = 5000; //Seconds before starting the periodic update task

    private Timer updateTimer = null;

    private GpsLocationDescriptor updatedGpsLocationDescriptor = null;

    private List<WayPoint> wayPointList = null;

    private ListIterator<WayPoint> wayPointListIterator;

    public GpsGpxSensorResource() {
        super(UUID.randomUUID().toString(), GpsGpxSensorResource.RESOURCE_TYPE);
        init();
    }

    public GpsGpxSensorResource(String id, String type) {
        super(id, type);
        init();
    }

    /**
     * - Load Gpx waypoint
     * - Start Periodic Location update from available GPX points
     */
    private void init(){

        try{

            this.updatedGpsLocationDescriptor = new GpsLocationDescriptor();

            this.wayPointList = GPX.read(GPX_FILE_NAME).tracks()
                    .flatMap(Track::segments)
                    .flatMap(TrackSegment::points)
                    .collect(Collectors.toList());

            logger.info("GPX File WayPoint correctly loaded ! Size: {}", this.wayPointList.size());

            this.wayPointListIterator = this.wayPointList.listIterator();

            startPeriodicEventValueUpdateTask();

        }catch(Exception e){
            logger.error("Error init Resource Object ! Msg: {}", e.getLocalizedMessage());
        }

    }

    private void startPeriodicEventValueUpdateTask(){

        try{

            logger.info("Starting periodic Update Task with Period: {} ms", UPDATE_PERIOD);

            this.updateTimer = new Timer();
            this.updateTimer.schedule(new TimerTask() {
                @Override
                public void run() {

                    if(wayPointListIterator.hasNext()){

                        WayPoint currentWayPoint = wayPointListIterator.next();

                        //logger.info("{} -> Lat:{}, Lng:{}",
                        //        RESOURCE_TYPE,
                        //        currentWayPoint.getLatitude(),
                        //        currentWayPoint.getLongitude());

                        updatedGpsLocationDescriptor = new GpsLocationDescriptor(
                                currentWayPoint.getLatitude().doubleValue(),
                                currentWayPoint.getLongitude().doubleValue(),
                                (currentWayPoint.getElevation().isPresent() ? currentWayPoint.getElevation().get().doubleValue() : 0.0),
                                GpsLocationDescriptor.FILE_LOCATION_PROVIDER);

                        notifyUpdate(updatedGpsLocationDescriptor);

                    }
                    //At the end of the WayPoint List
                    else{
                        logger.info("Reversing WayPoint List ...");
                        Collections.reverse(wayPointList);
                        wayPointListIterator = wayPointList.listIterator();
                        logger.info("Iterating backward on the GPS Waypoint List ...");
                    }


                }
            }, TASK_DELAY_TIME, UPDATE_PERIOD);

        }catch (Exception e){
            logger.error("Error executing periodic resource value ! Msg: {}", e.getLocalizedMessage());
        }
    }

    @Override
    public GpsLocationDescriptor loadUpdatedValue() {
        return this.updatedGpsLocationDescriptor;
    }

    public static void main(String[] args) {
        GpsGpxSensorResource gpsGpxSensorResource = new GpsGpxSensorResource();

        logger.info("New {} Resource Created with Id: {} ! Updated Value: {}",
                gpsGpxSensorResource.getType(),
                gpsGpxSensorResource.getId(),
                gpsGpxSensorResource.loadUpdatedValue());

        gpsGpxSensorResource.addDataListener(new ResourceDataListener<GpsLocationDescriptor>() {
            @Override
            public void onDataChanged(SmartObjectResource<GpsLocationDescriptor> resource, GpsLocationDescriptor updatedValue) {
                if(resource != null && updatedValue != null)
                    logger.info("Device: {} -> New Value Received: {}", resource.getId(), updatedValue);
                else
                    logger.error("onDataChanged Callback -> Null Resource or Updated Value !");
            }
        });
    }
}
