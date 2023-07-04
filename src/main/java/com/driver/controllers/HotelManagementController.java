package com.driver.controllers;

import com.driver.model.Booking;
import com.driver.model.Facility;
import com.driver.model.Hotel;
import com.driver.model.User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

@RestController
@RequestMapping("/hotel")
public class HotelManagementController {

    private Map<String, Hotel> hotelMap = new HashMap<>();
    private Map<Integer, User> userMap = new HashMap<>();

    private Map<String, Booking> bookingMap = new HashMap<>();
    private Map<Integer, Integer> bookings = new HashMap<>();

    @PostMapping("/add-hotel")
    public String addHotel(@RequestBody Hotel hotel){
        //You need to add an hotel to the database
        //incase the hotelName is null or the hotel Object is null return an empty a FAILURE
        //Incase somebody is trying to add the duplicate hotelName return FAILURE
        //in all other cases return SUCCESS after successfully adding the hotel to the hotelDb.
        if(hotel == null || hotel.getHotelName() == null) {
            return "FAILURE";
        }
        if(hotelMap.containsKey(hotel.getHotelName())) {
            return  "FAILURE";
        }
        hotelMap.put(hotel.getHotelName(), hotel);
        return "SUCCESS";
    }

    @PostMapping("/add-user")
    public Integer addUser(@RequestBody User user){
        //You need to add a User Object to the database
        //Assume that user will always be a valid user and return the aadharCardNo of the user
        userMap.put(user.getaadharCardNo(), user);
        return user.getaadharCardNo();
    }

    @GetMapping("/get-hotel-with-most-facilities")
    public String getHotelWithMostFacilities(){
        //Out of all the hotels we have added so far, we need to find the hotelName with most no of facilities
        //Incase there is a tie return the lexicographically smaller hotelName
        //Incase there is not even a single hotel with atleast 1 facility return "" (empty string)
        String name = "";
        int maxF = 0;
        for(Hotel hotel : hotelMap.values()) {
            if(maxF < hotel.getFacilities().size()) {
                maxF = hotel.getFacilities().size();
                name = hotel.getHotelName();
            } else if (maxF == hotel.getFacilities().size()) {
                if(name.compareTo(hotel.getHotelName()) > 0) {
                    name = hotel.getHotelName();
                }
            }
        }
        return name;
    }

    @PostMapping("/book-a-room")
    public int bookARoom(@RequestBody Booking booking){
        //The booking object coming from postman will have all the attributes except bookingId and amountToBePaid;
        //Have bookingId as a random UUID generated String
        //save the booking Entity and keep the bookingId as a primary key
        //Calculate the total amount paid by the person based on no. of rooms booked and price of the room per night.
        //If there arent enough rooms available in the hotel that we are trying to book return -1
        //in other case return total amount paid

        String bookingId = UUID.randomUUID().toString();
        booking.setBookingId(bookingId);
        Hotel hotel = hotelMap.get(booking.getHotelName());
        if(booking.getNoOfRooms() > hotel.getAvailableRooms()) {
            return -1;
        }
        int totalPrice = booking.getNoOfRooms() * hotel.getPricePerNight();
        booking.setAmountToBePaid(totalPrice);
        hotel.setAvailableRooms(hotel.getAvailableRooms() - booking.getNoOfRooms());
        bookingMap.put(bookingId, booking);
        hotelMap.put(hotel.getHotelName(), hotel);

        int aadharCard = booking.getBookingAadharCard();
        Integer currentBookings = bookings.get(aadharCard);
        bookings.put(aadharCard, Objects.nonNull(currentBookings)?1+currentBookings:1);

        return totalPrice;
    }

    @GetMapping("/get-bookings-by-a-person/{aadharCard}")
    public int getBookings(@PathVariable("aadharCard")Integer aadharCard)
    {
        //In this function return the bookings done by a person
        return bookings.get(aadharCard);
    }

    @PutMapping("/update-facilities")
    public Hotel updateFacilities(List<Facility> newFacilities,String hotelName){

        //We are having a new facilites that a hotel is planning to bring.
        //If the hotel is already having that facility ignore that facility otherwise add that facility in the hotelDb
        //return the final updated List of facilities and also update that in your hotelDb
        //Note that newFacilities can also have duplicate facilities possible
        Hotel hotel = hotelMap.get(hotelName);
        for(Facility facility : newFacilities) {
            if (!hotel.getFacilities().contains(facility)) {
                hotel.getFacilities().add(facility);
            }
        }
        hotelMap.put(hotel.getHotelName(), hotel);
        return hotel;
    }
}