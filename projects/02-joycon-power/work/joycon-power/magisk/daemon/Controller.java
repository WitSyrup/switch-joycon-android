package local.switchpower;

import android.content.Context;
import android.os.PowerManager;
import android.os.Looper;
import java.io.*;
import java.nio.channels.FileLock;
import java.util.*;

/** Root process. No alarms, input grabs, or CPU wake locks. */
public final class Controller {
    static File dir;
    static PowerManager power;
    static volatile boolean active = true;
    static final Map<String,String> leds = new LinkedHashMap<>();
    static final Set<String> detached = new LinkedHashSet<>();
    static final String DRIVER = "/sys/bus/serial/drivers/joycon_serdev/";
    static volatile String mode = "wake";
    static int wakes;
    static boolean ledInventoryLogged;
    static int eventLogs;

    static synchronized void log(String s) {
        try {
            File f = new File(dir, "runtime.log");
            if (f.length() > 65536) f.renameTo(new File(dir, "runtime.previous.log"));
            try (FileWriter w = new FileWriter(f, true)) {
                w.write(new Date() + " " + s + "\n");
            }
        } catch (IOException ignored) {}
    }
    static String read(File f) throws IOException {
        try (BufferedReader r = new BufferedReader(new FileReader(f))) {
            String line = r.readLine();
            return line == null ? "" : line.trim();
        }
    }
    static void write(File f, String text) throws IOException {
        try (FileOutputStream out = new FileOutputStream(f)) {
            out.write((text + "\n").getBytes("UTF-8"));
        }
    }
    static void dark() {
        File[] files = new File("/sys/class/leds").listFiles();
        if (!ledInventoryLogged) {
            log("LED inventory=" + (files == null ? "unreadable" : Arrays.toString(files)));
            ledInventoryLogged = true;
        }
        if (files == null) return;
        for (File led : files) {
            String name = led.getName();
            if (!name.matches("serial[01]-0:(player[1-4]|home)")) continue;
            File value = new File(led, "brightness");
            try {
                String path = value.getAbsolutePath();
                if (!leds.containsKey(path)) leds.put(path, read(value));
                if (!"0".equals(read(value))) write(value, "0");
            } catch (IOException e) { log("LED access failed: " + name + " " + e); }
        }
    }
    static void restore() {
        for (String serial : new ArrayList<>(detached)) {
            try {
                if (!new File(DRIVER + serial).exists()) write(new File(DRIVER + "bind"), serial);
                detached.remove(serial);
                log("rail reconnected: " + serial);
            } catch (IOException e) { log("rail reconnect failed: " + serial + " " + e); }
        }
        Iterator<Map.Entry<String,String>> i = leds.entrySet().iterator();
        while (i.hasNext()) {
            Map.Entry<String,String> entry = i.next();
            File f = new File(entry.getKey());
            try {
                if (f.exists()) write(f, entry.getValue());
                i.remove();
            } catch (IOException e) { log("LED restore failed: " + e); }
        }
    }
    static void disconnect() {
        for (String serial : new String[]{"serial0-0", "serial1-0"}) {
            if (detached.contains(serial) || !new File(DRIVER + serial).exists()) continue;
            try {
                write(new File(DRIVER + "unbind"), serial);
                detached.add(serial);
                log("rail detached: " + serial + "; controller wake unavailable");
            } catch (IOException e) { log("rail detach failed: " + serial + " " + e); }
        }
    }
    static String device() throws IOException {
        String name = "";
        try (BufferedReader r = new BufferedReader(new FileReader("/proc/bus/input/devices"))) {
            String line;
            while ((line = r.readLine()) != null) {
                if (line.startsWith("N:")) name = line;
                if (line.startsWith("H:") && name.contains("Nintendo Switch Combined Joy-Cons")) {
                    for (String field : line.split("\\s+"))
                        if (field.matches("event[0-9]+")) return "/dev/input/" + field;
                }
            }
        }
        return null;
    }
    static void listen() {
        while (active) {
            try {
                String path = device();
                if (path == null) { Thread.sleep(3000); continue; }
                try (DataInputStream in = new DataInputStream(new FileInputStream(path))) {
                    log("input listener ready: " + path);
                    byte[] event = new byte[24]; // arm64 struct input_event
                    while (active) {
                        in.readFully(event);
                        int type = (event[16] & 255) | (event[17] & 255) << 8;
                        int code = (event[18] & 255) | (event[19] & 255) << 8;
                        int value = (event[20] & 255) | (event[21] & 255) << 8
                            | (event[22] & 255) << 16 | (event[23] & 255) << 24;
                        if (eventLogs++ < 8) log("event type=" + type + " code=" + code + " value=" + value);
                        // Linux BTN_MODE=316 is the Joy-Con Home button.
                        if (type != 1 || value != 1 || code != 316) continue;
                        if (!"wake".equals(mode) || power.isInteractive()) continue;
                        // WAKEUP only, never POWER toggle or keyguard dismissal.
                        Process p = new ProcessBuilder("/system/bin/input", "keyevent", "224").start();
                        int result = p.waitFor();
                        wakes++;
                        log("button " + code + " requested wake, result=" + result);
                    }
                }
            } catch (Exception e) {
                log("input retry: " + e);
                try { Thread.sleep(3000); } catch (InterruptedException ignored) {}
            }
        }
    }
    public static void main(String[] args) throws Exception {
        dir = new File(args[0]);
        try (RandomAccessFile lockFile = new RandomAccessFile(new File(dir,"daemon.lock"),"rw")) {
            FileLock lock = lockFile.getChannel().tryLock();
            if (lock == null) return;
            Looper.prepareMainLooper();
            Class<?> at = Class.forName("android.app.ActivityThread");
            Object thread = at.getMethod("systemMain").invoke(null);
            Context context = (Context) at.getMethod("getSystemContext").invoke(thread);
            power = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            log("root daemon started; version=1.2; Home-only wake; no wake lock");
            Thread input = new Thread(Controller::listen, "JoyConInput");
            input.setDaemon(true);
            input.start();
            String previous = "";
            while (active) {
                if (new File(dir,"disable").exists() || new File(dir,"remove").exists()) break;
                String next = read(new File(dir,"mode.conf"));
                if (!next.equals("wake") && !next.equals("disconnect")) next = "wake";
                if (!next.equals(mode)) { restore(); mode = next; }
                boolean awake = power.isInteractive();
                if (awake) restore();
                else if (mode.equals("disconnect")) disconnect();
                else dark();
                String state = "mode=" + mode + "\ninteractive=" + awake
                    + "\nrails_detached=" + detached.size() + "\nwake_requests=" + wakes;
                if (!state.equals(previous)) {
                    write(new File(dir,"status.txt"),state);
                    log(state.replace('\n',' '));
                    previous = state;
                }
                Thread.sleep(3000); // No wakeup alarm: sleeps with the CPU.
            }
            active = false;
            restore();
            log("disabled; rails and LEDs restored");
            lock.release();
        }
    }
}
