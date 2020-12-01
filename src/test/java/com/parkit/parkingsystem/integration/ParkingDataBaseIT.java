package com.parkit.parkingsystem.integration;

import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.integration.config.DataBaseTestConfig;
import com.parkit.parkingsystem.integration.service.DataBasePrepareService;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.ParkingService;
import com.parkit.parkingsystem.util.InputReaderUtil;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

import static com.parkit.parkingsystem.service.ParkingService.fareCalculatorService;
import static junit.framework.Assert.*;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ParkingDataBaseIT {

    private static DataBaseTestConfig dataBaseTestConfig = new DataBaseTestConfig();
    private static ParkingSpotDAO parkingSpotDAO;
    private static TicketDAO ticketDAO;
    private static DataBasePrepareService dataBasePrepareService;

    @Mock
    private static InputReaderUtil inputReaderUtil;

    @BeforeAll
    private static void setUp() throws Exception{
        parkingSpotDAO = new ParkingSpotDAO();
        parkingSpotDAO.dataBaseConfig = dataBaseTestConfig;
        ticketDAO = new TicketDAO();
        ticketDAO.dataBaseConfig = dataBaseTestConfig;
        dataBasePrepareService = new DataBasePrepareService();
    }

    @BeforeEach
    private void setUpPerTest() throws Exception {
        when(inputReaderUtil.readSelection()).thenReturn(1);
        when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
        dataBasePrepareService.clearDataBaseEntries();
    }

    @AfterEach
    void tearDown() {

    }

    /*@Test
    public void testParkingACar(){
        assert(ticketDAO.getTicket("ABCDEF") == null);
        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
        parkingService.processIncomingVehicle();
        //TODO: check that a ticket is actually saved in DB and Parking table is updated with availability
        assert(ticketDAO.getTicket("ABCDEF") != null);
    }*/

    @Test
    public void testParkingLotExit(){
        testParkingACar();
        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
        parkingService.processExitingVehicle();
        //TODO: check that the fare generated and out time are populated correctly in the database
    }

    @Test
    public void testParkingACar(){
        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);


        parkingService.processIncomingVehicle();
        try {
            Ticket ticket = ticketDAO.getTicket("ABCDEF");
            //check that a ticket is actually saved in DB and Parking table is updated with availability
            assertNotNull(ticket);
            assertNotEquals(1,parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR));
            assertEquals(2,parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR));
            //check that a ticket is not available for discount fare
            //assertFalse(ticket.isAvailableDiscount());
            //ticket.setInTime(LocalDateTime.now(ZoneId.systemDefault()).minusHours(1));
            //ticketDAO.updateTicketForIntegrationTest(ticket);
            parkingService.processExitingVehicle();

            // same vehicle new coming
            parkingService.processIncomingVehicle();
            Ticket ticket2 = ticketDAO.getTicket("ABCDEF");
            // check that ticket is available for discount fare
            //assertTrue(ticket2.isAvailableDiscount());
            // exit 1 hour later
            ZoneId zone = ZoneId.of("Europe/Berlin");
            Date out = Date.from(LocalDateTime.now(ZoneId.systemDefault()).plusHours(1).toInstant(zone.getRules().getOffset(LocalDateTime.now())));
            ticket2.setOutTime(out);
            ticketDAO.updateTicket(ticket2);
            // calcul fare
            fareCalculatorService.calculateFare(ticket2);
            ticketDAO.updateTicket(ticket2);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
