import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.IOException;
import java.util.regex.Pattern;

public class Main {
    static boolean disabled = false;
    static int retryCount = 0;
    // load an image
    static Image defaultImage = Toolkit.getDefaultToolkit().getImage(Main.class.getResource("default.png"));
    static Image disabledImage = Toolkit.getDefaultToolkit().getImage(Main.class.getResource("disabled.png"));

    public static void main(String[] args) {
        // get the SystemTray instance
        SystemTray tray = SystemTray.getSystemTray();
        TrayIcon trayIcon = new TrayIcon(defaultImage, "ClipboardBuddy");
        CheckboxMenuItem disableMenuItem = new CheckboxMenuItem("Disable");
        MenuItem quitMenuItem = new MenuItem("Quit");
        // create a action listener to listen for default action executed on the tray icon
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
        if (Pattern.compile("http(?:s)?:\\/\\/(?:www)?twitter\\.com\\/([a-zA-Z0-9_]+)\\/status(\\/.*)?").matcher(input).matches()) {
            StringSelection selection = new StringSelection(input.substring(0, input.indexOf("//") + 2) + "fx" + input.substring(input.indexOf("twitter")));
            clipboard.setContents(selection, selection);
        }
    }
}