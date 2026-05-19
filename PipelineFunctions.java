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
        int PC = 0;
        int ZF = 0;
        int SF = 0;
        int OF = 0;
    }

    public static String[] fetch(HashMap<String, String> memory, String PC) {
        String[] ret = new String[6];
        boolean done = false;
        String line = "";
        
        while (!done) {
            line = memory.get(PC);

            if (line != null) {
                done = true;
            } else {
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

        if (ret[0].equals("3") || ret[0].equals("4") || ret[0].equals("5")) {
            ret[4] = line.substring(4);
        } else if (ret[0].equals("7") || ret[0].equals("8")) {
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

    public static String[] execute(String icode, String ifun, String valA, String valB, String valC) {
        String[] ret = new String[2];

        String valE = "0x0";
        boolean cond = false;

        if (icode.equals("2")) {
            valE = valA;
        } else if (icode.equals("3")) {
            valE = valC;
        } else if (icode.equals("4") || icode.equals("5")) {
            valE = Integer.toHexString(Integer.decode(valB) + Integer.decode(valC));
        } else if (icode.equals("6")) {
            valE = Integer.toHexString(Integer.decode(valA) + Integer.decode(valB));
        } else if (icode.equals("7")) {
            valE = valC;
            if (ifun.equals("0")) {
                cond = true;
            } else if (ifun.equals("1")) {
                cond = (Integer.decode(valA) <= Integer.decode(valB));
            } else if (ifun.equals("2")) {
                cond = (Integer.decode(valA) < Integer.decode(valB));
            } else if (ifun.equals("3")) {
                cond = (Integer.decode(valA) == Integer.decode(valB));
            } else if (ifun.equals("4")) {
                cond = (Integer.decode(valA) != Integer.decode(valB));
            } else if (ifun.equals("5")) {
                cond = (Integer.decode(valA) >= Integer.decode(valB));
            } else if (ifun.equals("6")) {
                cond = (Integer.decode(valA) > Integer.decode(valB));
            }
        } else if (icode.equals("8")) {
            valE = Integer.toHexString(Integer.decode(valB) - 4);
        } else if (icode.equals("9")) {
            valE = Integer.toHexString(Integer.decode(valB) + 4);
        } else if (icode.equals("a")) {
            valE = Integer.toHexString(Integer.decode(valB) - 4);
        } else if (icode.equals("b")) {
            valE = Integer.toHexString(Integer.decode(valB) + 4);
        }

        ret[0] = valE;
        if (cond) {
            ret[1] = "1";
        } else {
            ret[1] = "0";
        }

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

    public static String PC(String valP, String cond, String valC) {
        if (cond.equals("0")) {
            return valC;
        } else {
            return valP;
        }
    }

    public static void main(String[] args) {
        
    }
}