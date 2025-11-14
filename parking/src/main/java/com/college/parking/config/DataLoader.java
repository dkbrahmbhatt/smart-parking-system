package com.college.parking.config;

import com.college.parking.model.ParkingSlot;
import com.college.parking.repository.ParkingRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.util.Arrays;
import java.util.List;

@Configuration
public class DataLoader {

    @Bean
    public CommandLineRunner initDatabase(ParkingRepository repository) {
        return args -> {
            
            // --- NEW CHECK FOR PERSISTENCE ---
            if (repository.count() == 0) { // Only run if the table is empty
                
                List<ParkingSlot> initialSlots = Arrays.asList(
                    // Define all the parking slots and their base price here.
                    new ParkingSlot("A1", 20.0),
                    new ParkingSlot("A2", 20.0),
                    new ParkingSlot("A3", 20.0),
                    new ParkingSlot("A4", 20.0),
                    new ParkingSlot("B1", 35.0),
                    new ParkingSlot("B2", 35.0),
                    new ParkingSlot("C1", 20.0) // Reserved slot
                );

                repository.saveAll(initialSlots);

                System.out.println("PostgreSQL Database Initialized with " + repository.count() + " parking slots!");
            } else {
                System.out.println("Database already contains data. Skipping initialization.");
            }
            // ---------------------------------
        };
    }
}