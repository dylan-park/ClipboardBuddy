import org.json.JSONArray;
import org.json.JSONObject;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {
    static String folderPath = System.getenv().get("APPDATA") + "\\ClipboardBuddy";
    static String filePath = folderPath + "\\rules.json";
    static Rule[] rules;
    static boolean disabled = false;
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
        MenuItem quitMenuItem = new MenuItem("Quit");
        // create an action listener to listen for default action executed on the tray icon
        ActionListener listener = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                disabled = !disabled;
                if (disabled) {
                    disableMenuItem.setState(true);
                    trayIcon.setImage(disabledImage);
                }
                if (!disabled) {
                    disableMenuItem.setState(false);
                    trayIcon.setImage(defaultImage);
                }
            }
        };

        ItemListener disabledListener = new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                disabled = !disabled;
                if (disabled) {
                    trayIcon.setImage(disabledImage);
                }
                if (!disabled) {
                    trayIcon.setImage(defaultImage);
                }
            }
        };

        ActionListener historyListener = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            }
        };

        ActionListener quitListener = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // TODO: possibly ensure required data is saved before exit
                System.exit(0);
            }
        };
        // create a popup menu
        PopupMenu popup = new PopupMenu();
        disableMenuItem.addItemListener(disabledListener);
        popup.add(disableMenuItem);
        /// ... add other items
        historyMenuItem.addActionListener(historyListener);
        popup.add(historyMenuItem);
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
        for (Rule rule : rules) {
            Pattern pattern = Pattern.compile(rule.getRegex());
            Matcher m = pattern.matcher(input);
            if (m.find()) {
                StringSelection output = new StringSelection("");
                for (int y = 0; y < m.groupCount(); y++) {
                    // TODO: IMPORTANT, this introduces a bug where only the last replace will be shown in the output. Need a way to marge strings
                    output = new StringSelection(input.substring(0, m.start(y + 1)) + rule.getReplace()[y] + input.substring(m.end(y + 1)));
                }
                clipboard.setContents(output, output);
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

    public static void loadDataFile() throws IOException {
        File file = new File(filePath);
        String content = new String(Files.readAllBytes(Paths.get(file.toURI())));
        JSONArray a = new JSONArray(content);
        rules = new Rule[a.length()];

        for (int i = 0; i < a.length(); i++) {
            JSONObject rule = a.getJSONObject(i);
            JSONArray jsonArray = rule.getJSONArray("replace");
            ArrayList<String> list = new ArrayList<String>();
            for (int y = 0; y < jsonArray.length(); y++) {
                list.add(jsonArray.getString(y));
            }
            String[] stringArray = list.toArray(new String[list.size()]);
            rules[i] = new Rule(rule.getString("name"), rule.getString("regex"), stringArray);
        }
    }
}