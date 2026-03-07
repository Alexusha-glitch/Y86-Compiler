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
        String temp = "";
        String operator;
        for (String line : lines) {
            String[] arr = line.split(" |,");
            out += "0x" + Integer.toHexString(current_pos) + ": ";
            if (dict.containsKey(arr[0])) { // If it is an operation
                operator = arr[0];
                out += dict.get(operator);
                current_pos++;
                for (int i = 1; i < arr.length; i++) {
                    if (arr[i].charAt(0) == '%') { // If it is a register
                        out += dict.get(arr[i]);
                    } else if (operator.equals("mrmovl")) { // Odd case where registers are backwards
                        temp = arr[i].split("\\(")[0];
                        temp = Integer.toHexString(Integer.parseInt(temp));
                        if (temp.length() == 1) {
                            temp = "0" + temp;
                        }
                        temp = dict.get(arr[i].split("\\(")[1].substring(0, 4)) + temp;
                        i = 2;
                        temp = dict.get(arr[i]) + temp;
                        out += temp; // Completes loop as it edits i value. Translates both
                    } else if (Character.isDigit(arr[i].split("\\(")[0].charAt(0))) {
                        temp = arr[i].split("\\(")[0];
                        temp = Integer.toHexString(Integer.parseInt(temp));
                        if (temp.length() == 1) {
                            temp = "0" + temp;
                        }
                        temp = dict.get(arr[i].split("\\(")[1].substring(0, 4)) + temp;
                        out += temp;
                    }
                }
            } else { // If it is an assembly directive
                if (arr[0].charAt(0) == '.') {
                    if (arr[0].contains(".pos")) {

                    }
                } else { // If it is a symbolic name for point of code ("main: ___")

                }
            }
            out += "\n";
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