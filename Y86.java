import java.awt.*;
import javax.swing.*;

public class Y86 {
    public static void main(String[] args) {
        JFrame frame = new JFrame("Y86 Compiler");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);
        frame.setBackground(Color.darkGray);

        JPanel panel = new JPanel();
        
        JLabel title = new JLabel("Y86 Code");
        title.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(title);

        JLabel Y86_Code = new JLabel("Y86 Code:");
        panel.add(Y86_Code);

        frame.add(panel);

        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}