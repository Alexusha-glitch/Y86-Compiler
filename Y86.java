import java.awt.*;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.swing.*;

public class Y86 {
    // Defines dictionary, translating from commands or registers into numbers
    private static Map<String, String> dict = Stream.of(new String[][] {
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

    private static String translator(String code) {
        int current_pos = 0;
        String[] lines = code.split("\n");
        String out = "";
        for (String line : lines) {
            String[] arr = line.split(" ");
            out += "0x" + current_pos;
            if (dict.containsKey(arr[0])) { // If it is an operation

            } else { // If it is an assembly directive
                if (arr[0].charAt(0) == '.') {

                } else { // If it is a symbolic name for point of code ("main: ___")

                }
            }
        }
        return out;
    }
    public static void main(String[] args) {
        JFrame frame = new JFrame("Y86 Compiler");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);
        frame.setBackground(Color.darkGray);



        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}