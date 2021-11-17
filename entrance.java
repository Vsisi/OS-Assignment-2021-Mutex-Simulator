import javax.swing.*;

import java.util.Vector;
import java.util.Queue;
import java.util.LinkedList;
import java.awt.*;
import java.awt.event.*;

class Constant{
    static final String SOFTWARE_TITIE = "CS Mutex Algorithm(Software) Simulator V1.0";
    static final int WIN_WIDTH = 1024;
    static final int WIN_HEIGHT = 768;
}

public class entrance {
    public static void main(String[] args) {
        new GUI();
    }
}
class GUI {
    public GUI() {
        createMainInterface();
    }
    private void createMainInterface() {
        initMenu();
        initFrame();
        frame.setResizable(false);
    }
    private void initMenu() {
        topMenu = new JMenuBar();
        topMenu.add(createFileMenu());
        topMenu.add(createFunctionMenu());
        topMenu.setVisible(true);
    }
    private void initFrame() {
        frame = new JFrame(Constant.SOFTWARE_TITIE);
        frame.setVisible(true);
        frame.setBounds(270, 15, Constant.WIN_WIDTH, Constant.WIN_HEIGHT + 70);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainUI = new JPanel();
        frame.add(mainUI);
        frame.setJMenuBar(topMenu);
        switchInterface("None");  
    }
    private JMenu createFileMenu() {
        JMenu file = new JMenu("File");
        JMenuItem exit = new JMenuItem("Exit");
        exit.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });
        file.add(exit);
        return file;
    }
    private JMenu createFunctionMenu() {
        JMenu function = new JMenu("Mode");
        JMenu criticalSec = new JMenu("Critical Section Mutex Algorithm(Software)");
        JMenuItem peterson = new JMenuItem("Peterson");
        addMenuItemListener_Function(peterson, "Peterson");
        JMenuItem dekker = new JMenuItem("Dekker");
        addMenuItemListener_Function(dekker, "Dekker");
        JMenuItem lamport = new JMenuItem("Lamport");
        addMenuItemListener_Function(lamport, "Lamport");
        JMenuItem em = new JMenuItem("Eisenberg/Mcguire");
        addMenuItemListener_Function(em, "EM");
        criticalSec.add(peterson);
        criticalSec.add(dekker);
        criticalSec.add(lamport);
        criticalSec.add(em);
        function.add(criticalSec);
        return function;
    }
    private void addMenuItemListener_Function(JMenuItem jmi, String dst) {
        jmi.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if(presentInterface == dst) {
                    return;
                }
                if(presentInterface == "None" || JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(frame, "Be Sure to switch? Current configuration will lose.", "Error", JOptionPane.YES_NO_OPTION)) {
                    switchInterface(dst);
                }
            }
        });
    }
    private void switchInterface(String dst) {
        mainUI.removeAll();
        mainUI.add(getMainUI(dst));
        mainUI.updateUI();
        presentInterface = dst;
    }
    private JPanel getMainUI(String str) {
        if(str == "Peterson") {
            return new PetersonInterface().getInterface();
        }else if(str == "Dekker") {
            return new DekkerInterface().getInterface();
        }else if(str == "Lamport") {
            return new LamportInterface().getInterface();
        }else if(str == "EM") {
            return new EMInterface().getInterface();
        }
        return new JPanel();
    }
    private JFrame frame;
    private JPanel mainUI;
    private JMenuBar topMenu;
    private String presentInterface;
}




