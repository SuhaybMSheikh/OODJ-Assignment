package main;

import javax.swing.*;

/**
 * HOW TO RUN FROM VS CODE TERMINAL:
 *   1. Open a terminal (Ctrl + `)
 *   2. Navigate to the project root:  cd path/to/APU_ASC
 *   3. Compile ALL files:
 *        Windows:  javac -d bin -sourcepath src src/main/Main.java
 *   4. Run:
 *        java -cp bin main.Main in terminal or press the Run button at the top right
 */

public class Main {

    public static void main(String[] args) {

        SwingUtilities.invokeLater(() -> {
            new LoginFrame().setVisible(true);
        });
    }
}
