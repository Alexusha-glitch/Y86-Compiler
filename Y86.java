import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Y86 {
    public static final String SAMPLE_PROGRAM = """
            .pos 0
            init:   irmovl Stack, %esp
                    irmovl Stack, %ebp
                    call Main
                    halt

            .align 4
            array:  .long 0xd
                    .long 0xc0
                    .long 0xb00
                    .long 0xa000

            Main:   pushl %ebp
                    rrmovl %esp, %ebp
                    irmovl $4, %eax
                    pushl %eax
                    irmovl array, %edx
                    pushl %edx
                    call Sum
                    rrmovl %ebp, %esp
                    popl %ebp
                    ret
            Sum:    pushl %ebp
                    rrmovl %esp, %ebp
                    mrmovl 8(%ebp), %ecx
                    mrmovl 12(%ebp), %edx
                    xorl %eax, %eax
                    andl %edx, %edx
                    je End
            Loop:   mrmovl (%ecx), %esi
                    addl %esi, %eax
                    irmovl $4, %ebx
                    addl %ebx, %ecx
                    irmovl $-1, %ebx
                    addl %ebx, %edx
                    jne Loop
            End:    rrmovl %ebp, %esp
                    popl %ebp
                    ret

            .pos 0x100
            Stack:
            """;

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
  { "pushl", "a0" },
  { "popl", "b0" },
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

    private static String int_to_endian(int n) {
        String hex = Integer.toHexString(n);
        int length = 8-hex.length();
        for (int i = 0; i < length; i++) {
            hex = "0" + hex;
        }
        return hex.substring(6, 8) + hex.substring(4, 6) + hex.substring(2, 4) + hex.substring(0, 2);
    }

    private static void print_arr(String[] a) {
        for (int i = 0; i < a.length; i++) {
            System.out.println(a[i]);
        }
    }

    public static String translator(String code) {
        Map<String, Integer> symbol = new HashMap<>();
        int current_pos = 0;
        String[] lines = code.split("\n");
        String out = "";
        String temp;
        String operator;
        int start;
        int counter;
        for (String line : lines) {
            counter = 0;
            if (line.isEmpty()) {
                continue;
            }
            String[] arr = line.split("\s*,\s*|\\s+");
            out += "0x" + Integer.toHexString(current_pos) + ": ";
            start = 1;
            operator = arr[counter];
            while (operator.isEmpty()) {
                counter++;
                operator = arr[counter];
            }
            if (operator.charAt(operator.length()-1) == ':') {
                symbol.put(operator.substring(0, operator.length() - 1), current_pos);
                if (arr.length > counter+1) {
                    counter++;
                    operator = arr[counter];
                } else {
                    continue;
                }
            }
            if (operator.charAt(0) != '.') { // If it is an operation or symbol with operation
                out += dict.get(operator);
                start = counter + 1;
                for (int i = start; i < arr.length; i++) {
                    if (arr[i].charAt(0) == '%') { // If it is a register
                        out += dict.get(arr[i]);
                    } else if (operator.equals("mrmovl")) { // Odd case where registers are backwards, mrmovl
                        temp = arr[i].split("\\(")[0];
                        if (!temp.isEmpty()) {
                            temp = int_to_endian(Integer.parseInt(temp));
                            temp = dict.get(arr[i].split("\\(")[1].substring(0, 4)) + temp;
                        } else {
                            temp = dict.get(arr[i].split("\\(")[1].substring(0, 4)) + int_to_endian(0);
                        }
                        i++;
                        temp = dict.get(arr[i]) + temp;
                        out += temp; // Completes loop as it edits i value. Translates both
                    } else if (Character.isDigit(arr[i].split("\\(")[0].charAt(0)) && operator.equals("rmmovl")) { // rmmovl
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
                        if (arr[i-1].charAt(0) != '$') {
                            out += "f" + dict.get(arr[i]) + "_" + arr[i-1];
                        } else {
                            out += "f" + dict.get(arr[i]) + int_to_endian(Integer.parseInt(arr[i-1].substring(1, arr[i-1].length())));
                        }
                    } else {
                        out += "f";
                    }
                }
                if (operator.equals("pushl") || operator.equals("popl")) {
                    out += "f";
                }
                current_pos += Integer.parseInt(memory.get(operator));
            } else { // If it is an assembly directive
                if (operator.charAt(0) == '.') {
                    if (operator.contains(".pos")) {
                        if (arr[1].length() >= 2 && arr[1].charAt(1) == 'x') {
                            current_pos = Integer.parseInt(arr[1].substring(2, arr[1].length()), 16);
                        } else {
                            current_pos = Integer.parseInt(arr[1]);
                        }
                    } else if (operator.contains(".align")) {
                        if (arr[1].length() >= 2 && arr[1].charAt(1) == 'x') {
                            current_pos += Integer.parseInt(arr[1].substring(2, arr[1].length()), 16) - current_pos % Integer.parseInt(arr[1].substring(2, arr[1].length()), 16);
                        } else {
                            current_pos += Integer.parseInt(arr[1]) - current_pos % Integer.parseInt(arr[1]);
                        }
                    } else {
                        out += int_to_endian(Integer.parseInt(arr[counter+1].substring(2, arr[counter+1].length()), 16));
                        current_pos += 4;
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
                    if (Character.isLetter(out.charAt(j))) {
                        name += out.charAt(j);
                        j++;
                    } else {
                        done = true;
                    }
                }
                if (!name.isEmpty()) {
                    out = out.substring(0, i) + int_to_endian(symbol.get(name)) + out.substring(j, out.length());
                }
                done = false;
            }
        }

        return out;
    }
    public static void main(String[] args) {
        System.out.println(
            translator(SAMPLE_PROGRAM)
        );
    }
}