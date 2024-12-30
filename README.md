This program was written in Java 21 and needs JDK 21 installed to run

Currently, the waiting list detection only works for administrators on a Linux system. However, the game can still run on all platforms

The program should be run from the Main.java file however if anything with the detection breaks the game can still be run from the SnakeGame.java file
// Main method to start the game in case the detection breaks
public static void main(String[] args) {
SnakeGame game = new SnakeGame();
JFrame frame = new JFrame();
frame.setTitle("Snake Game");                      // Set the window title
frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Close the window when exiting
frame.add(game);                                   // Add the game panel to the window
frame.pack();                                      // Pack the window to its preferred size
frame.setLocationRelativeTo(null);                 // Center the window on the screen
frame.setVisible(true);                            // Make the window visible
}