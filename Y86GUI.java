import java.awt.*;
import javax.swing.*;

public class Y86GUI extends JFrame {
    private final JTextArea inputArea;
    private final JTextArea outputArea;

    public Y86GUI() {
        setTitle("Y86 Assembler GUI");
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        inputArea = new JTextArea();
        outputArea = new JTextArea();

        inputArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        outputArea.setFont(new Font("Monospaced", Font.PLAIN, 14));

        outputArea.setEditable(false);
        inputArea.setTabSize(4);

        JScrollPane inputScroll = new JScrollPane(inputArea);
        JScrollPane outputScroll = new JScrollPane(outputArea);

        JPanel centerPanel = new JPanel(new GridLayout(1, 2, 10, 10));
        centerPanel.add(wrapPanel("Y86 Input", inputScroll));
        centerPanel.add(wrapPanel("Machine Code Output", outputScroll));

        JButton assembleButton = new JButton("Assemble");
        JButton clearButton = new JButton("Clear");
        JButton sampleButton = new JButton("Load Sample");

        assembleButton.addActionListener(e -> assembleCode());
        clearButton.addActionListener(e -> {
            inputArea.setText("");
            outputArea.setText("");
        });
        sampleButton.addActionListener(e -> inputArea.setText(Y86.SAMPLE_PROGRAM));

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(assembleButton);
        buttonPanel.add(clearButton);
        buttonPanel.add(sampleButton);

        setLayout(new BorderLayout(10, 10));
        add(centerPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        ((JComponent) getContentPane()).setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    }

    private JPanel wrapPanel(String title, JScrollPane scrollPane) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder(title));
        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    private void assembleCode() {
        String input = inputArea.getText();

        if (input.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter Y86 assembly code first.");
            return;
        }

        try {
            String result = Y86.translator(input);
            outputArea.setText(result);
        } catch (Exception ex) {
            outputArea.setText("Error while assembling:\n" + ex.getMessage());
            JOptionPane.showMessageDialog(
                this,
                "Assembly failed.\nCheck the input format.",
                "Error",
                JOptionPane.ERROR_MESSAGE
            );
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Y86GUI gui = new Y86GUI();
            gui.setVisible(true);
        });
    }
}