import java.awt.*;
import javax.swing.*;

public class PipelineVisualiser extends JFrame {
    private static final String[] STAGE_NAMES = {
        "Fetch", "Decode", "Execute", "Memory", "Writeback"
    };

    private static final String[][] STAGE_FIELDS = {
        {"PC", "icode", "ifun", "rA", "rB", "valC", "valP"},
        {"icode", "rA", "rB", "valA", "valB"},
        {"icode", "ifun", "valA", "valB", "valC", "ZF", "SF", "OF", "valE", "cond", "newZF", "newSF", "newOF"},
        {"icode", "valA", "valE", "valP", "valM"},
        {"icode", "rA", "rB", "valE", "valM", "cond"}
    };
    private static final String[] PC_FIELDS = {"icode", "valP", "cond", "valC", "valM", "newPC"};

    private final JTextArea inputArea;
    private final JTextArea[] stageAreas;
    private final JTextArea pcArea;
    private final JLabel statusLabel;
    private final JButton pauseButton;
    private final JComboBox<String> speedBox;
    private final Timer timer;

    private String[] pipelineLines;
    private int instruction;
    private int stage;

    public PipelineVisualiser() {
        setTitle("Y86 Pipeline Visualiser");
        setSize(1100, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        inputArea = makeTextArea(true);
        inputArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        inputArea.setTabSize(4);

        stageAreas = new JTextArea[STAGE_NAMES.length];
        pcArea = makeTextArea(false);
        statusLabel = new JLabel("Load translated assembly, then press Go.");
        pauseButton = new JButton("Pause");
        speedBox = new JComboBox<>(new String[] {"Normal", "Slow", "High"});
        timer = new Timer(getTimerDelay(), e -> advanceAnimation());

        JPanel visualPanel = createVisualPanel();

        JPanel centerPanel = new JPanel(new GridLayout(1, 2, 10, 10));
        centerPanel.add(wrapPanel("Translated Assembly Input", new JScrollPane(inputArea)));
        centerPanel.add(wrapPanel("Pipeline", visualPanel));

        JButton goButton = new JButton("Go");
        JButton resetButton = new JButton("Reset");
        JButton sampleButton = new JButton("Load Sample");

        goButton.addActionListener(e -> startPipeline());
        pauseButton.addActionListener(e -> togglePause());
        resetButton.addActionListener(e -> resetAll());
        sampleButton.addActionListener(e -> loadSample());
        speedBox.addActionListener(e -> timer.setDelay(getTimerDelay()));

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(goButton);
        buttonPanel.add(pauseButton);
        buttonPanel.add(resetButton);
        buttonPanel.add(sampleButton);
        buttonPanel.add(new JLabel("Speed"));
        buttonPanel.add(speedBox);

        setLayout(new BorderLayout(10, 10));
        add(statusLabel, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        ((JComponent) getContentPane()).setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    }

    private JPanel createVisualPanel() {
        JPanel panel = new JPanel(new GridLayout(2, 3, 8, 8));

        for (int i = 0; i < STAGE_NAMES.length; i++) {
            stageAreas[i] = makeTextArea(false);
            panel.add(wrapPanel(STAGE_NAMES[i], stageAreas[i]));
        }

        panel.add(wrapPanel("PC", pcArea));
        return panel;
    }

    private JPanel wrapPanel(String title, Component component) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder(title));
        panel.add(component, BorderLayout.CENTER);
        return panel;
    }

