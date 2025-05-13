/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package Application;

import Client.CClient;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;

/**
 * Game Frame for Yahtzee
 *
 * @author kasim
 */
public class GameFrm extends javax.swing.JFrame {

    // Client connection
    private CClient client;

    // Game components
    private JPanel mainPanel;
    private JPanel dicePanel;
    private JPanel scorePanel;
    private JButton rollButton;
    private JLabel playerNameLabel;
    private JLabel turnInfoLabel;
    private JLabel rollsLeftLabel;

    // Dice components
    private ArrayList<DiceButton> diceButtons;
    private int[] diceValues;
    private boolean[] diceHeld;
    private int rollsLeft;

    // Score table components
    private JTable scoreTable;
    private ScoreTableModel scoreTableModel;

    // Game state
    private boolean isMyTurn;

    /**
     * Creates new form GameFrm
     */
    public GameFrm(CClient client) {
        this.client = client;
        this.isMyTurn = false;
        this.rollsLeft = 3;

        // Initialize dice values and held state
        diceValues = new int[5];
        diceHeld = new boolean[5];

        initCustomComponents();
        setupLayout();
        resetGame();

        // Set the client ID as window title
        setTitle("Yahtzee - Client " + client.getId());
    }
    
    public TableModel getScoreTableModel(){
        return this.scoreTableModel;
    }

    /**
     * Initialize custom components for the Yahtzee game
     */
    private void initCustomComponents() {
        // Main panel with green background
        mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(new Color(74, 37, 0)); // Dark brown background

        // Player info panel
        JPanel infoPanel = new JPanel(new BorderLayout());
        infoPanel.setOpaque(false);

        playerNameLabel = new JLabel("Client " + client.getId());
        playerNameLabel.setFont(new Font("Arial", Font.BOLD, 24));
        playerNameLabel.setForeground(Color.WHITE);

        turnInfoLabel = new JLabel("Waiting for opponent...");
        turnInfoLabel.setFont(new Font("Arial", Font.ITALIC, 16));
        turnInfoLabel.setForeground(Color.WHITE);

        rollsLeftLabel = new JLabel("Rolls left: " + rollsLeft);
        rollsLeftLabel.setFont(new Font("Arial", Font.BOLD, 14));
        rollsLeftLabel.setForeground(Color.WHITE);

        JPanel namePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        namePanel.setOpaque(false);
        namePanel.add(playerNameLabel);

        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        statusPanel.setOpaque(false);
        statusPanel.add(turnInfoLabel);
        statusPanel.add(Box.createHorizontalStrut(20));
        statusPanel.add(rollsLeftLabel);

        infoPanel.add(namePanel, BorderLayout.WEST);
        infoPanel.add(statusPanel, BorderLayout.EAST);

        // Dice panel
        dicePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        dicePanel.setOpaque(false);

        diceButtons = new ArrayList<>();

        // Create dice buttons
        for (int i = 0; i < 5; i++) {
            DiceButton diceButton = new DiceButton(i);
            diceButton.addActionListener(new DiceClickListener());
            diceButtons.add(diceButton);
            dicePanel.add(diceButton);
        }

        // Roll button
        rollButton = new JButton("Roll Dice");
        rollButton.setBackground(new Color(212, 175, 55));
        rollButton.setForeground(Color.WHITE);
        rollButton.setFont(new Font("Arial", Font.BOLD, 16));
        rollButton.setFocusPainted(false);
        rollButton.addActionListener(new RollDiceListener());
        rollButton.setEnabled(false); // Disabled until it's player's turn

        JPanel rollPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        rollPanel.setOpaque(false);
        rollPanel.add(rollButton);

        // Combine dice panel and roll button
        JPanel diceControlPanel = new JPanel(new BorderLayout());
        diceControlPanel.setOpaque(false);
        diceControlPanel.add(dicePanel, BorderLayout.CENTER);
        diceControlPanel.add(rollPanel, BorderLayout.SOUTH);

        // Score table
        createScoreTable();
        JScrollPane scoreScrollPane = new JScrollPane(scoreTable);
        scoreScrollPane.setPreferredSize(new Dimension(350, 400));

        // Add components to main panel
        mainPanel.add(infoPanel, BorderLayout.NORTH);
        mainPanel.add(diceControlPanel, BorderLayout.CENTER);
        mainPanel.add(scoreScrollPane, BorderLayout.EAST);

        // Frame settings
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setContentPane(mainPanel);
        setSize(800, 600);
        setLocationRelativeTo(null);
    }

