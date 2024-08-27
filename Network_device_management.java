package network_man;

import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.util.Scanner;

public class nethish {
    private static final String url = "jdbc:mysql://localhost:3306/network_db";
    private static final String username = "root";
    private static final String password = "@Nethish1234";

    public static void main(String[] args) {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.out.println("JDBC Driver not found.");
            e.printStackTrace();
            return;
        }

        try (Connection connection = DriverManager.getConnection(url, username, password);
             Scanner scanner = new Scanner(System.in)) {

            // Create the devices table if it doesn't exist
            createTableIfNotExists(connection);

            while (true) {
                System.out.println();
                System.out.println("NETWORK DEVICE MANAGEMENT SYSTEM");
                System.out.println("1. Add a Device");
                System.out.println("2. View Devices");
                System.out.println("3. Get Device Details by ID");
                System.out.println("4. Update Device Details");
                System.out.println("5. Delete Device");
                System.out.println("0. Exit");
                System.out.print("Choose an option: ");
                int choice = scanner.nextInt();
                scanner.nextLine(); // Consume the newline character

                switch (choice) {
                    case 1:
                        addDevice(connection, scanner);
                        break;
                    case 2:
                        viewDevices(connection);
                        break;
                    case 3:
                        getDeviceDetailsById(connection, scanner);
                        break;
                    case 4:
                        updateDeviceDetails(connection, scanner);
                        break;
                    case 5:
                        deleteDevice(connection, scanner);
                        break;
                    case 0:
                        exit();
                        return;
                    default:
                        System.out.println("Invalid choice. Try again.");
                }
            }
        } catch (SQLException e) {
            System.out.println("Database connection error.");
            e.printStackTrace();
        } catch (InterruptedException e) {
            System.out.println("Error during exit process.");
            e.printStackTrace();
        }
    }

    private static void createTableIfNotExists(Connection connection) {
        String createTableSQL = "CREATE TABLE IF NOT EXISTS devices (" +
                "device_id INT AUTO_INCREMENT PRIMARY KEY," +
                "device_name VARCHAR(100) NOT NULL," +
                "ip_address VARCHAR(15) NOT NULL," +
                "device_type VARCHAR(50) NOT NULL," +
                "added_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                ")";

        try (Statement statement = connection.createStatement()) {
            statement.execute(createTableSQL);
            System.out.println("Table 'devices' is ready.");
        } catch (SQLException e) {
            System.out.println("Error creating table.");
            e.printStackTrace();
        }
    }

    private static void addDevice(Connection connection, Scanner scanner) {
        System.out.print("Enter device name: ");
        String deviceName = scanner.nextLine();
        System.out.print("Enter IP address: ");
        String ipAddress = scanner.nextLine();
        System.out.print("Enter device type: ");
        String deviceType = scanner.nextLine();

        String sql = "INSERT INTO devices (device_name, ip_address, device_type) VALUES (?, ?, ?)";

        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, deviceName);
            preparedStatement.setString(2, ipAddress);
            preparedStatement.setString(3, deviceType);
            int affectedRows = preparedStatement.executeUpdate();

            if (affectedRows > 0) {
                System.out.println("Device added successfully!");
            } else {
                System.out.println("Failed to add device.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void viewDevices(Connection connection) {
        String sql = "SELECT device_id, device_name, ip_address, device_type, added_date FROM devices";

        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {

            System.out.println("Current Devices:");
            System.out.println("+------------+---------------+---------------+----------------+-------------------------+");
            System.out.println("| Device ID  | Device Name   | IP Address    | Device Type    | Added Date              |");
            System.out.println("+------------+---------------+---------------+----------------+-------------------------+");

            while (resultSet.next()) {
                int deviceId = resultSet.getInt("device_id");
                String deviceName = resultSet.getString("device_name");
                String ipAddress = resultSet.getString("ip_address");
                String deviceType = resultSet.getString("device_type");
                String addedDate = resultSet.getTimestamp("added_date").toString();

                System.out.printf("| %-10d | %-13s | %-13s | %-14s | %-23s |\n",
                        deviceId, deviceName, ipAddress, deviceType, addedDate);
            }

            System.out.println("+------------+---------------+---------------+----------------+-------------------------+");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void getDeviceDetailsById(Connection connection, Scanner scanner) {
        System.out.print("Enter device ID: ");
        int deviceId = scanner.nextInt();

        String sql = "SELECT * FROM devices WHERE device_id = ?";

        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, deviceId);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    String deviceName = resultSet.getString("device_name");
                    String ipAddress = resultSet.getString("ip_address");
                    String deviceType = resultSet.getString("device_type");
                    String addedDate = resultSet.getTimestamp("added_date").toString();

                    System.out.println("Device Details:");
                    System.out.println("Device Name: " + deviceName);
                    System.out.println("IP Address: " + ipAddress);
                    System.out.println("Device Type: " + deviceType);
                    System.out.println("Added Date: " + addedDate);
                } else {
                    System.out.println("Device not found.");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void updateDeviceDetails(Connection connection, Scanner scanner) {
        System.out.print("Enter device ID to update: ");
        int deviceId = scanner.nextInt();
        scanner.nextLine(); // Consume the newline character

        if (!deviceExists(connection, deviceId)) {
            System.out.println("Device not found for the given ID.");
            return;
        }

        System.out.print("Enter new device name: ");
        String newDeviceName = scanner.nextLine();
        System.out.print("Enter new IP address: ");
        String newIpAddress = scanner.nextLine();
        System.out.print("Enter new device type: ");
        String newDeviceType = scanner.nextLine();

        String sql = "UPDATE devices SET device_name = ?, ip_address = ?, device_type = ? WHERE device_id = ?";

        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, newDeviceName);
            preparedStatement.setString(2, newIpAddress);
            preparedStatement.setString(3, newDeviceType);
            preparedStatement.setInt(4, deviceId);

            int affectedRows = preparedStatement.executeUpdate();

            if (affectedRows > 0) {
                System.out.println("Device updated successfully!");
            } else {
                System.out.println("Device update failed.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void deleteDevice(Connection connection, Scanner scanner) {
        System.out.print("Enter device ID to delete: ");
        int deviceId = scanner.nextInt();

        if (!deviceExists(connection, deviceId)) {
            System.out.println("Device not found for the given ID.");
            return;
        }

        String sql = "DELETE FROM devices WHERE device_id = ?";

        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, deviceId);

            int affectedRows = preparedStatement.executeUpdate();

            if (affectedRows > 0) {
                System.out.println("Device deleted successfully!");
            } else {
                System.out.println("Device deletion failed.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static boolean deviceExists(Connection connection, int deviceId) {
        String sql = "SELECT device_id FROM devices WHERE device_id = ?";

        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, deviceId);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                return resultSet.next(); // If there's a result, the device exists
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false; // Handle database errors as needed
        }
    }

    public static void exit() throws InterruptedException {
        System.out.print("Exiting System");
        int i = 5;
        while (i != 0) {
            System.out.print(".");
            Thread.sleep(1000);
            i--;
        }
        System.out.println();
        System.out.println("Thank you for using the Network Device Management System!!!");
    }
}