    private void startPipeline() {
        String input = inputArea.getText();

        if (input.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter translated Y86 assembly first.");
            return;
        }

        try {
            String[] result = PipelineFunctions.loop(input);
            pipelineLines = result[0].split("\n");
            instruction = 0;
            stage = 0;
            clearStages();
            showStage();
            timer.setDelay(getTimerDelay());
            pauseButton.setText("Pause");
            timer.start();
        } catch (Exception ex) {
            timer.stop();
            pauseButton.setText("Pause");
            statusLabel.setText("Pipeline failed.");
            JOptionPane.showMessageDialog(
                this,
                "Pipeline failed.\nCheck that the input is translated assembly.",
                "Error",
                JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void togglePause() {
        if (pipelineLines == null) {
            return;
        }

        if (timer.isRunning()) {
            timer.stop();
            pauseButton.setText("Resume");
            statusLabel.setText("Paused at instruction " + (instruction + 1) + " | " + STAGE_NAMES[stage]);
        } else {
            timer.setDelay(getTimerDelay());
            pauseButton.setText("Pause");
            timer.start();
        }
    }

    private JTextArea makeTextArea(boolean editable) {
        JTextArea area = new JTextArea();
        area.setEditable(editable);
        area.setFont(new Font("Monospaced", Font.PLAIN, 13));
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        return area;
    }

    private void advanceAnimation() {
        if (pipelineLines == null) {
            return;
        }

        if (stage < STAGE_NAMES.length - 1 && getLineIndex(instruction, stage + 1) < pipelineLines.length) {
            stage++;
            showStage();
        } else if (getLineIndex(instruction + 1, 0) < pipelineLines.length) {
            instruction++;
            stage = 0;
            clearStages();
            showStage();
        } else {
            timer.stop();
            pauseButton.setText("Pause");
            statusLabel.setText("Pipeline finished.");
        }
    }

    private void showStage() {
        int lineIndex = getLineIndex(instruction, stage);
        if (lineIndex >= pipelineLines.length) {
            timer.stop();
            return;
        }

        for (int i = 0; i < STAGE_NAMES.length; i++) {
            stageAreas[i].setBackground(i == stage ? new Color(245, 250, 255) : Color.WHITE);
        }

        stageAreas[stage].setText(formatStageLine(stage, pipelineLines[lineIndex]));
        showPCLine();
        statusLabel.setText("Instruction " + (instruction + 1) + " | " + STAGE_NAMES[stage]);
    }

    private void showPCLine() {
        int pcIndex = getLineIndex(instruction, 5);

        if (stage == STAGE_NAMES.length - 1 && pcIndex < pipelineLines.length) {
            pcArea.setText(formatLine(PC_FIELDS, pipelineLines[pcIndex], true));
        } else if (getLineIndex(instruction, 0) < pipelineLines.length) {
            String[] fetchValues = pipelineLines[getLineIndex(instruction, 0)].split(" ", -1);
            pcArea.setText("PC: " + displayValue(fetchValues[0]));
        } else {
            pcArea.setText("");
        }
    }

    private String formatStageLine(int stageNumber, String line) {
        return formatLine(STAGE_FIELDS[stageNumber], line, false);
    }

    private String formatLine(String[] fields, String line, boolean inline) {
        String[] values = line.split(" ", -1);
        StringBuilder out = new StringBuilder();

        for (int i = 0; i < values.length; i++) {
            String label = i < fields.length ? fields[i] : "value" + i;
            out.append(label).append(inline ? "=" : ": ").append(displayValue(values[i]));
            out.append(inline ? "  " : "\n");
        }

        return out.toString();
    }

    private String displayValue(String value) {
        if (value.isEmpty()) {
            return "(empty)";
        }
        if (value.equalsIgnoreCase("f")) {
            return "F (none)";
        }
        return value;
    }

    private int getLineIndex(int instructionNumber, int stageNumber) {
        return instructionNumber * 6 + stageNumber;
    }

    private int getTimerDelay() {
        String selected = (String) speedBox.getSelectedItem();
        if ("Slow".equals(selected)) {
            return 1200;
        }
        if ("High".equals(selected)) {
            return 300;
        }
        return 700;
    }

    private void loadSample() {
        inputArea.setText(Y86.translator(Y86.SAMPLE_PROGRAM));
        resetPipelineState();
        statusLabel.setText("Sample translated assembly loaded.");
    }

    private void resetAll() {
        inputArea.setText("");
        resetPipelineState();
        statusLabel.setText("Load translated assembly, then press Go.");
    }

    private void resetPipelineState() {
        timer.stop();
        pauseButton.setText("Pause");
        pipelineLines = null;
        instruction = 0;
        stage = 0;
        clearStages();
    }

    private void clearStages() {
        for (int i = 0; i < STAGE_NAMES.length; i++) {
            stageAreas[i].setText("");
            stageAreas[i].setBackground(Color.WHITE);
        }
        pcArea.setText("");
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            PipelineVisualiser gui = new PipelineVisualiser();
            gui.setVisible(true);
        });
    }
}
