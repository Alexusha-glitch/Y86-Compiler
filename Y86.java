import java.awt.*;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.swing.*;

public class Y86 {
    private static Map<String, String> map = Stream.of(new String[][] {
  { "halt", "00" }, 
  { "nop", "10" }, 
  { "rrmovl", "20" }, 
  { "cmovle", "21" }, 
  { "cmovl", "22" }, 
  { "cmove", "23" }, 
  { "cmovne", "24" }, 
  { "cmovge", "25" }, 
  { "cmovg", "26" }, 
  { "irmovl", "30" }, 
  { "rmmovl", "40" },
  { "mrmovl", "50" },
  { "addl", "60" },
  { "subl", "61" },
  { "andl", "62" },
  { "xorl", "63" },
  { "jmp", "70" },
  { "jle", "71" },
  { "jl", "72" },
  { "je", "73" },
  { "jne", "74" },
  { "jge", "75" },
  { "jg", "76" },
  { "call", "80" },
  { "ret", "90" },
  { "pushl", "A0" },
  { "popl", "B0" },
  { "%eax", "0" },
  { "%ecx", "1" },
  { "%edx", "2" },
  { "%ebx", "3" },
  { "%esp", "4" },
  { "%ebp", "5" },
  { "%esi", "6" },
  { "%edi", "7" },
}).collect(Collectors.toMap(data -> data[0], data -> data[1]));

    private static String line_translator(String line) {
        String[] arr = line.split(" ");
        String out = "";
        for (String s : arr) {
            if (!(s.contains(":"))) {
                out = out + map.get(s);
            }
        }
        return out;
    }
    public static void main(String[] args) {
        JFrame frame = new JFrame("Y86 Compiler");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);
        frame.setBackground(Color.darkGray);

        System.out.println(line_translator("addl %eax %ecx"));

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