abstract class Process {
    final int id;
    int remainTime = 0; //critical sec time(ms)
    String state, position;
    static int idMax = 0;
    int EnSIdx = -1, ExSIdx = -1;
    Process(String tstate) {
        id = idMax++;
        position = "Other";
        state = tstate;
    }
    public void changePosition(String newPosition) {
        position = newPosition;
    }
    public void setRemainTime(int trt) {
        remainTime = trt;
    }
    void changeState(String newState) {
        state = newState;
    }
    Vector<String> getInfo() {
        Vector<String> v = new Vector<String>();
        v.add(String.valueOf(id)); //id
        v.add(state); //state
        v.add(position);//position
        v.add(String.format("%.2f", remainTime/1000f)); //CS remainTime
        return v;
    }
    abstract Boolean checkCSCondition(JTextArea logging); //ok to enter critical section?
    abstract void requestCS(int lb, int ub, JTextArea logging);
    abstract boolean requestCS(int time, boolean singleStep, JTextArea logging);
    abstract void leaveCS(boolean singleStep, JTextArea logging);
}
class PetersonProcess extends Process{
    static int flag[] = new int[2];
    static int turn = 0;
    PetersonProcess(String tstate) {
        super(tstate);
    }
    Boolean checkCSCondition(JTextArea logging) {
        if(flag[1-id] == 1 && turn == 1-id) {
            return false;
        }
        logging.append("Process " + id + " enter critical section\n");
        position = "CS";
        return true;
    }
    void requestCS(int lb, int ub, JTextArea logging) {
        // System.out.println("" + lb+ " " + ub + " " + id);
        if(position == "Other") {
            position = "EnS"; // Entry Section
            flag[id] = 1;
            turn = 1 - id;
            lb = lb / 1000;
            ub = ub / 1000;
            int time = (int)(lb + (ub-lb) * Math.random());
            setRemainTime(1000 * time);
            // System.out.println(remainTime);
            logging.append("Process " + id + " request critical section for " + time + "s\n");
            if(!checkCSCondition(logging)) {// busy waiting
                logging.append("Process " + id + " is busy waiting\n");
                changeState("Busy Waiting");
            }
        }
    }
    boolean requestCS(int time, boolean singleStep, JTextArea logging) {
        if(position != "Other") { //already in cs,request failed
            return false;
        }
        if(!singleStep){
            position = "EnS"; // Entry Section
            flag[id] = 1;
            turn = 1-id;
            setRemainTime(1000 * time);
            logging.append("Process " + id + " request critical section for " + time + "s\n");
            if(!checkCSCondition(logging)) {
                logging.append("Process " + id + " is busy waiting\n");
                changeState("Busy Waiting");
            }
        }else {
            position = "EnS";
            EnSIdx = 0;
            setRemainTime(1000 * time);
            logging.append("Process " + id + " request critical section for " + time + "s\n");
        }
        return true;
    }
    void leaveCS(boolean singleStep, JTextArea logging) {
        if(!singleStep){
            position = "ExS";
            flag[id] = 0;
            position = "Other";
            logging.append("Process " + id + " leave critical section\n");
        }else {
            position = "ExS";
            ExSIdx = 0;
        }
    }
    public Vector<String> getInfo() {
        Vector<String> v = super.getInfo();
        v.add(String.valueOf(flag[id])); //flag[id]
        v.add(String.valueOf(turn)); // turn
        return v;
    }
}
class DekkerProcess extends Process{
    static int flag[] = new int[2];
    static int turn = 0;
    boolean first, second;
    DekkerProcess(String tstate) {
        super(tstate);
        first = second = false;
    }
    Boolean checkCSCondition(JTextArea logging) {
        if(!first && !second) {
            if(flag[1-id] == 1) { 
                if(turn == id){ // busy waiting in first loop
                    return false;
                }else {
                    first = true; //enter if
                    flag[id] = 0;
                }
            }else { //entry Section is empty
                first = second = true;
                return true;
            }
        }
        if(first && !second) { //after if, before while(turn == 1-id)
            if(turn == 1-id) {
                return false;
            }
            flag[id] = 1; 
            second = true;//enter CS
        }
        logging.append("Process " + id + " enter critical section\n");
        position = "CS";
        return true;
    }
    void requestCS(int lb, int ub, JTextArea logging) {
        if(position == "Other") {//request critical sec
            position = "EnS"; // Entry Section
            flag[id] = 1;
            lb = lb / 1000;
            ub = ub / 1000;
            int time = (int)(lb + (ub-lb) * Math.random());
            setRemainTime(1000 * time);
            logging.append("Process " + id + " request critical section for " + time + "s\n");
            if(!checkCSCondition(logging)) {// busy waiting
                logging.append("Process " + id + " is busy waiting\n");
                changeState("Busy Waiting");
            }
        }
    }
    boolean requestCS(int time,  boolean singleStep, JTextArea logging) {
        if(position != "Other") { //already in cs,request failed
            return false;
        }
        if(!singleStep){
            position = "EnS"; // Entry Section
            flag[id] = 1;
            remainTime = time * 1000;
            logging.append("Process " + id + " request critical section for " + time + "s\n");
            if(!checkCSCondition(logging)) {
                logging.append("Process " + id + " is busy waiting\n");
                changeState("Busy Waiting");
            }
        }else {
            position = "EnS";
            EnSIdx = 0;
            setRemainTime(1000*time);
            logging.append("Process " + id + " request critical section for " + time + "s\n");
        }
        return true;
    }
    void leaveCS(boolean singleStep, JTextArea logging) {
        if(!singleStep){
            position = "ExS";
            turn = 1 - id;
            flag[id] = 0;
            first = second = false;
            position = "Other";
            logging.append("Process " + id + " leave critical section\n");
        }else {
            position = "ExS";
            ExSIdx = 0;
        }
    }
    public Vector<String> getInfo() {
        Vector<String> v = super.getInfo();
        v.add(String.valueOf(flag[id])); //flag[id]
        v.add(String.valueOf(turn)); // turn
        return v;
    }
}
class LamportProcess extends Process{
    static boolean choosing[] = new boolean[26];
    static int number[] = new int[26];
    LamportProcess(String tstate) {
        super(tstate);
    }
    Boolean checkCSCondition(JTextArea logging) {
        for(int i = 0; i < Process.idMax; i++) {
            if(choosing[i] == true) {
                return false;
            }
            if(number[i] != 0 && ( (number[i] < number[id]) | ((number[i] == number[id]) && (i < id)) )) {
                return false;
            }
        }
        logging.append("Process " + id + " enter critical section\n");
        position = "CS";
        return true;
    }
    void requestCS(int lb, int ub, JTextArea logging) {
        if(position == "Other") {
            position = "EnS"; // Entry Section
            choosing[id] = true;
            int maxn = -1;
            for(int i = 0; i < Process.idMax; i++) {
                maxn = Math.max(number[i], maxn);
            }
            number[id] = maxn + 1;
            lb /= 1000;
            ub /= 1000;
            choosing[id] = false;
            int time = (int)(lb + (ub-lb) * Math.random());
            setRemainTime(1000 * time);
            logging.append("Process " + id + " request critical section for " + time + "s\n");
            if(!checkCSCondition(logging)) {// busy waiting
                logging.append("Process " + id + " is busy waiting\n");
                changeState("Busy Waiting");
            }
        }
    }
    boolean requestCS(int time,  boolean singleStep, JTextArea logging) {
        if(position != "Other") { //already in cs,request failed
            return false;
        }
        if(!singleStep){
            position = "EnS"; // Entry Section
            choosing[id] = true;
            int maxn = -1;
            for(int i = 0; i < Process.idMax; i++) {
                maxn = Math.max(number[i], maxn);
            }
            number[id] = maxn + 1;
            choosing[id] = false;
            remainTime = time * 1000;
            logging.append("Process " + id + " request critical section for " + time + "s\n");
            if(!checkCSCondition(logging)) {
                logging.append("Process " + id + " is busy waiting\n");
                changeState("Busy Waiting");
            }
        }else {
            position = "EnS";
            EnSIdx = 0;
            setRemainTime(1000*time);
            logging.append("Process " + id + " request critical section for " + time + "s\n");
        }
        return true;
    }
    void leaveCS(boolean singleStep, JTextArea logging) {
        if(!singleStep){
            position = "ExS";
            number[id] = 0;
            position = "Other";
            logging.append("Process " + id + " leave critical section\n");
        }else {
            position = "ExS";
            ExSIdx = 0;
        }
    }
    public Vector<String> getInfo() {
        Vector<String> v = super.getInfo();
        v.add(String.valueOf(choosing[id])); //choosing[id]
        v.add(String.valueOf(number[id])); // number[id]
        return v;
    }
}
class EMProcess extends Process{
    static int flag[] = new int[26];
    static int turn = 0;
    static final int idle = 0;
    static final int want_in = 1;
    static final int in_cs = 2;
    boolean first, second;
    EMProcess(String tstate) {
        super(tstate);
        first = second = false;
    }
    Boolean checkCSCondition(JTextArea logging) {
        if(!first && !second){
            int i = turn;
            while(i != id) {
                if(flag[i] != idle) {
                    return false; // busy waiting in inner loop
                }else {
                    i = (i + 1) % Process.idMax;
                }
            }
            flag[id] = in_cs;
            first = true;
        }
        if(first && !second){
            int i = 0;
            while(i < Process.idMax && (i == id || flag[i] != in_cs)) {
                i++;
            }
            if(i != Process.idMax) {
                flag[id] = want_in;
                return false; //busy waiting in outer loop
            }
            second = true;
        }
        turn = id;
        logging.append("Process " + id + " enter critical section\n");
        position = "CS";
        return true;
    }
    void requestCS(int lb, int ub, JTextArea logging) {
        if(position == "Other") {
            position = "EnS"; // Entry Section
            flag[id] = want_in;
            lb /= 1000;
            ub /= 1000;
            int time = (int)(lb + (ub-lb) * Math.random());
            setRemainTime(1000 * time);
            logging.append("Process " + id + " request critical section for " + time + "s\n");
            if(!checkCSCondition(logging)) {// busy waiting
                logging.append("Process " + id + " is busy waiting\n");
                changeState("Busy Waiting");
            }
        }
    }
    boolean requestCS(int time, boolean singleStep,  JTextArea logging) {
        if(position != "Other") { //already in cs,request failed
            return false;
        }
        if(!singleStep){
            position = "EnS"; // Entry Section
            flag[id] = want_in;
            remainTime = time * 1000;
            logging.append("Process " + id + " request critical section for " + time + "s\n");
            if(!checkCSCondition(logging)) {
                logging.append("Process " + id + " is busy waiting\n");
                changeState("Busy Waiting");
            }
        }else {
            position = "EnS";
            EnSIdx = 0;
            setRemainTime(1000*time);
            logging.append("Process " + id + " request critical section for " + time + "s\n");
        }
        return true;
    }
    void leaveCS(boolean singleStep, JTextArea logging) {
        if(!singleStep){
            position = "ExS";
            int i = (turn + 1) % Process.idMax;
            while(flag[i] == idle) {
                i = (i + 1) % Process.idMax;
            }
            turn = i;
            flag[id] = idle;
            first = second = false;
            position = "Other";
            logging.append("Process " + id + " leave critical section\n");
        }else {
            position = "ExS";
            ExSIdx = 0;
        }
    }
    public Vector<String> getInfo() {
        Vector<String> v = super.getInfo();
        v.add((flag[id]==0? "idle" : (flag[id] == 1) ? "want_in" : "in_cs")); //flag[id]
        v.add(String.valueOf(turn)); // turn
        return v;
    }
}




