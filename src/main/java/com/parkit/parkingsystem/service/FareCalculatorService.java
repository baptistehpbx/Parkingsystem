package com.parkit.parkingsystem.service;

import com.parkit.parkingsystem.model.Ticket;

import java.util.concurrent.TimeUnit;

import static com.parkit.parkingsystem.constants.Fare.BIKE_RATE_PER_HOUR;
import static com.parkit.parkingsystem.constants.Fare.CAR_RATE_PER_HOUR;


public class FareCalculatorService {

    public void calculateFare(Ticket ticket){
        if( (ticket.getOutTime() == null) || (ticket.getOutTime().before(ticket.getInTime())) ){
            throw new IllegalArgumentException("Out time provided is incorrect:"+ticket.getOutTime().toString());
        }

        long diffInMillies = Math.abs(ticket.getOutTime().getTime() - ticket.getInTime().getTime());
        long nbFullHour = TimeUnit.HOURS.convert(diffInMillies, TimeUnit.MILLISECONDS);
        long nbMinutes = TimeUnit.MINUTES.convert(diffInMillies, TimeUnit.MILLISECONDS) - nbFullHour * 60;

       double pricePerHour = getPricePerHour(ticket);
       double fullHoursPrice = pricePerHour * nbFullHour;

        double pricePerMinute = pricePerHour / 60;
        double minutesPrice = pricePerMinute * nbMinutes - (pricePerHour/2);
        double totalPrice = fullHoursPrice + minutesPrice ;

        ticket.setPrice(totalPrice);
    }

    private double getPricePerHour(Ticket ticket){

        switch (ticket.getParkingSpot().getParkingType()){
            case CAR: {
                return calculDiscount(CAR_RATE_PER_HOUR, ticket);

            }
            case BIKE: {
               return calculDiscount(BIKE_RATE_PER_HOUR,ticket);

            }
            default: throw new IllegalArgumentException("Unknown Parking Type");
        }

    }

    public double calculDiscount(double price, Ticket ticket){
        if (ticket.isAvailableDiscount()){
            double discount = (price * 5) / 100;
            price = price - discount;
        }
        ticket.setPrice(price);
        return price;
    }


    }
