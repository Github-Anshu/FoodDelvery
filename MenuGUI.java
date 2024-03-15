import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.time.LocalDate;

public class MenuGUI extends JFrame {
    private JPanel menuPanel;
    private JButton orderButton;
    private int cust_id;

    public MenuGUI(int id) {
        this.cust_id = id;
        setTitle("Menu Items by Restaurant");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(600, 400);
        setLocationRelativeTo(null);

        menuPanel = new JPanel();
        menuPanel.setLayout(new BoxLayout(menuPanel, BoxLayout.Y_AXIS));
        JScrollPane scrollPane = new JScrollPane(menuPanel);

        orderButton = new JButton("Place Order");
        orderButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                placeOrder();
            }
        });

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(orderButton);

        setLayout(new BorderLayout());
        add(scrollPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.EAST);

        displayMenuItems();
    }

    private void displayMenuItems() {
        try (
                Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/DELIVERY_MANAGEMENT", "root", "sql@123");
                PreparedStatement restaurantStatement = connection.prepareStatement(
                        "SELECT DISTINCT r.name AS restaurant_name, r.menu_id FROM restaurants r ORDER BY r.name");
                ResultSet restaurantResultSet = restaurantStatement.executeQuery()
        ) {
            while (restaurantResultSet.next()) {
                String restaurantName = restaurantResultSet.getString("restaurant_name");
                int menuId = restaurantResultSet.getInt("menu_id");

                JPanel restaurantPanel = new JPanel();
                restaurantPanel.setName(restaurantName); // Set the name property to the restaurant name
                restaurantPanel.setLayout(new BoxLayout(restaurantPanel, BoxLayout.Y_AXIS));
                restaurantPanel.setBorder(BorderFactory.createTitledBorder(restaurantName));

                PreparedStatement menuStatement = connection.prepareStatement(
                        "SELECT m.item_name, m.item_price " +
                                "FROM menu m " +
                                "JOIN restaurants r ON m.menu_id = r.menu_id " +
                                "WHERE r.menu_id = ? " +
                                "ORDER BY m.item_name");
                menuStatement.setInt(1, menuId);
                ResultSet menuResultSet = menuStatement.executeQuery();

                while (menuResultSet.next()) {
                    String itemName = menuResultSet.getString("item_name");
                    double itemPrice = menuResultSet.getDouble("item_price");

                    JPanel itemPanel = new JPanel(new BorderLayout());
                    itemPanel.add(new JLabel(itemName + " - $" + itemPrice), BorderLayout.WEST);

                    JPanel quantityPanel = new JPanel();
                    quantityPanel.setLayout(new BoxLayout(quantityPanel, BoxLayout.X_AXIS));

                    JButton minusButton = new JButton("-");
                    JLabel quantityLabel = new JLabel("0");
                    JButton plusButton = new JButton("+");

                    minusButton.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            int quantity = Integer.parseInt(quantityLabel.getText());
                            if (quantity > 0) {
                                quantityLabel.setText(String.valueOf(quantity - 1));
                            }
                        }
                    });

                    plusButton.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            int quantity = Integer.parseInt(quantityLabel.getText());
                            quantityLabel.setText(String.valueOf(quantity + 1));
                        }
                    });
                    // Add the quantity label to the quantity panel before the buttons
                    quantityPanel.add(quantityLabel);
                    quantityPanel.add(minusButton);
                    quantityPanel.add(plusButton);

                    itemPanel.add(quantityPanel, BorderLayout.EAST);
                    restaurantPanel.add(itemPanel);
                }
                menuPanel.add(restaurantPanel);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void placeOrder() {
        // Get current date
        LocalDate currentDate = LocalDate.now();
        // Print the date
        System.out.println("Order Date: " + currentDate);
        System.out.println("Customer ID: " + this.cust_id);

        StringBuilder orderDetails = new StringBuilder("Order Details:\n");
        double totalAmount = 0.0; // Initialize total amount
        String currentRestaurant = null;
        try (
                Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/DELIVERY_MANAGEMENT", "root", "sql@123");
                PreparedStatement restaurantStatement = connection.prepareStatement(
                        "SELECT r.restaurant_id, r.name AS restaurant_name " +
                                "FROM restaurants r " +
                                "ORDER BY r.name");
                ResultSet restaurantResultSet = restaurantStatement.executeQuery()
        ) {
            while (restaurantResultSet.next()) {
                int restaurantId = restaurantResultSet.getInt("restaurant_id");
                String restaurantName = restaurantResultSet.getString("restaurant_name");
                boolean itemsFound = false; // Flag to check if items are available for this restaurant
                Component[] restaurantPanels = menuPanel.getComponents();
                for (Component restaurantPanel : restaurantPanels) {
                    if (restaurantPanel instanceof JPanel && restaurantName.equals(((JPanel) restaurantPanel).getName())) {
                        Component[] itemPanels = ((JPanel) restaurantPanel).getComponents();
                        for (Component itemPanel : itemPanels) {
                            if (itemPanel instanceof JPanel) {
                                JLabel itemNameLabel = (JLabel) ((JPanel) itemPanel).getComponent(0);
                                JPanel quantityPanel = (JPanel) ((JPanel) itemPanel).getComponent(1);
                                JLabel quantityLabel = (JLabel) quantityPanel.getComponent(0);
                                int quantity = Integer.parseInt(quantityLabel.getText());
                                if (quantity > 0) {
                                    if (!itemsFound) {
                                        // Append restaurant details only if items are available for this restaurant
                                        currentRestaurant = restaurantName;
                                        orderDetails.append("Restaurant ID: ").append(restaurantId).append("\n");
                                        orderDetails.append("Restaurant Name: ").append(restaurantName).append("\n");
                                        itemsFound = true;
                                    }
                                    double price = getPriceFromLabel(itemNameLabel);
                                    double amount = price * quantity;
                                    totalAmount += amount; // Update total amount
                                    orderDetails.append("Item: ").append(itemNameLabel.getText()).append("\n");
                                    orderDetails.append("Quantity: ").append(quantity).append("\n");
                                    orderDetails.append("Amount: ").append(amount).append("\n\n");
                                    // Reset quantity to zero
                                    quantityLabel.setText("0");
                                }
                            }
                        }
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        orderDetails.append("Total Amount: ").append(totalAmount); // Append total amount
        System.out.println(orderDetails.toString());
    }

    // Helper method to extract price from item name label
    private double getPriceFromLabel(JLabel itemNameLabel) {
        String text = itemNameLabel.getText();
        String priceString = text.substring(text.lastIndexOf("$") + 1).trim();
        return Double.parseDouble(priceString);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            MenuGUI menuGUI = new MenuGUI(0);
            menuGUI.setVisible(true);
        });
    }
}
