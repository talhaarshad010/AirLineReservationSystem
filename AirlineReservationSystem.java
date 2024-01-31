import java.sql.*;
import java.util.*;

class Passenger {
    private String name;
    private String passportNumber;

    public Passenger(String name, String passportNumber) {
        this.name = name;
        this.passportNumber = passportNumber;
    }

    public String getName() {
        return name;
    }

    public String getPassportNumber() {
        return passportNumber;
    }
}

class Flight {
    private String flightNumber;
    private String destination;
    private int capacity;
    private int bookedSeats;

    public Flight(String flightNumber, String destination, int capacity) {
        this.flightNumber = flightNumber;
        this.destination = destination;
        this.capacity = capacity;
        this.bookedSeats = 0;
    }

    public String getFlightNumber() {
        return flightNumber;
    }

    public String getDestination() {
        return destination;
    }

    public int getCapacity() {
        return capacity;
    }

    public int getBookedSeats() {
        return bookedSeats;
    }

    public boolean bookSeat() {
        if (bookedSeats < capacity) {
            bookedSeats++;
            return true;
        } else {
            System.out.println("Sorry, the flight is fully booked.");
            return false;
        }
    }
}

class InternationalFlight extends Flight {
    private boolean hasImmigrationForm;

    public InternationalFlight(String flightNumber, String destination, int capacity, boolean hasImmigrationForm) {
        super(flightNumber, destination, capacity);
        this.hasImmigrationForm = hasImmigrationForm;
    }

    public boolean hasImmigrationForm() {
        return hasImmigrationForm;
    }
}

class Reservation {
    private Flight flight;
    private Passenger passenger;

    public Reservation(Flight flight, Passenger passenger) {
        this.flight = flight;
        this.passenger = passenger;
    }

    public void confirmReservation() {
        System.out.println("Reservation confirmed for " + passenger.getName() +
                " on flight " + flight.getFlightNumber() + " to " + flight.getDestination());
    }
}

class Admin {
    private String username;
    private String password;

    public Admin(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public boolean authenticate(String inputPassword) {
        return password.equals(inputPassword);
    }
}

public class AirlineReservationSystem {
    private static Scanner scanner = new Scanner(System.in);
    private static List<Admin> adminDatabase = new ArrayList<>();
    private static List<Flight> flights = new ArrayList<>();
    private static Admin currentAdmin;
    private static Connection connection;

    static {
        adminDatabase.add(new Admin("admin", "1234"));

        String url = "jdbc:mysql://localhost:3306/airlineresevationsystem";
        String username = "root";
        String password = "";

        try {
            connection = DriverManager.getConnection(url, username, password);
            System.out.println("Database connection established successfully.");
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Failed to establish database connection. Exiting.");
            System.exit(1);
        }
    }

    public static void main(String[] args) {
        while (true) {
            System.out.println("\nChoose an operation:");
            System.out.println("1. Admin Login");
            System.out.println("2. Add Passenger");
            System.out.println("3. Add Flight");
            System.out.println("4. Make Reservation");
            System.out.println("5. Display Flight Details from Database");
            System.out.println("6. Exit");

            int choice = 0;
            try {
                choice = scanner.nextInt();
                scanner.nextLine();
            } catch (InputMismatchException e) {
                System.out.println("Invalid input. Please enter a number.");
                scanner.nextLine();
                continue;
            }
            switch (choice) {
                  case 1:
                    adminLogin();
                    break;
                case 2:
                    addPassenger();
                    break;
               case 3:
                    addFlight();
                    break;
                case 4:
                    makeReservation();
                    break;
                case 5:
                    displayFlightDetailsFromDatabase();
                    break;
                case 6:
                    System.out.println("Exiting program. Goodbye!");
                    scanner.close();
                    System.exit(0);
                default:
                    System.out.println("Invalid choice. Please enter a number between 1 and 7.");
            }
        }
    }

    private static void adminLogin() {
        for (int attempts = 3; attempts > 0; attempts--) {
            System.out.print("Enter admin username: ");
            String username = scanner.nextLine();
            System.out.print("Enter admin password: ");
            String password = scanner.nextLine();

            for (Admin admin : adminDatabase) {
                if (admin.getUsername().equals(username) && admin.authenticate(password)) {
                    currentAdmin = admin;
                    System.out.println("Admin login successful. Welcome, " + admin.getUsername() + "!");
                    return;
                }
            }

            System.out.println("Invalid admin credentials. " + (attempts - 1) + " attempts remaining.");
        }
        System.out.println("Too many invalid attempts. Exiting.");
        System.exit(0);
    }
    private static void insertPassengerData(Passenger passenger) {
        String insertQuery = "INSERT INTO passengerstable (passenger_name,passport_no ) VALUES (?, ?)";

        try (PreparedStatement preparedStatement = connection.prepareStatement(insertQuery)) {
            preparedStatement.setString(1, passenger.getName());
            preparedStatement.setString(2, passenger.getPassportNumber());

            preparedStatement.executeUpdate();
            System.out.println("Passenger data inserted into the database successfully.");
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Failed to insert passenger data into the database.");
        }
    }
    private static void addPassenger() {
        if (isAdminAuthenticated()) {
            System.out.println("\nEnter passenger details:");
            System.out.print("Name: ");
            String name = scanner.nextLine();
            System.out.print("Passport Number: ");
            String passportNumber = scanner.nextLine();

            Passenger newPassenger = new Passenger(name, passportNumber);

            // Insert passenger data into the database
            insertPassengerData(newPassenger);

            System.out.println("Passenger added: " + newPassenger.getName());
        }
    }