    /**
     * Create the score table for Yahtzee
     */
    private void createScoreTable() {
        // Score table column names
        String[] columnNames = {"Category", "You", "Opponent"};

        // Score table data
        Object[][] data = {
            {"Ones", null, null},
            {"Twos", null, null},
            {"Threes", null, null},
            {"Fours", null, null},
            {"Fives", null, null},
            {"Sixes", null, null},
            {"Subtotal", 0, 0},
            {"Bonus (35)", 0, 0},
            {"Three of a Kind", null, null},
            {"Four of a Kind", null, null},
            {"Full House", null, null},
            {"Small Straight", null, null},
            {"Large Straight", null, null},
            {"Chance", null, null},
            {"Yahtzee", null, null},
            {"Total Score", 0, 0}
        };

        // Create table model
        scoreTableModel = new ScoreTableModel(data, columnNames);
        scoreTable = new JTable(scoreTableModel);

        // Table appearance
        scoreTable.setRowHeight(25);
        scoreTable.setShowGrid(true);
        scoreTable.setGridColor(Color.BLACK);
        scoreTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 14));
        scoreTable.setFont(new Font("Arial", Font.PLAIN, 14));
        scoreTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Category column cell renderer
        scoreTable.getColumnModel().getColumn(0).setCellRenderer(new CategoryCellRenderer());
        //scoreTable.getColumnModel().getColumn(0).setPreferredWidth(150);


        // Add click listener for selecting categories
        scoreTable.addMouseListener(new ScoreTableClickListener());
    }

    /**
     * Set up additional layout settings
     */
    private void setupLayout() {
        // Additional layout settings if needed
    }

    /**
     * Reset the game state
     */
    public void resetGame() {
        // Reset dice
        for (int i = 0; i < 5; i++) {
            diceValues[i] = 1;
            diceHeld[i] = false;
            diceButtons.get(i).setValue(1);
            diceButtons.get(i).setHeld(false);
        }

        // Reset score table
        scoreTableModel.resetScores();

        // Reset rolls
        rollsLeft = 3;
        updateRollsLeftLabel();
    }

    /**
     * Update the rolls left label
     */
    private void updateRollsLeftLabel() {
        rollsLeftLabel.setText("Rolls left: " + rollsLeft);
    }

    /**
     * Set the current turn
     *
     * @param isMyTurn true if it's this client's turn
     */
    public void setTurn(boolean isMyTurn) {
        this.isMyTurn = isMyTurn;
        if (isMyTurn) {
            turnInfoLabel.setText("Your turn");
            rollButton.setEnabled(true);
        } else {
            turnInfoLabel.setText("Opponent's turn");
            rollButton.setEnabled(false);
        }
    }

    /**
     * Roll the dice
     */
    private void rollDice() {
        if (!isMyTurn || rollsLeft <= 0) {
            return;
        }

        // Roll non-held dice
        for (int i = 0; i < 5; i++) {
            if (!diceHeld[i]) {
                diceValues[i] = (int) (Math.random() * 6) + 1;
                diceButtons.get(i).setValue(diceValues[i]);
            }
        }

        // Update rolls left
        rollsLeft--;
        updateRollsLeftLabel();

        // Disable roll button if no rolls left
        if (rollsLeft <= 0) {
            rollButton.setEnabled(false);
        }

        // Send dice values to server (implement if needed)
        try {
            // This is where you would send the dice roll info to the server
            // client.sendDiceRollMessage(diceValues);
        } catch (Exception ex) {
            Logger.getLogger(GameFrm.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Toggle dice hold state
     *
     * @param diceIndex index of the dice to toggle
     */
    private void toggleDiceHold(int diceIndex) {
        if (!isMyTurn || rollsLeft == 3) {
            return; // Can't hold dice before first roll
        }

        diceHeld[diceIndex] = !diceHeld[diceIndex];
        diceButtons.get(diceIndex).setHeld(diceHeld[diceIndex]);
    }

    /**
     * Select a category on the score table
     *
     * @param rowIndex the row index of the selected category
     */
    private void selectCategory(int rowIndex) {
        if (!isMyTurn || rollsLeft == 3) {
            return; // Must roll at least once before selecting category
        }

        // Locked categories and info rows (subtotal, bonus, total score)
        if (rowIndex == 6 || rowIndex == 7 || rowIndex == 15) {
            return;
        }

        // if there is a score in the cell, dont click
        if (scoreTableModel.getValueAt(rowIndex, 1) instanceof Integer) {
            return;
        }

        // 
        // Calculate score for selected category
        int score = calculateScore(rowIndex);

        // Set score in table
        scoreTableModel.setValueAt(score, rowIndex, 1);

        // Update total scores
        updateTotalScores();

        removeHintScores();

        // End turn
        endTurn();

        // Send score to server
        try {
            client.sendScoreMessage(rowIndex, score);
        } catch (IOException ex) {
            Logger.getLogger(GameFrm.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Update total scores in the score table
     */
    private void updateTotalScores() {
        // Upper section subtotal (1-6)
        int upperSectionSum = 0;
        for (int i = 0; i < 6; i++) {
            Object value = scoreTableModel.getValueAt(i, 1);
            // if cell is not null and not hint
            if (value != null && !(value instanceof String && ((String) value).contains("gray"))) {
                upperSectionSum += (Integer) value;
            }
        }
        scoreTableModel.setValueAt(upperSectionSum, 6, 1); // write upperSectionSum to Subtotal

        // Bonus check (35 points if upper section is 63 or more)
        int bonus = (upperSectionSum >= 63) ? 35 : 0;
        scoreTableModel.setValueAt(bonus, 7, 1);

        // Lower section total
        int lowerSectionSum = 0;
        for (int i = 8; i < 15; i++) {
            Object value = scoreTableModel.getValueAt(i, 1);
            if (value != null && !(value instanceof String && ((String) value).contains("gray"))) {
                lowerSectionSum += (Integer) value;
            }
        }

        // Total score
        int totalScore = upperSectionSum + bonus + lowerSectionSum;
        scoreTableModel.setValueAt(totalScore, 15, 1);
    }

    /**
     * Calculate score for selected category
     *
     * @param category category index
     * @return calculated score
     */
    private int calculateScore(int category) {
        int[] counts = new int[7]; // Count of each dice value (1-6)

        // Count dice values
        for (int i = 0; i < 5; i++) {
            counts[diceValues[i]]++;
        }

        // Sum of all dice
        int sum = 0;
        for (int i = 0; i < 5; i++) {
            sum += diceValues[i];
        }

        // Calculate score based on category
        switch (category) {
            case 0: // Ones
                return counts[1] * 1;
            case 1: // Twos
                return counts[2] * 2;
            case 2: // Threes
                return counts[3] * 3;
            case 3: // Fours
                return counts[4] * 4;
            case 4: // Fives
                return counts[5] * 5;
            case 5: // Sixes
                return counts[6] * 6;
            case 8: // Three of a Kind
                for (int i = 1; i <= 6; i++) {
                    if (counts[i] >= 3) {
                        return sum;
                    }
                }
                return 0;
            case 9: // Four of a Kind
                for (int i = 1; i <= 6; i++) {
                    if (counts[i] >= 4) {
                        return sum;
                    }
                }
                return 0;
            case 10: // Full House (3 of one kind + 2 of another)
                boolean hasThree = false;
                boolean hasTwo = false;
                for (int i = 1; i <= 6; i++) {
                    if (counts[i] == 3) {
                        hasThree = true;
                    }
                    if (counts[i] == 2) {
                        hasTwo = true;
                    }
                }
                return (hasThree && hasTwo) ? 25 : 0;
            case 11: // Small Straight (sequence of 4)
                if ((counts[1] >= 1 && counts[2] >= 1 && counts[3] >= 1 && counts[4] >= 1)
                        || (counts[2] >= 1 && counts[3] >= 1 && counts[4] >= 1 && counts[5] >= 1)
                        || (counts[3] >= 1 && counts[4] >= 1 && counts[5] >= 1 && counts[6] >= 1)) {
                    return 30;
                }
                return 0;
            case 12: // Large Straight (sequence of 5)
                if ((counts[1] >= 1 && counts[2] >= 1 && counts[3] >= 1 && counts[4] >= 1 && counts[5] >= 1)
                        || (counts[2] >= 1 && counts[3] >= 1 && counts[4] >= 1 && counts[5] >= 1 && counts[6] >= 1)) {
                    return 40;
                }
                return 0;
            case 13: // Chance
                return sum;
            case 14: // Yahtzee (5 of a kind)
                for (int i = 1; i <= 6; i++) {
                    if (counts[i] == 5) {
                        return 50;
                    }
                }
                return 0;
            default:
                return 0;
        }
    }

    private void showHintScores() {

        for (int i = 0; i < 15; i++) { // dont loop for 15

            if (scoreTableModel.getValueAt(i, 1) == null && i != 6 && i != 7) {
                // Calculate hint score
                int score = calculateScore(i);
                if (score == 0) {
                    continue;
                }
                // Write hint score in table
                String grayText = "<html><font color='gray'>" + score + "</font></html>";
                scoreTableModel.setValueAt(grayText, i, 1);
            }
        }
    }

    /**
     * Removes hint scores after selection
     */
    private void removeHintScores() {
        for (int i = 0; i < 15; i++) {
            Object val = scoreTableModel.getValueAt(i, 1);
            if (val instanceof String && ((String) val).contains("gray")) {
                scoreTableModel.setValueAt(null, i, 1);
            }
        }
    }

    /**
     * End the current turn
     */
    private void endTurn() {
        // Reset dice hold state
        for (int i = 0; i < 5; i++) {
            diceHeld[i] = false;
            diceButtons.get(i).setHeld(false);
        }

        // Reset rolls
        rollsLeft = 3;
        updateRollsLeftLabel();

        // Change turn
        setTurn(false);
    }

    /**
     * Event listener for dice buttons
     */
    private class DiceClickListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            DiceButton button = (DiceButton) e.getSource();
            toggleDiceHold(button.getDiceIndex());
        }
    }

    /**
     * Event listener for roll button
     */
    private class RollDiceListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            rollDice();
            removeHintScores();
            showHintScores();
        }
    }

    /**
     * Event listener for score table clicks
     */
    private class ScoreTableClickListener extends MouseAdapter {

        @Override
        public void mouseClicked(MouseEvent e) {
            int row = scoreTable.rowAtPoint(e.getPoint());
            int column = scoreTable.columnAtPoint(e.getPoint());

            if (row >= 0 && column == 1) { // column 1 is score column of the player 
                selectCategory(row);
            }
        }
    }

    /**
     * Custom button class for dice
     */
    private class DiceButton extends JButton {

        private int diceIndex;
        private int value;
        private boolean held;

        public DiceButton(int diceIndex) {
            this.diceIndex = diceIndex;
            this.value = 1;
            this.held = false;

            setPreferredSize(new Dimension(60, 60));
            updateAppearance();
        }

        public int getDiceIndex() {
            return diceIndex;
        }

        public void setValue(int value) {
            this.value = value;
            updateAppearance();
        }

        public void setHeld(boolean held) {
            this.held = held;
            updateAppearance();
        }

        private void updateAppearance() {
            // Update dice appearance
            setBackground(held ? new Color(200, 50, 50) : Color.WHITE);
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(Color.BLACK, 2),
                    BorderFactory.createEmptyBorder(5, 5, 5, 5)
            ));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            // Draw dice
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();
            int dotSize = Math.min(w, h) / 5;

            g2d.setColor(Color.BLACK);

            // Draw dots based on value
            switch (value) {
                case 1:
                    drawDot(g2d, w / 2, h / 2, dotSize);
                    break;
                case 2:
                    drawDot(g2d, w / 4, h / 4, dotSize);
                    drawDot(g2d, 3 * w / 4, 3 * h / 4, dotSize);
                    break;
                case 3:
                    drawDot(g2d, w / 4, h / 4, dotSize);
                    drawDot(g2d, w / 2, h / 2, dotSize);
                    drawDot(g2d, 3 * w / 4, 3 * h / 4, dotSize);
                    break;
                case 4:
                    drawDot(g2d, w / 4, h / 4, dotSize);
                    drawDot(g2d, 3 * w / 4, h / 4, dotSize);
                    drawDot(g2d, w / 4, 3 * h / 4, dotSize);
                    drawDot(g2d, 3 * w / 4, 3 * h / 4, dotSize);
                    break;
                case 5:
                    drawDot(g2d, w / 4, h / 4, dotSize);
                    drawDot(g2d, 3 * w / 4, h / 4, dotSize);
                    drawDot(g2d, w / 2, h / 2, dotSize);
                    drawDot(g2d, w / 4, 3 * h / 4, dotSize);
                    drawDot(g2d, 3 * w / 4, 3 * h / 4, dotSize);
                    break;
                case 6:
                    drawDot(g2d, w / 4, h / 4, dotSize);
                    drawDot(g2d, 3 * w / 4, h / 4, dotSize);
                    drawDot(g2d, w / 4, h / 2, dotSize);
                    drawDot(g2d, 3 * w / 4, h / 2, dotSize);
                    drawDot(g2d, w / 4, 3 * h / 4, dotSize);
                    drawDot(g2d, 3 * w / 4, 3 * h / 4, dotSize);
                    break;
            }
        }

        private void drawDot(Graphics2D g2d, int x, int y, int size) {
            g2d.fillOval(x - size / 2, y - size / 2, size, size);
        }
    }

    /**
     * Custom table model for score table
     */
    private class ScoreTableModel extends DefaultTableModel {

        public ScoreTableModel(Object[][] data, Object[] columnNames) {
            super(data, columnNames);
        }

        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }

        public void resetScores() {
            // Reset scores but keep categories
            for (int i = 0; i < getRowCount(); i++) {
                if (i != 6 && i != 7 && i != 15) {
                    setValueAt(null, i, 1);
                } else {
                    setValueAt(0, i, 1);
                }
            }
        }
    }

    /**
     * Custom cell renderer for category cells
     */
    private class CategoryCellRenderer extends DefaultTableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {

            Component c = super.getTableCellRendererComponent(
                    table, value, isSelected, hasFocus, row, column);

            // Make header rows bold
            if (row == 6 || row == 7 || row == 15) {
                c.setFont(c.getFont().deriveFont(Font.BOLD));
                c.setBackground(new Color(220, 220, 180));
            } else {
                c.setFont(c.getFont().deriveFont(Font.PLAIN));
                c.setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
            }

            return c;
        }
    }

    /**
     * Initialize the form - required by NetBeans
     *
     */
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 400, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 300, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(GameFrm.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(GameFrm.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(GameFrm.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(GameFrm.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {

            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}
