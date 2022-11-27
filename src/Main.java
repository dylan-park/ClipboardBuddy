import org.json.JSONArray;
import org.json.JSONObject;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionListener;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {
    static String folderPath = System.getenv().get("APPDATA") + "\\ClipboardBuddy";
    static String filePath = folderPath + "\\rules.json";
    static ArrayList<Rule> rules;
    static Deque<String> history = new LinkedList<>();
    static boolean disabled = false;
    static boolean historyOpen = false;
    static boolean optionsOpen = false;
    static int retryCount = 0;
    // load an image
    static Image defaultImage = Toolkit.getDefaultToolkit().getImage(Main.class.getResource("default.png"));
    static Image disabledImage = Toolkit.getDefaultToolkit().getImage(Main.class.getResource("disabled.png"));

    public static void main(String[] args) throws IOException {
        createDataFile();
        loadDataFile();
        // get the SystemTray instance
        SystemTray tray = SystemTray.getSystemTray();
        TrayIcon trayIcon = new TrayIcon(defaultImage, "ClipboardBuddy");
        CheckboxMenuItem disableMenuItem = new CheckboxMenuItem("Disable");
        MenuItem historyMenuItem = new MenuItem("History");
        MenuItem optionsMenuItem = new MenuItem("Options");
        MenuItem quitMenuItem = new MenuItem("Quit");
        // create an action listener to listen for default action executed on the tray icon
        ActionListener listener = e -> {
            disabled = !disabled;
            if (disabled) {
                disableMenuItem.setState(true);
                trayIcon.setImage(disabledImage);
            }
            if (!disabled) {
                disableMenuItem.setState(false);
                trayIcon.setImage(defaultImage);
            }
        };

        ItemListener disabledListener = e -> {
            disabled = !disabled;
            if (disabled) {
                trayIcon.setImage(disabledImage);
            }
            if (!disabled) {
                trayIcon.setImage(defaultImage);
            }
        };

        ActionListener historyListener = e -> {
        };

        ActionListener optionsListener = e -> {
        };

        ActionListener quitListener = e -> {
            try {
                saveDataFile();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
            System.exit(0);
        };
        // create a popup menu
        PopupMenu popup = new PopupMenu();
        disableMenuItem.addItemListener(disabledListener);
        popup.add(disableMenuItem);
        /// ... add other items
        historyMenuItem.addActionListener(historyListener);
        popup.add(historyMenuItem);
        optionsMenuItem.addActionListener(optionsListener);
        popup.add(optionsMenuItem);
        popup.addSeparator();
        quitMenuItem.addActionListener(quitListener);
        popup.add(quitMenuItem);
        // construct a TrayIcon
        trayIcon.setPopupMenu(popup);
        // set the TrayIcon properties
        trayIcon.addActionListener(listener);
        // ...
        // add the tray image
        try {
            tray.add(trayIcon);
        } catch (AWTException e) {
            System.err.println(e);
        }
        // ...

// ...
// some time later
// the application state has changed - update the image
//        if (trayIcon != null) {
//            trayIcon.setImage(updatedImage);
//        }
// ...

        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();

        clipboard.addFlavorListener(e -> {
            if (!disabled) {
                try {
                    processClipboard(clipboard);
                } catch (IllegalStateException ex) {
                    if (retryCount <= 5) {
                        try {
                            retryCount++;
                            Thread.sleep(50);
                            processClipboard(clipboard);
                        } catch (IOException | UnsupportedFlavorException | InterruptedException exc) {
                            throw new RuntimeException(exc);
                        }
                    } else {
                        retryCount = 0;
                        System.out.println("Clipboard Inaccessible");
                        throw new RuntimeException(ex);
                    }
                } catch (IOException | UnsupportedFlavorException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });
    }

    public static void processClipboard(Clipboard clipboard) throws IOException, UnsupportedFlavorException {
        String input = clipboard.getData(DataFlavor.stringFlavor).toString();
        if (history.size() == 50) {
            history.removeLast();
        }
        history.addFirst(input);
        for (Rule rule : rules) {
            if (rule.isDisabled()) {
                continue;
            }
            Pattern pattern = Pattern.compile(rule.getRegex());
            Matcher m = pattern.matcher(input);
            if (m.find()) {
                int changedIndex = 0;
                String workingCopy = input;
                for (int y = 0; y < m.groupCount(); y++) {
                    workingCopy = workingCopy.substring(0, m.start(y + 1) + changedIndex) + rule.getReplace()[y] + workingCopy.substring(m.end(y + 1) + changedIndex);
                    int matchLen = m.end(y + 1) - m.start(y + 1);
                    changedIndex = changedIndex + (rule.getReplace()[y].length() - matchLen);
                }
                StringSelection output = new StringSelection(workingCopy);
                clipboard.setContents(output, output);
                break;
            }
        }
    }

    public static void createDataFile() throws IOException {
        File newRulesDir = new File(folderPath);
        if (!newRulesDir.exists()) {
            newRulesDir.mkdir();
        }
        File newRules = new File(filePath);
        if (!newRules.exists()) {
            newRules.createNewFile();
        }
    }

    public static void saveDataFile() throws IOException {
        JSONArray jsonArray = new JSONArray();
        for (int i = 0; i < rules.size(); i++) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("name", rules.get(i).getName());
            jsonObject.put("regex", rules.get(i).getRegex());
            jsonObject.put("replace", rules.get(i).getReplace());
            jsonObject.put("disabled", rules.get(i).isDisabled());
            jsonArray.put(jsonObject);
        }
        FileWriter file = new FileWriter(filePath);
        file.write(jsonArray.toString(4));
        file.close();
    }

    public static void loadDataFile() throws IOException {
        File file = new File(filePath);
        String content = new String(Files.readAllBytes(Paths.get(file.toURI())));
        JSONArray a = new JSONArray(content);
        rules = new ArrayList<>();

        for (int i = 0; i < a.length(); i++) {
            JSONObject rule = a.getJSONObject(i);
            JSONArray jsonArray = rule.getJSONArray("replace");
            ArrayList<String> list = new ArrayList<String>();
            for (int y = 0; y < jsonArray.length(); y++) {
                list.add(jsonArray.getString(y));
            }
            String[] stringArray = list.toArray(new String[list.size()]);
            rules.add(new Rule(rule.getString("name"), rule.getString("regex"), stringArray, rule.getBoolean("disabled")));
        }
    }
}