import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

public class Main {
    static boolean disabled = false;
    // load an image
    static Image defaultImage = Toolkit.getDefaultToolkit().getImage("resources/default.png");
    static Image disabledImage = Toolkit.getDefaultToolkit().getImage("resources/disabled.png");

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
    }
}