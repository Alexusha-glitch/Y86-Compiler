import java.util.HashMap;

public class PipelineFunctions {
    public static HashMap<String, String> assemblyToMemory(String assembly) {
        HashMap<String, String> memory = new HashMap<>();
        String[] assemblyArr = assembly.split("\n| ");
        String address;
        String command;

        for (int i = 0; i < assemblyArr.length; i += 2) {
            address = assemblyArr[i].substring(0, assemblyArr[i].length() - 1);
            command = assemblyArr[i+1];
            memory.put(address, command);
        }

        return memory;
    }
    public static HashMap<String, String> createRegisters() {
        HashMap<String, String> registers = new HashMap<>();

        for (int i = 0; i <= 15; i++) {
            registers.put(Integer.toString(i), "0x0");
        }

        return registers;
    }

    public static String loop(String assembly) {
        HashMap<String, String> registers = createRegisters();
        HashMap<String, String> memory = assemblyToMemory(assembly);
        String PC = "0x0";
    }

    public static String[] fetch(HashMap<String, String> memory, String PC) {
        String[] ret = new String[6];
        boolean done = false;
        String line = "";
        
        while (!done) {
            try {
                line = memory.get(PC);
                done = true;
            } catch (Exception e) {
                PC = Integer.toHexString(Integer.decode(PC) + 1);
            }
        }
        
        ret[0] = line.substring(0, 1); // icode
        ret[1] = line.substring(1, 2); // ifun

        if (ret[0].equals("0") || ret[0].equals("1") || ret[0].equals("7") || ret[0].equals("8") || ret[0].equals("9")) {
            ret[2] = "F";
            ret[3] = "F";
        } else {
            ret[2] = line.substring(2, 3);
            ret[3] = line.substring(3, 4);
        }

        if (ret[0].equals(3) || ret[0].equals(4) || ret[0].equals(5)) {
            ret[4] = line.substring(4);
        } else if (ret[0].equals(7) || ret[0].equals(8)) {
            ret[4] = line.substring(2);
        } else {
            ret[4] = "0";
        }

        ret[5] = Integer.toHexString(Integer.decode(PC) + line.length()/2);

        return ret;
    }

    public static String[] decode(String rA, String rB, HashMap<String, String> registers) {
        String[] ret = new String[2];

        if (!rA.equals("F")) {
            ret[0] = registers.get(rA);
        } else {
            ret[0] = "";
        }

        if (!rA.equals("F")) {
            ret[1] = registers.get(rB);
        } else {
            ret[1] = "";
        }

        return ret;
    }

    public static int[] execute(int ifun, int valA, int valB) {
        int[] ret = new int[3];

        

        return ret;
    }

    public static int[] memory(int valA, int valE, int valP) {
        int[] ret = new int[2];

        

        return ret;
    }

    public static int writeBack(int valE, int valM) {
        int ret = 0;

        

        return ret;
    }

    public static int PC(int valP, int CC, int valC) {
        int ret = 0;

        

        return ret;
    }

    public static void main(String[] args) {
        
    }
}