abstract class ProcessManagerBase{
    protected Queue<Process> readyQueue;
    protected Process runningProcess;
    private int timeSlice = 5 * 1000;
    private int timeSliceLeft = 5 * 1000;
    private int csTimeLB = 1 * 1000, csTimeUB = 3 * 1000;
    private boolean arcs = false;
    boolean singleStep = false;
    static final int NORMAL_EnS = 0;
    static final int BW_EnS = 1;
    static final int GRANTED_EnS = 2;
    static final int NOTIN_EnS = 3;
    static final int NORMAL_ExS = 0;
    static final int OUT_ExS = 1;
    static final int NOTIN_ExS = 2;
    ProcessManagerBase() {
        Process.idMax = 0;
    }
    int nextEnSInstr(JTextArea logging) {return 0;}
    int nextExSInstr(JTextArea logging) {return 1;}
    public void setARCS(boolean parcs) {
        arcs = parcs;
    }
    public float getTimeSliceLeft() {
        return timeSliceLeft / 1000f;
    }
    public void setCSTimeLB(int lb) {
        csTimeLB = lb;
    }
    public void setCSTimeUB(int ub) {
        csTimeUB = ub;
    }
    public void scheduling(JTextArea logging) {
        timeSliceLeft = timeSlice;
        runningProcess.changeState("Ready");
        logging.append("Process " + runningProcess.id + " leave CPU\n");
        readyQueue.offer(runningProcess);
        runningProcess = readyQueue.poll();
        logging.append("Process " + runningProcess.id + " enter CPU\n");
        if(!singleStep){
            if(runningProcess.position == "EnS") {
                if(runningProcess.checkCSCondition(logging)){
                    runningProcess.changeState("Run");
                }else {
                    logging.append("Process " + runningProcess.id + " is busy waiting\n");
                    runningProcess.changeState("Busy Waiting");
                }
            }else {
                runningProcess.changeState("Run");
            }
        }else {
            if(runningProcess.position == "EnS") {
                if(nextEnSInstr(logging) == BW_EnS) {
                    runningProcess.changeState("Busy Waiting");
                }
            }else {
                runningProcess.changeState("Run");
            }
        }
    }
    public void setTimeSlice(int ts) {
        timeSlice = ts;
    }
    public void proceed10ms(JTextArea logging) {
        if(!singleStep){
            if(runningProcess.state == "Run"){
                if(runningProcess.position == "CS") { //in critical section
                    runningProcess.remainTime -= 10;
                    // System.out.println(runningProcess.remainTime);
                    if(runningProcess.remainTime == 0) { //out critical section
                        runningProcess.leaveCS(singleStep,logging);
                    }
                }else {
                    if(arcs){
                        runningProcess.requestCS(csTimeLB, csTimeUB, logging); //request critical section at the beginning
                        if(runningProcess.position == "CS") {
                            runningProcess.remainTime -= 10;
                        }
                    }
                }
            }else if(runningProcess.state == "Busy Waiting") {
                if(runningProcess.checkCSCondition(logging)) { //check entrance condition
                    runningProcess.state = "Run";
                }
            }
            timeSliceLeft -= 10;
            if(timeSliceLeft == 0) {
                scheduling(logging);
            }
        }else {
            if(runningProcess.position == "ExS" || (runningProcess.position == "EnS" && runningProcess.state != "Busy Waiting")) {
                return;
            }else if(runningProcess.position == "CS") {
                runningProcess.remainTime -= 10;
                if(runningProcess.remainTime == 0) {
                    runningProcess.leaveCS(singleStep, logging);
                }
            }
            timeSliceLeft -= 10;
            if(timeSliceLeft == 0) {
                scheduling(logging);
            }
        }
    }
    public Queue<Process> getReadyQueue() {
        return readyQueue;
    }
    public Process getRunningProcess() {
        return runningProcess;
    }
    public void interrupt(JTextArea logging) {
        scheduling(logging);
    }
    public void addProcess() {}
}
class PetersonProcessManager extends ProcessManagerBase{
    PetersonProcessManager() {
        for(int i = 0; i < 2; i++) {
            PetersonProcess.flag[i] = 0;
        }
        PetersonProcess.turn = 0;
        PetersonProcess p0 = new PetersonProcess("Ready"), p1 = new PetersonProcess("Run");
        readyQueue = new LinkedList<Process>();
        readyQueue.offer(p0);
        runningProcess = p1;
    }
    int nextEnSInstr(JTextArea logging) {
        if(runningProcess.EnSIdx == 0) {
            logging.append("Process" + runningProcess.id + ": flag[" + runningProcess.id + "] = 1\n");
            PetersonProcess.flag[runningProcess.id] = 1;
            runningProcess.EnSIdx++;
            return NORMAL_EnS;
        }else if(runningProcess.EnSIdx == 1) {
            logging.append("Process" + runningProcess.id + ": turn = " + (1-runningProcess.id) + "\n");
            PetersonProcess.turn = 1-runningProcess.id;
            runningProcess.EnSIdx++;
            return NORMAL_EnS;
        }
        else if(runningProcess.EnSIdx == 2) {
            logging.append("Process" + runningProcess.id + ": while(flag[1-id]==1&&turn==1-id)\n");
            if(PetersonProcess.flag[1-runningProcess.id] == 1 && PetersonProcess.turn == 1-runningProcess.id) {
                logging.append("Process" + runningProcess.id + " is busy waiting\n");
                runningProcess.changeState("Busy Waiting");
                return BW_EnS;
            }
            logging.append("Process" + runningProcess.id + " enter critical section\n");
            runningProcess.changePosition("CS");
            runningProcess.changeState("Run");
            runningProcess.EnSIdx=-1;
            return GRANTED_EnS;
        }else {
            return NOTIN_EnS;
        }
    }
    int nextExSInstr(JTextArea logging) {
        if(runningProcess.ExSIdx == 0) {
            logging.append("Process" + runningProcess.id + ": flag[" + runningProcess.id + "] = 0\n");
            PetersonProcess.flag[runningProcess.id] = 0;
            runningProcess.ExSIdx = -1;
            logging.append("Process" + runningProcess.id + " leave critical section\n");
            runningProcess.changePosition("Other");
            return OUT_ExS;
        }else {
            return NOTIN_ExS;
        }
    }
}
class DekkerProcessManager extends ProcessManagerBase{
    DekkerProcessManager() {
        for(int i = 0; i < 2; i++) {
            DekkerProcess.flag[i] = 0;
        }
        DekkerProcess.turn = 0;
        DekkerProcess p0 = new DekkerProcess("Ready"), p1 = new DekkerProcess("Run");
        readyQueue = new LinkedList<Process>();
        readyQueue.offer(p0);
        runningProcess = p1;
    }
    int nextEnSInstr(JTextArea logging) {
        switch (runningProcess.EnSIdx) {
            case 0:
                logging.append("Process " + runningProcess.id + ": flag[id] = 1\n");
                DekkerProcess.flag[runningProcess.id] = 1;
                runningProcess.EnSIdx++;
                return NORMAL_EnS;
            case 1:
                logging.append("Process " + runningProcess.id + ": while(flag[1-id] = 1\n");
                if(DekkerProcess.flag[1-runningProcess.id] == 0) { //critical sec is empty
                    runningProcess.EnSIdx = -1;
                    runningProcess.changeState("Run");
                    runningProcess.changePosition("CS");
                    return GRANTED_EnS;
                }else {
                    runningProcess.EnSIdx++;
                    return NORMAL_EnS;
                }
            case 2:
                logging.append("Process " + runningProcess.id + ": if(turn == 1-id)\n");
                if(DekkerProcess.turn == 1-runningProcess.id) {
                    runningProcess.EnSIdx++;
                    runningProcess.changeState("Run");
                    return NORMAL_EnS;
                }else {
                    logging.append("Process " + runningProcess.id + " is busy waiting\n");
                    runningProcess.changeState("Busy Waiting");
                    return BW_EnS;
                }
            case 3:
                logging.append("Process " + runningProcess.id + ": flag[id] = 0\n");
                DekkerProcess.flag[runningProcess.id] = 0;
                runningProcess.EnSIdx++;
                return NORMAL_EnS;
            case 4:
                logging.append("Process " + runningProcess.id + ": while(turn==1-id)\n");
                if(DekkerProcess.turn == 1-runningProcess.id) {
                    logging.append("Process " + runningProcess.id + " is busy waiting\n");
                    runningProcess.changeState("Busy Waiting");
                    return BW_EnS;
                }else {
                    runningProcess.changeState("Run");
                    runningProcess.EnSIdx++;
                    return NORMAL_EnS;
                }
            case 5:
                logging.append("Process " + runningProcess.id + ": flag[id] = 1\n");
                DekkerProcess.flag[runningProcess.id] = 1;
                runningProcess.EnSIdx++;
                return NORMAL_EnS;
            case 6:
                logging.append("Process " + runningProcess.id + ": while(flag[1-id] = 1\n");
                if(DekkerProcess.flag[1-runningProcess.id] == 1) {
                    runningProcess.EnSIdx = 2;
                    logging.append("Process " + runningProcess.id + " is busy waiting\n");
                    runningProcess.changeState("Busy Waiting");
                    return BW_EnS;
                }else {
                    runningProcess.EnSIdx = -1;
                    logging.append("Process " + runningProcess.id + " enter critical section\n");
                    runningProcess.changeState("Run");
                    runningProcess.changePosition("CS");
                    return GRANTED_EnS;
                }
            default:
                return NOTIN_EnS;
        }
    }
    int nextExSInstr(JTextArea logging) {
        switch (runningProcess.ExSIdx) {
            case 0:
                logging.append("Process " + runningProcess.id + ": turn = 1-id\n");
                DekkerProcess.turn = 1-runningProcess.id;
                runningProcess.ExSIdx++;
                return NORMAL_ExS;
            case 1:
                logging.append("Process " + runningProcess.id + ": flag[id] = 0\n");
                DekkerProcess.flag[runningProcess.id] = runningProcess.id;
                runningProcess.ExSIdx=-1;
                logging.append("Process " + runningProcess.id + " leave critical section\n");
                runningProcess.changePosition("Other");
                return OUT_ExS;
            default:
                return NOTIN_ExS;
        }
    }
}
class LamportProcessManager extends ProcessManagerBase{
    LamportProcessManager() {
        for(int i = 0; i < 26; i++) {
            LamportProcess.choosing[i] = false;
            LamportProcess.number[i] = 0;
        }
        readyQueue = new LinkedList<Process>();
        readyQueue.offer(new LamportProcess("Ready"));
        readyQueue.offer(new LamportProcess("Ready")); //p0, p1 wait
        runningProcess = new LamportProcess("Run"); //p2 run
    }
    int j;
    int maxn;
    int nextEnSInstr(JTextArea logging) {
        switch (runningProcess.EnSIdx) {
            case 0:
                logging.append("Process " + runningProcess.id + ": choosing[id] = true\n");
                LamportProcess.choosing[runningProcess.id] = true;
                runningProcess.EnSIdx++;
                maxn = 0;
                return NORMAL_EnS;
            case 1:
                for(int i = 0; i < LamportProcess.idMax; i++) {
                    maxn = Math.max(LamportProcess.number[i], maxn);
                }
                maxn++;
                logging.append("Process " + runningProcess.id + ": max{number[i]} + 1 = " + maxn + "\n");
                runningProcess.EnSIdx++;
                return NORMAL_EnS;
            case 2:
                logging.append("Process " + runningProcess.id + ": number[id] = max{number[i]} + 1\n");
                LamportProcess.number[runningProcess.id] = maxn;
                runningProcess.EnSIdx++;
                return NORMAL_EnS;
            case 3:
                logging.append("Process " + runningProcess.id + ": choosing[id] = false\n");
                LamportProcess.choosing[runningProcess.id] = false;
                runningProcess.EnSIdx++;
                j=0;
                return NORMAL_EnS;
            case 4:
                logging.append("Process " + runningProcess.id + ": while(choosing[" + j + "])\n");
                if(LamportProcess.choosing[j] == true) {
                    logging.append("Process " + runningProcess.id + " is busy waiting\n");
                    runningProcess.changeState("Busy Waiting");
                    return BW_EnS;
                }else {
                    runningProcess.changeState("Run");
                    runningProcess.EnSIdx++;
                    return NORMAL_EnS;
                }
            case 5:
                logging.append("Process " + runningProcess.id + ": number[" + j + "]!=0 && (number[j],j)<(number[i],i)\n");
                if(LamportProcess.number[j] != 0 && (LamportProcess.number[j] < LamportProcess.number[runningProcess.id] || (LamportProcess.number[j] == LamportProcess.number[runningProcess.id] && j < runningProcess.id))) {
                    logging.append("Process " + runningProcess.id + " is busy waiting\n");
                    runningProcess.changeState("Busy Waiting");
                    return BW_EnS;
                }else {
                    runningProcess.changeState("Run");
                    runningProcess.EnSIdx++;
                    return NORMAL_EnS;
                }
            case 6:
                logging.append("Process " + runningProcess.id + ": j++\n");
                if(j < LamportProcess.idMax-1) {
                    runningProcess.EnSIdx = 4;
                    j++;
                    return NORMAL_EnS;
                }else {
                    runningProcess.EnSIdx = -1;
                    logging.append("Process " + runningProcess.id + " enter critical section\n");
                    runningProcess.changeState("Run");
                    runningProcess.changePosition("CS");
                    return GRANTED_EnS; 
                }
            default:
                return NOTIN_EnS;
        }
    }
    int nextExSInstr(JTextArea logging) {
        switch (runningProcess.ExSIdx) {
            case 0:
                LamportProcess.number[runningProcess.id] = 0;
                runningProcess.ExSIdx = -1;
                runningProcess.changePosition("Other");
                logging.append("Process " + runningProcess.id + " leave critical section\n");
                return OUT_ExS;
            default:
                return NOTIN_ExS;
        }
    }
    public void addProcess() {
        readyQueue.offer(new LamportProcess("Ready"));
    }
}
class EMProcessManager extends ProcessManagerBase{
    EMProcessManager() {
        for(int i = 0; i < 26; i++) {
            EMProcess.flag[i] = EMProcess.idle;
        }
        EMProcess.turn = 0;
        readyQueue = new LinkedList<Process>();
        readyQueue.offer(new EMProcess("Ready"));
        readyQueue.offer(new EMProcess("Ready")); //p0, p1 wait
        runningProcess = new EMProcess("Run"); //p2 run
    }
    int j;
    int nextEnSInstr(JTextArea logging) {
        switch (runningProcess.EnSIdx) {
            case 0:
                logging.append("Process " + runningProcess.id + " : " + "flag[id] = want_in\n");
                EMProcess.flag[runningProcess.id] = EMProcess.want_in;
                runningProcess.EnSIdx++;
                return NORMAL_EnS;
            case 1:
                logging.append("Process " + runningProcess.id + " : " + "j = turn\n");
                j = EMProcess.turn;
                runningProcess.EnSIdx++;
                return NORMAL_EnS;
            case 2:
                logging.append("Process " + runningProcess.id + " : " + "while(j!=id)\n");
                if(j == runningProcess.id) {
                    runningProcess.EnSIdx = 5;
                    return NORMAL_EnS;
                }else {
                    runningProcess.EnSIdx++;
                    return NORMAL_EnS;
                }
            case 3:
                logging.append("Process " + runningProcess.id + " : " + "if(flag[" + j + "] != idle) then j = turn\n");
                if(EMProcess.flag[j] != EMProcess.idle) {
                    j = EMProcess.turn;
                    runningProcess.EnSIdx = 2;
                    logging.append("Process " + runningProcess.id + " is busy waiting\n");
                    runningProcess.changeState("Busy Waiting");
                    return BW_EnS;
                }else {
                    runningProcess.changeState("Run");
                    runningProcess.EnSIdx++;
                    return NORMAL_EnS;
                }
            case 4:
                logging.append("Process " + runningProcess.id + " : j=(j+1)%idMax\n");
                j = (j + 1) % Process.idMax;
                runningProcess.EnSIdx = 2;
                return NORMAL_EnS;
            case 5:
                logging.append("Process " + runningProcess.id + " : flag[id] = in_cs\n");
                EMProcess.flag[runningProcess.id] = EMProcess.in_cs;
                j = 0;
                runningProcess.EnSIdx++;
                return NORMAL_EnS;
            case 6:
                logging.append("Process " + runningProcess.id + " : while(j < idMax && (j == id || flag[j] == in_cs)\n");
                while(j < Process.idMax && (j == runningProcess.id || EMProcess.flag[j] != EMProcess.in_cs)) {
                    j++;
                }
                runningProcess.EnSIdx++;
                return NORMAL_EnS;
            case 7:
                logging.append("Process " + runningProcess.id + " : while(j != idMax)\n");
                if(j == Process.idMax) {
                    runningProcess.EnSIdx++;
                    runningProcess.changeState("Run");
                    return NORMAL_EnS;
                }else {
                    runningProcess.EnSIdx = 0;
                    logging.append("Process " + runningProcess.id + " is busy waiting\n");
                    runningProcess.changeState("Busy Waiting");
                    return BW_EnS;
                }
            case 8:
                logging.append("Process " + runningProcess.id + " : turn = id\n");
                EMProcess.turn = runningProcess.id;
                logging.append("Process " + runningProcess.id + " enter critical section\n");
                runningProcess.changePosition("CS");
                runningProcess.changeState("Run");
                runningProcess.EnSIdx = -1;
                return GRANTED_EnS;
            default:
                return NOTIN_EnS;
        }
    }
    int nextExSInstr(JTextArea logging) {
        switch (runningProcess.ExSIdx) {
            case 0:
                logging.append("Process " + runningProcess.id + " : j = (turn+1) % idMax\n");
                j = (EMProcess.turn + 1) % Process.idMax;
                runningProcess.ExSIdx++;
                return NORMAL_ExS;
            case 1:
                logging.append("Process " + runningProcess.id + " : while(flag[j]==idle)\n");
                if(EMProcess.flag[j] == EMProcess.idle) {
                    runningProcess.ExSIdx++;
                    return NORMAL_ExS;
                }else {
                    runningProcess.ExSIdx = 3;
                    return NORMAL_ExS;
                }
            case 2:
                logging.append("Process " + runningProcess.id + " : j=(j+1)%idMax\n");
                j = (j + 1) % Process.idMax;
                runningProcess.ExSIdx++;
                return NORMAL_ExS;
            case 3:
                logging.append("Process " + runningProcess.id + " : turn=j\n");
                EMProcess.turn = j;
                runningProcess.ExSIdx++;
                return NORMAL_ExS;
            case 4:
                logging.append("Process " + runningProcess.id + " : flag[id]=idle\n");
                EMProcess.flag[j] = EMProcess.idle;
                logging.append("Process " + runningProcess.id + " leave critical section\n");
                runningProcess.changePosition("Other");
                runningProcess.ExSIdx = -1;
                return OUT_ExS;
            default:
                return NOTIN_ExS;
        }
    }
    public void addProcess() {
        readyQueue.offer(new EMProcess("Ready"));
    }
}






