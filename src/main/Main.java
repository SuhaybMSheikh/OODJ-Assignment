package main;

import javax.swing.*;

/**
 * ENTRY POINT — Main.java
 * ------------------------
 * This is the FIRST file Java runs when you start the program.
 * It simply launches the LoginFrame window.
 *
 * HOW TO RUN FROM VS CODE TERMINAL:
 *   1. Open a terminal (Ctrl + `)
 *   2. Navigate to the project root:  cd path/to/APU_ASC
 *   3. Compile ALL files:
 *        Windows:  javac -d bin -sourcepath src src/main/Main.java
 *   4. Run:
 *        java -cp bin main.Main
 */
public class Main {efvgftgrgf

    public static void main(String[] args) {

        // SwingUtilities.invokeLater() ensures the GUI is created on the
        // correct thread (the "Event Dispatch Thread").
        // Always do this — skipping it can cause visual glitches.
        SwingUtilities.invokeLater(() -> {
            new LoginFrame().setVisible(true);
        });
    }
}
