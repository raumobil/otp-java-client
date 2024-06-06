package org.opentripplanner;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.opentripplanner.StationParameters.OSLO_EAST;
import static org.opentripplanner.StationParameters.OSLO_LUFTHAVN_ID;
import static org.opentripplanner.StationParameters.OSLO_LUFTHAVN_QUAY;
import static org.opentripplanner.StationParameters.OSLO_S_ID;
import static org.opentripplanner.StationParameters.OSLO_WEST;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.opentripplanner.client.OtpApiClient;
import org.opentripplanner.client.model.Coordinate;
import org.opentripplanner.client.model.FareProductUse;
import org.opentripplanner.client.model.RequestMode;
import org.opentripplanner.client.parameters.TripPlanParameters;
import org.opentripplanner.client.parameters.TripPlanParameters.SearchDirection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Execution(ExecutionMode.CONCURRENT)
public class IntegrationTest {
  public static final Logger LOG = LoggerFactory.getLogger(IntegrationTest.class);

  public static OtpApiClient client =
      new OtpApiClient(ZoneId.of("Europe/Oslo"), "https://otp2debug.entur.org");

  @Test
  public void plan() throws IOException {

    var result =
        client.plan(
            TripPlanParameters.builder()
                .withFrom(OSLO_WEST)
                .withTo(OSLO_EAST)
                .withTime(LocalDateTime.now())
                .withModes(RequestMode.TRANSIT)
                .withNumberOfItineraries(3)
                .build());

    LOG.info("Received {} itineraries", result.itineraries().size());

    assertNotNull(result.itineraries().get(0).legs().get(0).startTime());

    var leg = result.itineraries().get(0).legs().get(0);

    var transitLeg = result.transitItineraries().get(0).transitLegs().get(0);
    assertFalse(transitLeg.from().stop().isEmpty());
    assertFalse(transitLeg.to().stop().isEmpty());
    assertNotNull(transitLeg.from().stop().get().id());
    assertTrue(transitLeg.trip().headsign().isPresent());

    assertEquals(List.of(), leg.fareProducts());
  }

  @Test
  public void planPlaceToPlace() throws IOException {

    var result =
        client.plan(
            TripPlanParameters.builder()
                .withFrom(OSLO_LUFTHAVN_ID)
                .withTo(OSLO_S_ID)
                .withTime(LocalDateTime.now())
                .withModes(RequestMode.TRANSIT)
                .withNumberOfItineraries(3)
                .build());

    LOG.info("Received {} itineraries", result.itineraries().size());

    assertNotNull(result.itineraries().get(0).legs().get(0).startTime());

    var leg = result.itineraries().get(0).legs().get(0);

    var transitLeg = result.transitItineraries().get(0).transitLegs().get(0);
    assertFalse(transitLeg.from().stop().isEmpty());
    assertFalse(transitLeg.to().stop().isEmpty());
    assertNotNull(transitLeg.from().stop().get().id());

    assertEquals(List.of(), leg.fareProducts());
  }

  @Test
  public void planPlaceToPlaceWithSearchWindow() throws IOException {
    var result =
        client.plan(
            TripPlanParameters.builder()
                .withFrom(OSLO_LUFTHAVN_ID)
                .withTo(OSLO_S_ID)
                .withTime(LocalDateTime.now())
                .withModes(RequestMode.TRANSIT)
                .withNumberOfItineraries(1)
                .withSearchWindow(Duration.ofDays(1))
                .build());

    LOG.info("Received {} itineraries", result.itineraries().size());
    assertEquals(1, result.itineraries().size());

    assertNotNull(result.itineraries().get(0).legs().get(0).startTime());

    var leg = result.itineraries().get(0).legs().get(0);

    var transitLeg = result.transitItineraries().get(0).transitLegs().get(0);
    assertFalse(transitLeg.from().stop().isEmpty());
    assertFalse(transitLeg.to().stop().isEmpty());
    assertNotNull(transitLeg.from().stop().get().id());

    assertEquals(List.of(), leg.fareProducts());
  }

  @Test
  public void arriveByPlan() throws IOException {

    var result =
        client.plan(
            TripPlanParameters.builder()
                .withFrom(OSLO_WEST)
                .withTo(OSLO_EAST)
                .withTime(LocalDateTime.now())
                .withModes(RequestMode.TRANSIT)
                .withSearchDirection(SearchDirection.ARRIVE_BY)
                .build());

    LOG.info("Received {} itineraries", result.itineraries().size());

    assertNotNull(result.itineraries().get(0).legs().get(0).startTime());
  }