    private static void addFlight() {
        if (isAdminAuthenticated()) {
            System.out.println("\nEnter flight details:");
            System.out.print("Flight Number: ");
            String flightNumber = scanner.nextLine();
            System.out.print("Destination: ");
            String destination = scanner.nextLine();
            System.out.print("Capacity: ");
            int capacity = 0;
            try {
                capacity = scanner.nextInt();
            } catch (InputMismatchException e) {
                System.out.println("Invalid input for capacity. Please enter a number.");
                scanner.nextLine();  // Consume the invalid input
                return;
            }

            // Create a new Flight object
            Flight newFlight = new Flight(flightNumber, destination, capacity);

            // Insert flight data into the database
            insertFlightData(newFlight);

            System.out.println("Flight added: " + newFlight.getFlightNumber());
        }
    }

    private static void insertFlightData(Flight flight) {
        String insertQuery = "INSERT INTO flights (flightNumber, destination, capacity, bookedSeats) VALUES (?, ?, ?, 0)";

        try (PreparedStatement preparedStatement = connection.prepareStatement(insertQuery)) {
            preparedStatement.setString(1, flight.getFlightNumber());
            preparedStatement.setString(2, flight.getDestination());
            preparedStatement.setInt(3, flight.getCapacity());

            preparedStatement.executeUpdate();
            System.out.println("Flight data inserted into the database successfully.");
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Failed to insert flight data into the database.");
        }
    }

///////////////////////////    this function doesn't work   ///////////////////////////
    private static void makeReservation() {
        System.out.println("\nEnter reservation details:");
        displayAvailableFlights();

        System.out.print("Enter the Flight Number to reserve a seat: ");
        String flightNumber = scanner.nextLine();

        Flight selectedFlight = findFlightByNumber(flightNumber);

        if (selectedFlight != null) {
            System.out.print("Enter the Passenger's name: ");
            String passengerName = scanner.nextLine();
            System.out.print("Enter the Passenger's passport number: ");
            String passengerPassportNumber = scanner.nextLine();

            Passenger passenger = new Passenger(passengerName, passengerPassportNumber);
            Reservation newReservation = new Reservation(selectedFlight, passenger);

            if (selectedFlight.bookSeat()) {
                newReservation.confirmReservation();
            } else {
                System.out.println("Reservation failed for " + passenger.getName() +
                        " on flight " + selectedFlight.getFlightNumber() + " to " + selectedFlight.getDestination());
            }
        } else {
            System.out.println("Invalid Flight Number. Reservation failed.");
        }
    }

    private static void displayFlightDetailsFromDatabase() {
        String selectQuery = "SELECT * FROM flights";

        try (PreparedStatement preparedStatement = connection.prepareStatement(selectQuery);
             ResultSet resultSet = preparedStatement.executeQuery()) {

            System.out.println("\nFlight Details from Database:");

            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String flightNumber = resultSet.getString("flightNumber");
                String destination = resultSet.getString("destination");
                int capacity = resultSet.getInt("capacity");
                int bookedSeats = resultSet.getInt("bookedSeats");
                System.out.println("S.No"+id);
                System.out.println("Flight Number: " + flightNumber);
                System.out.println("Destination: " + destination);
                System.out.println("Capacity: " + capacity);
                System.out.println("Booked Seats: " + bookedSeats);
                System.out.println();
            }

        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Failed to fetch flight data from the database.");
        }
    }

    private static void displayAvailableFlights() {
        System.out.println("\nAvailable Flights:");

        for (Flight flight : flights) {
            System.out.println("Flight Number: " + flight.getFlightNumber() +
                    ", Destination: " + flight.getDestination() +
                    ", Available Seats: " + (flight.getCapacity() - flight.getBookedSeats()));
        }
    }


    private static Flight findFlightByNumber(String flightNumber) {
        for (Flight flight : flights) {
            if (flight.getFlightNumber().equalsIgnoreCase(flightNumber)) {
                return flight;
            }
        }
        return null;
    }

    private static boolean isAdminAuthenticated() {
        if (currentAdmin != null) {
            return true;
        } else {
            System.out.println("Admin authentication required. Please login first.");
            adminLogin();
            return currentAdmin != null;
        }
    }
}
