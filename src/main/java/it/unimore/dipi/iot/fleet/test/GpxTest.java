package it.unimore.dipi.iot.fleet.test;

import io.jenetics.jpx.GPX;
import io.jenetics.jpx.Track;
import io.jenetics.jpx.TrackSegment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Marco Picone, Ph.D. - picone.m@gmail.com
 * @project vehicle-emulator
 * @created 29/10/2020 - 22:27
 */
public class GpxTest {

    private static final Logger logger = LoggerFactory.getLogger(GpxTest.class);

    public static void main(String[] args) {

        try{

            GPX.read("tracks/demo.gpx").tracks()
                    .flatMap(Track::segments)
                    .flatMap(TrackSegment::points)
                    .forEach(wayPoint -> {
                        logger.info("Lat: {} - Lng: {} - Time: {}",
                                wayPoint.getLatitude(),
                                wayPoint.getLongitude(),
                                wayPoint.getTime().get());
                    });

        }catch (Exception e){
            e.printStackTrace();
        }

    }

}
