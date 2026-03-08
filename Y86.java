import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Y86 {
    // Defines dictionary, translating from commands or registers into numbers
    private final static Map<String, String> dict = Stream.of(new String[][] {
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

private final static Map<String, String> memory = Stream.of(new String[][] {
  { "halt", "1" }, 
  { "nop", "1" }, 
  { "rrmovl", "2" }, 
  { "cmovle", "2" }, 
  { "cmovl", "2" }, 
  { "cmove", "2" }, 
  { "cmovne", "2" }, 
  { "cmovge", "2" }, 
  { "cmovg", "2" }, 
  { "irmovl", "6" }, 
  { "rmmovl", "6" },
  { "mrmovl", "6" },
  { "addl", "2" },
  { "subl", "2" },
  { "andl", "2" },
  { "xorl", "2" },
  { "jmp", "5" },
  { "jle", "5" },
  { "jl", "5" },
  { "je", "5" },
  { "jne", "5" },
  { "jge", "5" },
  { "jg", "5" },
  { "call", "5" },
  { "ret", "1" },
  { "pushl", "2" },
  { "popl", "2" },
}).collect(Collectors.toMap(data -> data[0], data -> data[1]));

    private String int_to_endian(int n) {
        String hex = Integer.toHexString(n);
        for (int i = 0; i < 8-hex.length(); i++) {
            hex = "0" + hex;
        }
        return hex.substring(6, 8) + hex.substring(4, 6) + hex.substring(2, 4) + hex.substring(0, 2);
    }

    private String translator(String code) {
        Map<String, Integer> symbol = new HashMap<>();
        int current_pos = 0;
        String[] lines = code.split("\n");
        String out = "";
        String temp;
        String operator;
        int start;
        for (String line : lines) {
            String[] arr = line.split(" |,");
            operator = arr[0];
            out += "0x" + Integer.toHexString(current_pos) + ": ";
            start = 1;
            if (operator.charAt(0) != '.') { // If it is an operation
                operator = arr[0];
                out += dict.get(operator);
                if (operator.charAt(operator.length()) == ':') {
                    symbol.put(operator, current_pos);
                    operator = arr[1];
                    start = 2;
                }
                for (int i = start; i < arr.length; i++) {
                    if (arr[i].charAt(0) == '%') { // If it is a register
                        out += dict.get(arr[i]);
                    } else if (operator.equals("mrmovl")) { // Odd case where registers are backwards, mrmovl
                        temp = arr[i].split("\\(")[0];
                        temp = int_to_endian(Integer.parseInt(temp));
                        temp = dict.get(arr[i].split("\\(")[1].substring(0, 4)) + temp;
                        i++;
                        temp = dict.get(arr[i]) + temp;
                        out += temp; // Completes loop as it edits i value. Translates both
                    } else if (Character.isDigit(arr[i].split("\\(")[0].charAt(0)) && arr[0].equals("rmmovl")) { // rmmovl
                        temp = arr[i].split("\\(")[0];
                        temp = int_to_endian(Integer.parseInt(temp));
                        temp = dict.get(arr[i].split("\\(")[1].substring(0, 4)) + temp;
                        out += temp;
                    } else if (operator.charAt(0) == 'j' || operator.equals("call")) { // Jumping or Calling. _ Used as an indicator to come back and replace with address
                        out += "_" + arr[i];
                    } else if (Character.isDigit(arr[i].charAt(0)) && operator.equals("irmovl")) { // irmovl
                        temp = int_to_endian(Integer.parseInt(arr[i]));
                        i++;
                        out += "f" + dict.get(arr[i]) + temp;
                    } else if (operator.equals("irmovl")) { // irmovl for stuff like stacks
                        i++;
                        out += "f" + dict.get(arr[i]) + "_" + arr[i-1];
                    } else {
                        out += "f";
                    }
                }
                current_pos += Integer.parseInt(memory.get(operator));
            } else { // If it is an assembly directive
                if (arr[0].charAt(0) == '.') {
                    if (arr[0].contains(".pos")) {
                        current_pos = Integer.parseInt(arr[1]);
                    } else if (arr[0].contains(".align")) {
                        current_pos += Integer.parseInt(arr[1]) - current_pos % Integer.parseInt(arr[1]);
                    } else {
                        out += int_to_endian(Integer.parseInt(arr[1].substring(2, arr[1].length()), 16));
                    }
                }
            }
            out += "\n";
        }

        boolean done = false;
        String name;
        int j;
        for (int i = 0; i < out.length(); i++) {
            if (out.charAt(i) == '_') {
                name = "";
                j = i+1;
                while (!done) {
                    if (!Character.isDigit(out.charAt(j))) {
                        name += out.charAt(j);
                        j++;
                    }
                }
                out = out.substring(0, i) + symbol.get(name) + out.substring(j-1, out.length());
            }
        }

        return out;
    }
    public static void main(String[] args) {
        
    }
}