abstract class mutexAlgoSInterface{
    protected void initPanel(JPanel jp, JPanel displayField, JPanel controlPanel) {
        jp.setLayout(new BorderLayout());
        displayField.setPreferredSize(new Dimension(Constant.WIN_WIDTH * 4/5, Constant.WIN_HEIGHT));
        displayField.setBorder(BorderFactory.createLineBorder(Color.black));
        displayField.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
        // displayField.setBackground(Color.BLUE);
        controlPanel.setPreferredSize(new Dimension(Constant.WIN_WIDTH / 5, Constant.WIN_HEIGHT));
        controlPanel.setBorder(BorderFactory.createLineBorder(Color.black));
        controlPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
        controlPanel.setBackground(Color.LIGHT_GRAY);
        jp.add(displayField, BorderLayout.CENTER);
        jp.add(controlPanel, BorderLayout.EAST);
    }
    protected void controlPanelCreateTitle(Box cpMainBox, String algoTypeStr) {
        Box titleBox = Box.createHorizontalBox();
        JLabel title = new JLabel("Control Panel", JLabel.CENTER);
        title.setFont(new Font("Series", Font.BOLD, 28));
        titleBox.add(Box.createHorizontalGlue());
        titleBox.add(title);
        titleBox.add(Box.createHorizontalGlue());
        cpMainBox.add(Box.createVerticalStrut(10));
        cpMainBox.add(titleBox);
        cpMainBox.add(Box.createVerticalStrut(10));
        
        Box algoTypeBox = Box.createHorizontalBox();
        cpMainBox.add(algoTypeBox);
        cpMainBox.add(Box.createVerticalStrut(10));
        JLabel algoType = new JLabel(algoTypeStr , JLabel.RIGHT);
        algoType.setFont(new Font("Series", Font.PLAIN, 15));
        algoTypeBox.add(Box.createHorizontalGlue());
        algoTypeBox.add(algoType);
        algoTypeBox.add(Box.createHorizontalGlue());
    }
    protected Box createProcessBox(String info) {
        Box processBox = Box.createHorizontalBox();
        processBox.setBorder(BorderFactory.createLineBorder(Color.black, 2));
        JLabel jb = new JLabel(info, JLabel.CENTER);
        processBox.add(jb);
        return processBox;
    }
    protected JLabel createTimeSliceLabel(String timeSliceLeft) {
        JLabel jb = new JLabel("TSLR:" + timeSliceLeft + "s", JLabel.CENTER);
        jb.setFont(new Font("Series", Font.PLAIN, 18));
        return jb;
    }
    protected void displayFieldInit(JPanel displayField) {
        Box totBoxDF = Box.createHorizontalBox();
        displayField.add(totBoxDF);
        // totBoxDF.setBorder(BorderFactory.createLineBorder(Color.green, 3));
        
        //ready queue init
        Box readyQueueBox = Box.createVerticalBox();
        totBoxDF.add(Box.createRigidArea(new Dimension(5, Constant.WIN_HEIGHT)));
        totBoxDF.add(readyQueueBox);
        totBoxDF.add(Box.createRigidArea(new Dimension(5, Constant.WIN_HEIGHT)));
        Box readyQueueTitleBox = Box.createHorizontalBox();
        // readyQueueTitleBox.setBorder(BorderFactory.createLineBorder(Color.yellow, 3));
        readyQueueBox.add(Box.createVerticalStrut(10));
        readyQueueBox.add(readyQueueTitleBox);
        readyQueueBox.add(Box.createVerticalStrut(10));
        JLabel readyQueueTitle = new JLabel("Ready Queue", JLabel.CENTER);
        readyQueueTitle.setFont(new Font("Series", Font.BOLD, 25));
        readyQueueTitleBox.add(readyQueueTitle);
        // readyQueueBox.setBorder(BorderFactory.createLineBorder(Color.blue, 3));

        //ready queue Content
        Box processBoxRQ = Box.createVerticalBox();
        readyQueueBox.add(processBoxRQ);
        processPanelReady = new JPanel();
        processPanelReady.setOpaque(false);
        processPanelReady.setLayout(new GridLayout(5, 5, 10, 10));
        // processPanelReady.setBorder(BorderFactory.createLineBorder(Color.red, 3));
        processBoxRQ.add(processPanelReady);
        
        
        
        //Margin
        totBoxDF.add(Box.createHorizontalStrut(30));
        
        //Running init
        Box runBox = Box.createVerticalBox();
        totBoxDF.add(runBox);
        totBoxDF.add(Box.createHorizontalStrut(5));
        JLabel runTitle = new JLabel(" CPU ", JLabel.CENTER);
        runTitle.setFont(new Font("Series", Font.BOLD, 25));
        Box runTitleBox = Box.createHorizontalBox();
        runTitleBox.add(Box.createHorizontalGlue());
        runTitleBox.add(runTitle);
        runTitleBox.add(Box.createHorizontalGlue());
        runBox.add(Box.createVerticalStrut(10));
        runBox.add(runTitleBox);
        runBox.add(Box.createVerticalStrut(10));
        processPanelRun = new JPanel();
        processPanelRun.setOpaque(false);
        runBox.add(processPanelRun);
        runBox.add(Box.createVerticalGlue());
    }
    public abstract JPanel getInterface();
    protected void update(ProcessManagerBase pmb) {
        Queue<Process> readyProArr = pmb.getReadyQueue();
        processPanelReady.removeAll();
        for (Process process : readyProArr) {
            addReadyProcess(parseInfo(process.getInfo()));
        }
        processPanelReady.updateUI();

        processPanelRun.removeAll();
        Box processPanelRunBox = Box.createVerticalBox();
        processPanelRun.add(processPanelRunBox);
        processPanelRunBox.add(createTimeSliceLabel(String.format("%.2f", pmb.getTimeSliceLeft())));
        processPanelRunBox.add(Box.createVerticalStrut(20));
        processPanelRunBox.add(createProcessBox(parseInfo(pmb.getRunningProcess().getInfo())));
        processPanelRun.updateUI();
    }
    protected abstract String parseInfo(Vector<String> infoItems);

