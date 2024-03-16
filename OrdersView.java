import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class OrdersView extends JFrame {
    private int cust_id;
    private JTable table;
    private DefaultTableModel model;

    public OrdersView(int cust_id) {

        this.cust_id = cust_id;
        setTitle("Orders View");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        // Create table model
        model = new DefaultTableModel();
        table = new JTable(model);
        table.setEnabled(false);

        // Set table columns
        model.addColumn("Order ID");
        model.addColumn("Restaurant Name");
        model.addColumn("Item Name");
        model.addColumn("Item Price");
        model.addColumn("Quantity");
        model.addColumn("Amount");

        // Center align cells
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        table.setDefaultRenderer(Object.class, centerRenderer);

        // Set table size and make it fill the frame
        table.setPreferredScrollableViewportSize(new Dimension(800, 600));
        table.setFillsViewportHeight(true);

        // Add table to a scroll pane
        JScrollPane scrollPane = new JScrollPane(table);

        Font font = new Font("Arial", Font.PLAIN, 16); // Change the font and size as needed
        table.setFont(font);

        // Add scroll pane to the frame's content pane
        getContentPane().add(scrollPane, BorderLayout.CENTER);

        // Load orders into the table
        loadOrders();

        // Center the frame
        setLocationRelativeTo(null);
    }

    private void loadOrders() {
        try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/DELIVERY_MANAGEMENT", "root", "sql@123");
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT s.order_id, r.name AS restaurant_name, s.item_name, s.item_price, s.quantity, s.amount " +
                             "FROM suborder s " +
                             "INNER JOIN restaurants r ON s.restaurant_id = r.restaurant_id " +
                             "WHERE s.cust_id = ? " +
                             "ORDER BY s.order_id");
        ) {
            statement.setInt(1, cust_id);
            ResultSet resultSet = statement.executeQuery();

            int currentOrderId = -1;
            double orderTotal = 0.0;

            while (resultSet.next()) {
                int orderId = resultSet.getInt("order_id");
                String restaurantName = resultSet.getString("restaurant_name");
                String itemName = resultSet.getString("item_name");
                double itemPrice = resultSet.getDouble("item_price");
                int quantity = resultSet.getInt("quantity");
                double amount = resultSet.getDouble("amount");

                if (currentOrderId != orderId) {
                    // Add total amount for the previous order
                    if (currentOrderId != -1) {
                        model.addRow(new Object[]{"", "", "", "", "Total Amount", orderTotal});
                    }

                    // Add a blank row between orders
                    model.addRow(new Object[]{"", "", "", "", "", ""});

                    // Start a new order
                    currentOrderId = orderId;
                    orderTotal = 0.0;
                }

                // Add order details to the table
                model.addRow(new Object[]{orderId, restaurantName, itemName, itemPrice, quantity, amount});
                orderTotal += amount;
            }

            // Add total amount for the last order
            if (currentOrderId != -1) {
                model.addRow(new Object[]{"", "", "", "", "Total Amount", orderTotal});
            }

            resultSet.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            OrdersView orderView = new OrdersView(0);
            orderView.setVisible(true);
        });
    }
}
