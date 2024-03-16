import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalTime;
import java.sql.*;

public class NewUserGUI extends JFrame {
    private JTextField phoneField;
    private JTextField firstNameField;
    private JTextField lastNameField;
    private JPasswordField desiredPasswordField;

    public NewUserGUI() {
        setTitle("New User Registration");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(300, 200);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel(new GridLayout(5, 2));

        JLabel phoneLabel = new JLabel("Phone Number:");
        phoneField = new JTextField();
        JLabel firstNameLabel = new JLabel("First Name:");
        firstNameField = new JTextField();
        JLabel lastNameLabel = new JLabel("Last Name:");
        lastNameField = new JTextField();
        JLabel desiredPasswordLabel = new JLabel("Desired Password:");
        desiredPasswordField = new JPasswordField();
        JButton submitButton = new JButton("Submit");

        panel.add(phoneLabel);
        panel.add(phoneField);
        panel.add(firstNameLabel);
        panel.add(firstNameField);
        panel.add(lastNameLabel);
        panel.add(lastNameField);
        panel.add(desiredPasswordLabel);
        panel.add(desiredPasswordField);
        panel.add(new JLabel()); // Empty space for alignment
        panel.add(submitButton);

        add(panel);


                    submitButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    String phone = phoneField.getText();
                    String firstName = firstNameField.getText();
                    String lastName = lastNameField.getText();
                    String desiredPassword = new String(desiredPasswordField.getPassword());

                    int userId = generateId();
                    System.out.println("User ID: " + userId);
                    System.out.println("Phone Number: " + phone);
                    System.out.println("First Name: " + firstName);
                    System.out.println("Last Name: " + lastName);
                    System.out.println("Desired Password: " + desiredPassword);

                    // Insert user details into the database
                    insertUserIntoDatabase(userId, phone, firstName, lastName, desiredPassword);

                    dispose(); // Close the window
                }
            });


    }

    private void insertUserIntoDatabase(int userId, String phone, String firstName, String lastName, String password) {
        String query = "INSERT INTO customers (cust_id, phone, first_name, last_name, password) VALUES (?, ?, ?, ?, ?)";
        try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/DELIVERY_MANAGEMENT", "root", "sql@123");
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setInt(1, userId);
            preparedStatement.setString(2, phone);
            preparedStatement.setString(3, firstName);
            preparedStatement.setString(4, lastName);
            preparedStatement.setString(5, password);
            preparedStatement.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }


    private int generateId() {
        // Implement your logic to generate a unique ID
        LocalTime time = LocalTime.now();
        int seconds = time.getSecond();
        int minutes = time.getMinute();
        int hours = time.getHour();
        return hours * 10000 + minutes * 100 + seconds;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            NewUserGUI newUserGUI = new NewUserGUI();
            newUserGUI.setVisible(true);
        });
    }
}