  @Test
  public void bikePlan() throws IOException {

    var result =
        client.plan(
            TripPlanParameters.builder()
                .withFrom(OSLO_WEST)
                .withTo(OSLO_EAST)
                .withTime(LocalDateTime.now())
                .withModes(Set.of(RequestMode.BICYCLE))
                .build());

    LOG.info("Received {} itineraries", result.itineraries().size());

    assertNotNull(result.itineraries().get(0).legs().get(0).startTime());
  }

  @Test
  public void bikeAndParkPlan() throws IOException {

    var result =
        client.plan(
            TripPlanParameters.builder()
                .withFrom(OSLO_WEST)
                .withTo(OSLO_EAST)
                .withTime(LocalDateTime.now())
                .withModes(Set.of(RequestMode.BICYCLE_PARK, RequestMode.TRANSIT))
                .build());

    LOG.info("Received {} itineraries", result.itineraries().size());

    assertNotNull(result.itineraries().get(0).legs().get(0).startTime());
  }

  @Test
  public void carPlan() throws IOException {

    var result =
        client.plan(
            TripPlanParameters.builder()
                .withFrom(OSLO_WEST)
                .withTo(OSLO_EAST)
                .withTime(LocalDateTime.now())
                .withModes(Set.of(RequestMode.CAR))
                .build());

    LOG.info("Received {} itineraries", result.itineraries().size());

    assertNotNull(result.itineraries().get(0).legs().get(0).startTime());
  }

  @Test
  public void carAndParkPlan() throws IOException {

    var result =
        client.plan(
            TripPlanParameters.builder()
                .withFrom(OSLO_WEST)
                .withTo(OSLO_EAST)
                .withTime(LocalDateTime.now())
                .withModes(Set.of(RequestMode.CAR_PARK, RequestMode.TRANSIT))
                .build());

    LOG.info("Received {} itineraries", result.itineraries().size());

    assertNotNull(result.itineraries().get(0).legs().get(0).startTime());
  }

  @Test
  public void rentalStations() throws IOException {

    var result = client.vehicleRentalStations();

    LOG.info("Received {} rental stations", result.size());

    assertFalse(result.isEmpty());
  }

  @Test
  public void routes() throws IOException {
    var routes = client.routes();
    LOG.info("Received {} routes", routes.size());

    assertFalse(routes.isEmpty());
    routes.forEach(
        r -> {
          assertFalse(r.name().isEmpty(), "Route %s has no name.".formatted(r));
          assertFalse(r.agency().name().isEmpty());
        });
  }

  @Test
  public void patterns() throws IOException {

    var result = client.patterns();

    LOG.info("Received {} patterns", result.size());

    assertFalse(result.isEmpty());

    result.forEach(
        pattern -> {
          assertNotNull(pattern.name());
          assertNotNull(pattern.vehiclePositions());
        });
  }

  @Test
  public void stop() throws IOException {

    var result = client.stop(OSLO_LUFTHAVN_QUAY);

    LOG.info("Received stop");

    assertNotNull(result);
    assertNotNull(result.name());
    assertNotNull(result.id());
  }

  @Disabled
  @Test
  public void seattleFares() throws IOException {

    var southSeattle = new Coordinate(47.5634, -122.3155);
    var northSeattle = new Coordinate(47.6225, -122.3312);
    var client = new OtpApiClient(ZoneId.of("America/Los_Angeles"), "http://localhost:8080");

    var result =
        client.plan(
            TripPlanParameters.builder()
                .withFrom(southSeattle)
                .withTo(northSeattle)
                .withTime(LocalDateTime.now())
                .withModes(Set.of(RequestMode.TRANSIT))
                .build());

    var itin = result.itineraries().get(1);

    var transitLeg = itin.legs().get(1);

    var product =
        transitLeg.fareProducts().stream()
            .map(FareProductUse::product)
            .filter(fp -> fp.id().equals("orca:farePayment"))
            .findFirst()
            .get();

    assertNotNull(product.price());
    assertNotNull(product.price().currency());
    assertNotNull(product.price().amount());
    assertNotNull(product.medium().get().id());
    assertNotNull(product.medium().get().name());
    assertNotNull(product.riderCategory().get().id());
    assertNotNull(product.riderCategory().get().name());
  }
}