    protected void initInterface(String algoName, String mode) {
        //Overall panel
        jp = new JPanel();
        JPanel displayField = new JPanel();
        JPanel controlPanel = new JPanel();
        initPanel(jp, displayField, controlPanel);

        //------------------------control Panel---------------------
        //control panel init
        Box totBoxCP = Box.createHorizontalBox();
        totBoxCP.setPreferredSize(controlPanel.getPreferredSize());
        controlPanel.add(totBoxCP);
        totBoxCP.add(Box.createHorizontalGlue());
        Box cpMainBox = Box.createVerticalBox();
        totBoxCP.add(cpMainBox);
        totBoxCP.add(Box.createHorizontalGlue());
        controlPanelCreateTitle(cpMainBox, algoName);
        
        //control panel button
        Box boxTS = Box.createHorizontalBox();
        cpMainBox.add(boxTS);
        cpMainBox.add(Box.createVerticalStrut(20));
        JLabel timeSlice = new JLabel("TSL(s)", JLabel.LEFT);
        JLabel timeSliceNum = new JLabel("5", JLabel.CENTER); //default timeslice length
        JButton timeSliceEdit = new JButton("Edit");
        timeSliceEdit.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String newTimeSliceNum = JOptionPane.showInputDialog(controlPanel, "Please enter new time slice length");
                if(newTimeSliceNum != null && !newTimeSliceNum.isEmpty()) {
                    try{
                        int i = Integer.parseInt(newTimeSliceNum);
                        if(i <= 0) {
                            throw new NumberFormatException();
                        }
                        pmb.setTimeSlice(i * 1000);
                        timeSliceNum.setText(newTimeSliceNum);
                        logArea.append("New TSL is " + newTimeSliceNum + "s\n");
                    }catch(NumberFormatException exception){
                        JOptionPane.showMessageDialog(controlPanel, "Please enter a positive integer", "Error",  JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });
        boxTS.add(timeSlice);
        boxTS.add(Box.createHorizontalGlue());
        boxTS.add(timeSliceNum);
        boxTS.add(Box.createHorizontalGlue());
        boxTS.add(timeSliceEdit);
        
        Box boxCSTR = Box.createHorizontalBox();
        cpMainBox.add(boxCSTR);
        cpMainBox.add(Box.createVerticalStrut(20));
        JLabel csTimeRange = new JLabel("CSTR(s)", JLabel.LEFT);
        JLabel csTimeRangeLB = new JLabel("1");
        JLabel csTimeRangeSep = new JLabel("-");
        JLabel csTimeRangeUB = new JLabel("3");
        JButton csTimeRangeEdit = new JButton("Edit");
        csTimeRangeEdit.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String newLB = JOptionPane.showInputDialog(controlPanel, "Please enter new lower bound of CSTR", csTimeRangeLB.getText());
                String newUB = JOptionPane.showInputDialog(controlPanel, "Please enter new upper bound of CSTR", csTimeRangeUB.getText());
                try{
                    int newLBi = -1, newUBi = -1;
                    if(newLB != null) { //Cancel -> unchange
                        newLBi = Integer.parseInt(newLB);
                    }else {
                        newLBi = Integer.parseInt(csTimeRangeLB.getText());
                    }
                    if(newUB != null) { //Cancel -> unchange
                        newUBi = Integer.parseInt(newUB);
                    }else {
                        newUBi = Integer.parseInt(csTimeRangeUB.getText());
                    }
                    if(newLBi <= 0 || newUBi <= 0 || newLBi > newUBi) {
                        throw new NumberFormatException();
                    }
                    pmb.setCSTimeLB(newLBi * 1000);
                    pmb.setCSTimeUB(newUBi * 1000);
                    logArea.append("New CSTR is " + newLBi + "-" + newUBi + "s\n");
                    csTimeRangeLB.setText(newLBi+"");
                    csTimeRangeUB.setText(newUBi+"");
                }catch(NumberFormatException exception) {
                    JOptionPane.showMessageDialog(controlPanel, "Please enter positive integers, and make sure lower bound is less than or equal to upper bound", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        boxCSTR.add(csTimeRange);
        boxCSTR.add(Box.createHorizontalGlue());
        boxCSTR.add(csTimeRangeLB);
        boxCSTR.add(csTimeRangeSep);
        boxCSTR.add(csTimeRangeUB);
        boxCSTR.add(Box.createHorizontalGlue());
        boxCSTR.add(csTimeRangeEdit);

        Box boxARCS = Box.createHorizontalBox();
        cpMainBox.add(boxARCS);
        cpMainBox.add(Box.createVerticalStrut(20));
        JRadioButton arcs = new JRadioButton("ARCS");
        arcs.setBackground(Color.LIGHT_GRAY);
        arcs.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                if(e.getStateChange() == ItemEvent.SELECTED) {
                    pmb.setARCS(true);
                }else {
                    pmb.setARCS(false);
                }
            }
        });
        JRadioButton ssrb = new JRadioButton("SS");
        ssrb.setBackground(Color.LIGHT_GRAY);
        ssrb.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                if(e.getStateChange() == ItemEvent.SELECTED) {
                    pmb.singleStep = true;
                }else {
                    pmb.singleStep = false;
                }
            }
        });
        boxARCS.add(Box.createHorizontalGlue());
        boxARCS.add(arcs);
        boxARCS.add(Box.createHorizontalGlue());
        boxARCS.add(ssrb);
        boxARCS.add(Box.createHorizontalGlue());
        

        Box enterCSBox = Box.createHorizontalBox();
        cpMainBox.add(enterCSBox);
        cpMainBox.add(Box.createVerticalStrut(20));
        JButton enterCS = new JButton("Request CS");
        enterCS.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if(pmb.getRunningProcess().getInfo().get(2) == "Other"){ //not in critical Sec
                    String csTimeStr = JOptionPane.showInputDialog(controlPanel, "Please enter time for occupying critial section");
                    int csTime = -1;
                    if(csTimeStr == null) {
                        return;
                    }
                    try {
                        csTime = Integer.parseInt(csTimeStr);
                        if(csTime <= 0) {
                            throw new NumberFormatException();
                        }
                        pmb.getRunningProcess().requestCS(csTime, pmb.singleStep, logArea);
                        update(pmb);
                    }catch(NumberFormatException exception) {
                        JOptionPane.showMessageDialog(controlPanel, "Please enter a positive integer", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }else {
                    JOptionPane.showMessageDialog(controlPanel, "Already applied for or in critical section", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        enterCSBox.add(Box.createHorizontalGlue());
        enterCSBox.add(enterCS);
        enterCSBox.add(Box.createHorizontalGlue());

        Box intBox = Box.createHorizontalBox();
        cpMainBox.add(intBox);
        cpMainBox.add(Box.createVerticalStrut(20));
        JButton intB = new JButton("Interrupt");
        intB.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                pmb.scheduling(logArea);
                update(pmb);
            }
        });
        intBox.add(Box.createHorizontalGlue());
        intBox.add(intB);
        intBox.add(Box.createHorizontalGlue());

        Box boxSS = Box.createHorizontalBox();
        cpMainBox.add(boxSS);
        cpMainBox.add(Box.createVerticalStrut(20));
        JButton ssb = new JButton("Next Instr");
        ssb.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if(pmb.singleStep){
                    if(pmb.runningProcess.position == "EnS") {
                        pmb.nextEnSInstr(logArea);
                    }else if(pmb.runningProcess.position == "ExS") {
                        pmb.nextExSInstr(logArea);
                    }else {
                        JOptionPane.showMessageDialog(controlPanel, "Running Process must be in Entry Section or Exit Section");
                    }
                    update(pmb);
                }else {
                    JOptionPane.showMessageDialog(controlPanel, "Must in single step mode");
                }
            }
        });
        boxSS.add(Box.createHorizontalGlue());
        boxSS.add(ssb);
        boxSS.add(Box.createHorizontalGlue());

        if(mode == "Multi"){
            Box addPrBox = Box.createHorizontalBox();
            cpMainBox.add(addPrBox);
            cpMainBox.add(Box.createVerticalStrut(20));
            JButton addPr = new JButton("Add Process");
            addPr.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    if(Process.idMax >= 26) {
                        JOptionPane.showMessageDialog(controlPanel, "Too many processes", "Error", JOptionPane.ERROR_MESSAGE);
                    }else {
                        pmb.addProcess();
                        update(pmb);
                    }
                }
            });
            addPrBox.add(Box.createHorizontalGlue());
            addPrBox.add(addPr);
            addPrBox.add(Box.createHorizontalGlue());
        }

        Box boxLog = Box.createHorizontalBox();
        cpMainBox.add(boxLog);
        cpMainBox.add(Box.createVerticalStrut(20));
        logArea = new JTextArea(10, 15);
        logArea.setLineWrap(true);
        logArea.setEditable(false);
        logArea.append("CPU scheduling algorithm: RR\n");
        logArea.append("Default time slice length: 5s\n");
        if(mode == "Multi"){
            logArea.append("Default state: P0:Ready, P1:Ready, P2:Run\n");
        }else {
            logArea.append("Default state: P0:Ready, P1:Run\n");            
        }
        logArea.append("Abbr:\n");
        logArea.append("CS(Critical Section)\n");
        logArea.append("TSL(Time Slice Length)\n");
        logArea.append("CSTR(Critical Section Time Range)\n");
        logArea.append("ARCS(Auto Request Critical Section)\n");
        logArea.append("TSLR(Time Slice Length Remained)\n");
        logArea.append("CSOTR(Critical Section Occupied Time Remained)\n");
        logArea.append("---------------------------------------\n");
        JScrollPane logScrollPane = new JScrollPane(logArea);
        boxLog.add(Box.createHorizontalGlue());
        boxLog.add(logScrollPane);
        boxLog.add(Box.createHorizontalGlue());

        Box boxSpecT = Box.createHorizontalBox();
        cpMainBox.add(Box.createVerticalGlue());
        cpMainBox.add(boxSpecT);
        JButton pcd1s = new JButton("+1s");
        pcd1s.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                for(int i = 0; i < 100; i++) {
                    pmb.proceed10ms(logArea);
                }
                // System.out.println("Proceed 1s");
                update(pmb);
            }
        });
        JButton pcd10ms = new JButton("+0.01s");
        pcd10ms.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                pmb.proceed10ms(logArea);
                update(pmb);
            }
        });
        boxSpecT.add(Box.createHorizontalGlue());
        boxSpecT.add(pcd10ms);
        boxSpecT.add(Box.createHorizontalGlue());
        boxSpecT.add(pcd1s);
        boxSpecT.add(Box.createHorizontalGlue());

        Box autoBox = Box.createHorizontalBox();
        cpMainBox.add(Box.createVerticalStrut(20));
        cpMainBox.add(autoBox);
        cpMainBox.add(Box.createVerticalStrut(50));
        Timer tm = new Timer(1, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                pmb.proceed10ms(logArea);
                update(pmb);
            }
        });
        JButton auto = new JButton("Auto");
        auto.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if(!tm.isRunning()) {
                    tm.start();
                }else {
                    tm.stop();
                }
            }
        });
        autoBox.add(Box.createHorizontalGlue());
        autoBox.add(auto);
        autoBox.add(Box.createHorizontalGlue());


        //--------------------------displayField-----------------------
        displayFieldInit(displayField);
        Queue<Process> readyQueue = pmb.getReadyQueue();
        for (Process pr : readyQueue) {
            processPanelReady.add(createProcessBox(parseInfo(pr.getInfo())));
        }


        processPanelRun.removeAll();
        Box processPanelRunBox = Box.createVerticalBox();
        processPanelRun.add(processPanelRunBox);
        processPanelRunBox.add(createTimeSliceLabel(String.format("%.2f", pmb.getTimeSliceLeft())));
        processPanelRunBox.add(Box.createVerticalStrut(20));
        processPanelRunBox.add(createProcessBox(parseInfo(pmb.getRunningProcess().getInfo())));
        processPanelRun.updateUI();
    }
    public void addReadyProcess(String info) {
        processPanelReady.add(createProcessBox(info));
    }
    protected JPanel jp;
    protected JPanel processPanelReady;
    protected JPanel processPanelRun;
    protected JTextArea logArea;
    protected ProcessManagerBase pmb;
}
class PetersonInterface extends mutexAlgoSInterface{
    public PetersonInterface() {
        pmb = new PetersonProcessManager();
        initInterface("Peterson", "Dual");
        // update();
    }
    public JPanel getInterface() {
        return jp;
    }
    protected String parseInfo(Vector<String> infoItems) {
        String info = "<html><center>Process  " + infoItems.get(0) + "<center><br>State: " + infoItems.get(1) + "<br>Position: " + infoItems.get(2) + "<br>CSOTR: " + infoItems.get(3) + "s<br>flag[" + infoItems.get(0) + "]: " +infoItems.get(4) + "<br>turn: " + infoItems.get(5) + "<html>";
        return info;
    }
}
class DekkerInterface extends mutexAlgoSInterface{
    public DekkerInterface() {
        pmb = new DekkerProcessManager();
        initInterface("Dekker", "Dual");
        // update();
    }
    public JPanel getInterface() {
        return jp;
    }
    protected String parseInfo(Vector<String> infoItems) {
        String info = "<html><center>Process  " + infoItems.get(0) + "<center><br>State: " + infoItems.get(1) + "<br>Position: " + infoItems.get(2) + "<br>CSOTR: " + infoItems.get(3) + "s<br>flag[" + infoItems.get(0) + "]: " +infoItems.get(4) + "<br>turn: " + infoItems.get(5) + "<html>";
        return info;
    }
}
class LamportInterface extends mutexAlgoSInterface{
    public LamportInterface() {
        pmb = new LamportProcessManager();
        initInterface("Lamport", "Multi");
        // update();
    }
    public JPanel getInterface() {
        return jp;
    }
    protected String parseInfo(Vector<String> infoItems) {
        String info = "<html><center>Process  " + infoItems.get(0) + "<center><br>State: " + infoItems.get(1) + "<br>Position: " + infoItems.get(2) + "<br>CSOTR: " + infoItems.get(3) + "s<br>choose[" + infoItems.get(0) + "]: " +infoItems.get(4) + "<br>number[" + infoItems.get(0) + "]: " + infoItems.get(5) + "<html>";
        return info;
    }
}
class EMInterface extends mutexAlgoSInterface{
    public EMInterface() {
        pmb = new EMProcessManager();
        initInterface("Eisenberg/Mcguire", "Multi");
    }
    public JPanel getInterface() {
        return jp;
    }
    protected String parseInfo(Vector<String> infoItems) {
        String info = "<html><center>Process  " + infoItems.get(0) + "<center><br>State: " + infoItems.get(1) + "<br>Position: " + infoItems.get(2) + "<br>CSOTR: " + infoItems.get(3) + "s<br>flag[" + infoItems.get(0) + "]: " +infoItems.get(4) + "<br>turn: " + infoItems.get(5) + "<html>";
        return info;
    }
}