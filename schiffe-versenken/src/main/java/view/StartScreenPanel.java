package view;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

public class StartScreenPanel extends JPanel {

    private final JTextField nameTextField;
    private final JButton startButton;

    public StartScreenPanel() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(40, 40, 40, 40));

        JLabel titleLabel = new JLabel("Schiffeversenken", SwingConstants.CENTER);
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 36f));

        nameTextField = new JTextField();
        nameTextField.setPreferredSize(new Dimension(260, 36));
        nameTextField.setHorizontalAlignment(SwingConstants.CENTER);
        nameTextField.setToolTipText("Spielername eingeben");

        startButton = new JButton("Spiel starten");
        startButton.setPreferredSize(new Dimension(260, 40));
        startButton.setFont(startButton.getFont().deriveFont(Font.BOLD, 15f));

        JPanel centerPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(8, 0, 8, 0);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        centerPanel.add(nameTextField, gbc);

        gbc.gridy = 1;
        centerPanel.add(startButton, gbc);

        add(titleLabel, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);
    }

    public String getPlayerName() {
        return nameTextField.getText().trim();
    }

    public String getTextFieldValue() {
        return nameTextField.getText().trim();
    }

    public void setStartAction(Runnable action) {
        startButton.addActionListener(e -> action.run());
        nameTextField.addActionListener(e -> action.run());
    }
}
