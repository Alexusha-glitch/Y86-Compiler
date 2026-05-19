import java.util.HashMap;

public class PipelineFunctions {
    public static String registersToString(HashMap<String, String> registers) {
        String s = "";

        for (int i = 0; i <= 7; i++) {
            s += registers.get(Integer.toString(i));
            if (i != 7) {
                s += " ";
            }
        }

        return s;
    }
    
    public static int parseHexInt(String s) {
        return (int) Long.parseLong(s.substring(2), 16);
    }

    public static String littleEndianToHex(String s) {
        String ret = "";
        for (int i = s.length(); i > 0; i -= 2) {
            ret += s.substring(i-2, i);
        }
        return "0x" + Long.toHexString(Long.parseLong(ret, 16));
    }

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

        for (int i = 0; i <= 7; i++) {
            registers.put(Integer.toString(i), "0x0");
        }

        return registers;
    }

    public static String[] loop(String assembly) {
        String[] ret = new String[2];
        HashMap<String, String> registers = createRegisters();
        HashMap<String, String> instructionMemory = assemblyToMemory(assembly);
        HashMap<String, String> dataMemory = new HashMap<>();
        String PC = "0x0";
        boolean ZF = false;
        boolean SF = false;
        boolean OF = false;
        String output = "";
        String registerOutput = "";
        registerOutput += registersToString(registers) + "\n";
        String[] fetched, decoded, executed;
        String icode, ifun, rA, rB, valC, valP, valA, valB, valE, cond, valM, newPC;
        boolean oldZF, oldSF, oldOF;
        
        while(true) {
            fetched = fetch(instructionMemory, PC);
            icode = fetched[0];
            ifun = fetched[1];
            rA = fetched[2];
            rB = fetched[3];
            valC = fetched[4];
            valP = fetched[5];

            output = output + PC + " " + icode + " " + ifun + " " + rA + " " + rB + " " + valC + " " + valP + "\n";

            if (icode.equals("0")) {
                break;
            }

            decoded = decode(icode, rA, rB, registers);

            valA = decoded[0];
            valB = decoded[1];

            output = output + icode + " " + rA + " " + rB + " " + valA + " " + valB + "\n";

            oldZF = ZF;
            oldSF = SF;
            oldOF = OF;

            executed = execute(icode, ifun, valA, valB, valC, ZF, SF, OF);

            valE = executed[0];
            cond = executed[1];

            ZF = executed[2].equals("1");
            SF = executed[3].equals("1");
            OF = executed[4].equals("1");

            output = output + icode + " " + ifun + " " + valA + " " + valB + " " + valC + " " + (oldZF ? "1" : "0") + " " + (oldSF ? "1" : "0") + " " + (oldOF ? "1" : "0") + " " + valE + " " + cond + " " + executed[2] + " " + executed[3] + " " + executed[4] + "\n";

            valM = memory(icode, valA, valE, valP, dataMemory);

            output = output + icode + " " + valA + " " + valE + " " + valP + " " + valM + "\n";

            writeBack(icode, rA, rB, valE, valM, cond, registers);

            output = output + icode + " " + rA + " " + rB + " " + valE + " " + valM + " " + cond + "\n";

            newPC = PC(icode, valP, cond, valC, valM);

            output = output + icode + " " + valP + " " + cond + " " + valC + " " + valM + " " + newPC + "\n";

            PC = newPC;
            registerOutput += registersToString(registers) + "\n";
        }
        
        ret[0] = output;
        ret[1] = registerOutput;

        return ret;
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
                PC = "0x" + Integer.toHexString(parseHexInt(PC) + 1);
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
            ret[4] = littleEndianToHex(line.substring(4));
        } else if (ret[0].equals("7") || ret[0].equals("8")) {
            ret[4] = littleEndianToHex(line.substring(2));
        } else {
            ret[4] = "0x0";
        }

        ret[5] = "0x" + Integer.toHexString(parseHexInt(PC) + line.length()/2);

        return ret;
    }

    public static String[] decode(String icode, String rA, String rB, HashMap<String, String> registers) {
        String[] ret = new String[2];

        String valA = "";
        String valB = "";

        if (icode.equals("2") || icode.equals("4") || icode.equals("6") || icode.equals("a")) {
            valA = registers.get(rA);
        } else if (icode.equals("9") || icode.equals("b")) {
            valA = registers.get("4");
        }

        if (icode.equals("4") || icode.equals("5") || icode.equals("6")) {
            valB = registers.get(rB);
        } else if (icode.equals("8") || icode.equals("9") || icode.equals("a") || icode.equals("b")) {
            valB = registers.get("4");
        }

        ret[0] = valA;
        ret[1] = valB;

        return ret;
    }

    @SuppressWarnings("ConvertToStringSwitch")
    public static String[] execute(String icode, String ifun, String valA, String valB, String valC, boolean ZF, boolean SF, boolean OF) {
        String[] ret = new String[5];

        String valE = "0x0";
        boolean cond = false;

        if (icode.equals("2")) {
            valE = valA;
            if (ifun.equals("0")) {
                cond = true;
            } else if (ifun.equals("1")) {
                cond = (SF^OF) || ZF;
            } else if (ifun.equals("2")) {
                cond = (SF^OF);
            } else if (ifun.equals("3")) {
                cond = (ZF);
            } else if (ifun.equals("4")) {
                cond = (!ZF);
            } else if (ifun.equals("5")) {
                cond = (!(SF^OF));
            } else if (ifun.equals("6")) {
                cond = !(SF^OF) && !ZF;
            }
        } else if (icode.equals("3")) {
            valE = valC;
        } else if (icode.equals("4") || icode.equals("5")) {
            valE = "0x" + Integer.toHexString(parseHexInt(valB) + parseHexInt(valC));
        } else if (icode.equals("6")) {
            if (ifun.equals("0")) {
                valE = "0x" + Integer.toHexString(parseHexInt(valB) + parseHexInt(valA));
                OF = ((parseHexInt(valA) < 0) == (parseHexInt(valB) < 0)) && ((parseHexInt(valE) < 0) != (parseHexInt(valA) < 0));
            } else if (ifun.equals("1")) {
                valE = "0x" + Integer.toHexString(parseHexInt(valB) - parseHexInt(valA));
                OF = ((parseHexInt(valA) < 0) != (parseHexInt(valB) < 0)) && ((parseHexInt(valE) < 0) != (parseHexInt(valB) < 0));
            } else if (ifun.equals("2")) {
                valE = "0x" + Integer.toHexString(parseHexInt(valB) & parseHexInt(valA));
                OF = false;
            } else if (ifun.equals("3")) {
                valE = "0x" + Integer.toHexString(parseHexInt(valB) ^ parseHexInt(valA));
                OF = false;
            }
            ZF = parseHexInt(valE) == 0;
            SF = parseHexInt(valE) < 0;
        } else if (icode.equals("7")) {
            valE = valC;
            if (ifun.equals("0")) {
                cond = true;
            } else if (ifun.equals("1")) {
                cond = (SF^OF) || ZF;
            } else if (ifun.equals("2")) {
                cond = (SF^OF);
            } else if (ifun.equals("3")) {
                cond = (ZF);
            } else if (ifun.equals("4")) {
                cond = (!ZF);
            } else if (ifun.equals("5")) {
                cond = (!(SF^OF));
            } else if (ifun.equals("6")) {
                cond = !(SF^OF) && !ZF;
            }
        } else if (icode.equals("8")) {
            valE = "0x" + Integer.toHexString(parseHexInt(valB) - 4);
        } else if (icode.equals("9")) {
            valE = "0x" + Integer.toHexString(parseHexInt(valB) + 4);
        } else if (icode.equals("a")) {
            valE = "0x" + Integer.toHexString(parseHexInt(valB) - 4);
        } else if (icode.equals("b")) {
            valE = "0x" + Integer.toHexString(parseHexInt(valB) + 4);
        }

        ret[0] = valE;
        if (cond) {
            ret[1] = "1";
        } else {
            ret[1] = "0";
        }
        if (ZF) {
            ret[2] = "1";
        } else {
            ret[2] = "0";
        }
        if (SF) {
            ret[3] = "1";
        } else {
            ret[3] = "0";
        }
        if (OF) {
            ret[4] = "1";
        } else {
            ret[4] = "0";
        }

        return ret;
    }

    public static String memory(String icode, String valA, String valE, String valP, HashMap<String, String> dataMemory) {
        String valM = "0x0";

        if (icode.equals("4")) {
            dataMemory.put(valE, valA);
        } else if (icode.equals("5")) {
            valM = dataMemory.getOrDefault(valE, "0x0");
        } else if (icode.equals("8")) {
            dataMemory.put(valE, valP);
        } else if (icode.equals("9")) {
            valM = dataMemory.getOrDefault(valA, "0x0");
        } else if (icode.equals("a")) {
            dataMemory.put(valE, valA);
        } else if (icode.equals("b")) {
            valM = dataMemory.getOrDefault(valA, "0x0");
        }

        return valM;
    }

    public static void writeBack(String icode, String rA, String rB, String valE, String valM, String cond, HashMap<String, String> registers) {
        if (icode.equals("2")) {
            if (cond.equals("1")) {
                registers.put(rB, valE);
            }
        } else if (icode.equals("3")) {
            registers.put(rB, valE);
        } else if (icode.equals("5")) {
            registers.put(rA, valM);
        } else if (icode.equals("6")) {
            registers.put(rB, valE);
        } else if (icode.equals("8")) {
            registers.put("4", valE);
        } else if (icode.equals("9")) {
            registers.put("4", valE);
        } else if (icode.equals("a")) {
            registers.put("4", valE);
        } else if (icode.equals("b")) {
            registers.put("4", valE);
            registers.put(rA, valM);
        }
    }

    public static String PC(String icode, String valP, String cond, String valC, String valM) {
        if (icode.equals("8")) {
            return valC;
        } else if (icode.equals("7") && cond.equals("1")) {
            return valC;
        } else if (icode.equals("9")) {
            return valM;
        } else {
            return valP;
        }
    }

    public static void main(String[] args) {
        int i = 1;
        System.out.println(loop("0x0: 30f400010000 0x6: 30f500010000 0xc: 8024000000 0x11: 00 0x14: 0d000000 0x18: c0000000 0x1c: 000b0000 0x20: 00a00000 0x24: a05f 0x26: 2045 0x28: 30f004000000 0x2e: a00f 0x30: 30f214000000 0x36: a02f 0x38: 8042000000 0x3d: 2054 0x3f: b05f 0x41: 90 0x42: a05f 0x44: 2045 0x46: 501508000000 0x4c: 50250c000000 0x52: 6300 0x54: 6222 0x56: 7378000000 0x5b: 506100000000 0x61: 6060 0x63: 30f304000000 0x69: 6031 0x6b: 30f3ffffffff 0x71: 6032 0x73: 745b000000 0x78: 2054 0x7a: b05f 0x7c: 90")[i]);
    }
}