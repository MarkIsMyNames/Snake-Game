import java.awt.Color;                  // For setting the colors of different game elements (e.g., snake, food)
import java.awt.Dimension;              // For defining the dimensions (width & height) of the game panel
import java.awt.Font;                   // For setting the font style and size of the text (e.g., score, "Game Over")
import java.awt.FontMetrics;            // For measuring text width and height to center the score and messages
import java.awt.Graphics;               // For rendering graphics like the snake, food, and score
import java.awt.event.ActionEvent;      // For handling action events like pressing the reset button
import java.awt.event.ActionListener;   // Interface to listen for action events (e.g., button clicks)
import java.awt.event.KeyAdapter;       // For detecting key presses to control the snake's movement
import java.awt.event.KeyEvent;         // For key press events like moving the snake up, down, left, and right
import javax.swing.*;


// The SnakeGame class extends JPanel to create a custom game panel for rendering the game
// and implements ActionListener to handle time-based events (like updating the game state on each timer tick).
public class SnakeGame extends JPanel implements ActionListener {

    // Constants for the game dimensions and setup
    private static final int TILE_SIZE = 30;           // Size of each square tile (used for snake and food)
    private static final int GAME_WIDTH = 800;          // Width of the game panel (in pixels)
    private static final int GAME_HEIGHT = 600;         // Height of the game panel (in pixels)
    private static final int NUM_TILES_X = GAME_WIDTH / TILE_SIZE;  // Number of tiles in the X direction
    private static final int NUM_TILES_Y = GAME_HEIGHT / TILE_SIZE; // Number of tiles in the Y direction

    private static final int INITIAL_SNAKE_LENGTH = 5;  // Initial length of the snake
    private static final int MAX_SNAKE_LENGTH = NUM_TILES_X * NUM_TILES_Y; // Maximum length of the snake (entire game area)

    private boolean isGameActive = false;               // Flag to check if the game is currently active
    private final int[] snakeXCoordinates = new int[MAX_SNAKE_LENGTH];  // Array to hold the X coordinates of each snake segment
    private final int[] snakeYCoordinates = new int[MAX_SNAKE_LENGTH];  // Array to hold the Y coordinates of each snake segment
    private int snakeLength = INITIAL_SNAKE_LENGTH;    // Current length of the snake (starts at 5)

    private int foodXCoordinate;                        // X coordinate of the food
    private int foodYCoordinate;                        // Y coordinate of the food

    private boolean movingLeft = false, movingRight = true, movingUp = false, movingDown = false; // Direction flags for snake movement

    private long score = 0;                             // Current score of the player

    private Timer gameTimer;                            // Timer to control the game loop (update intervals)
    private final JButton resetButton;                        // Button to reset the game after a game over


    protected int lastSnakeXCoordinates;
    protected int lastSnakeYCoordinates;

    // Constructor that sets up the game panel and the reset button
    public SnakeGame() {

        setPreferredSize(new Dimension(GAME_WIDTH, GAME_HEIGHT));  // Set the size of the game panel
        setBackground(Color.black);                            // Set the background color of the panel to black
        setFocusable(true);                                    // Make sure the panel can receive key events
        addKeyListener(new KeyInputHandler());                 // Add the key listener for controlling the snake

        resetButton = new JButton("Reset Game");              // Create a new button for resetting the game
        resetButton.setFont(new Font("Helvetica", Font.BOLD, 20));  // Set the font size and style of the button text
        resetButton.setFocusable(false);                       // Ensure the button doesn't get focus (for key events)
        resetButton.setBounds(GAME_WIDTH / 2 - 100, 20, 200, 50);  // Position the button in the center horizontally, 20px from the top
        resetButton.addActionListener(event -> {        //lambda function
            if (!isGameActive) {
                startNewGame();  // Restart the game when the reset button is clicked
            }
        });
        add(resetButton);  // Add the reset button to the game panel

        startNewGame();  // Start the game as soon as the program runs
    }

    // Method to initialize (or reset) the game state
    private void startNewGame() {
        // Reset the game state variables
        snakeLength = INITIAL_SNAKE_LENGTH;
        score = 0;
        isGameActive = true;

        //Reset error handling movement variables
        lastSnakeXCoordinates = 2147483647;
        lastSnakeYCoordinates = 2147483647;

        // Reset movement flags (snake initially moves to the right)
        movingLeft = false;
        movingRight = true;
        movingUp = false;
        movingDown = false;

        // Set the snake's initial position (starting from the middle of the screen)
        for (int i = 0; i < snakeLength; i++) {
            snakeXCoordinates[i] = NUM_TILES_X / 2 - i;  // Starting X position
            snakeYCoordinates[i] = NUM_TILES_Y / 2;      // Starting Y position
        }

        spawnFood();  // Place the food at a random location on the grid

        resetButton.setVisible(false);  // Hide the reset button during active gameplay

        // Stop the current game timer (if any) before starting a new one
        if (gameTimer != null) {
            gameTimer.stop();
        }

        // Create a new game timer to control the update frequency of the game (move the snake, check for collisions)
        gameTimer = new Timer(100, this);
        gameTimer.start();
    }

    // Method to update the game state (called on each timer tick)
    @Override
    public void actionPerformed(ActionEvent event) {
        if (isGameActive) {
            moveSnake();      // Move the snake based on the current direction
            checkForCollisions(); // Check if the snake collides with itself or the wall
            repaint();        // Redraw the game screen
        }
    }

    // Method to update the snake's position and check if it eats the food
    private void moveSnake() {
        // Move the body of the snake by shifting each segment to the position of the segment in front of it
        for (int i = snakeLength; i > 0; i--) {
            snakeXCoordinates[i] = snakeXCoordinates[i - 1];
            snakeYCoordinates[i] = snakeYCoordinates[i - 1];
        }

        // Move the snake's head in the direction it's facing
        if (movingRight) snakeXCoordinates[0]++;
        if (movingLeft) snakeXCoordinates[0]--;
        if (movingUp) snakeYCoordinates[0]--;
        if (movingDown) snakeYCoordinates[0]++;

        // Check if the snake has eaten the food
        if (snakeXCoordinates[0] == foodXCoordinate && snakeYCoordinates[0] == foodYCoordinate) {
            snakeLength++;  // Increase the length of the snake
            score++;        // Increase the score
            spawnFood();    // Spawn new food at a random location
        }
    }

    // Method to spawn food at a random location on the grid
    private void spawnFood() {
        do {
            // Generate random coordinates for the food
            foodXCoordinate = (int) (Math.random() * NUM_TILES_X);
            foodYCoordinate = (int) (Math.random() * NUM_TILES_Y);
        } while (isFoodOnSnake(foodXCoordinate, foodYCoordinate));  // Ensure the food doesn't overlap with the snake
    }

    // Method to check if the food is on the snake's body (if so, regenerate the food)
    private boolean isFoodOnSnake(int foodX, int foodY) {
        for (int i = 0; i < snakeLength; i++) {
            if (snakeXCoordinates[i] == foodX && snakeYCoordinates[i] == foodY) {
                return true;  // Food is on the snake's body, so it must be regenerated
            }
        }
        return false;  // Food is not on the snake's body, so it can be placed
    }

    // Method to check for collisions with the wall or the snake's own body
    private void checkForCollisions() {
        // Check if the snake hits the wall (game over)
        if (snakeXCoordinates[0] < 0 || snakeXCoordinates[0] >= NUM_TILES_X || snakeYCoordinates[0] < 0 || snakeYCoordinates[0] >= NUM_TILES_Y) {
            isGameActive = false;  // End the game if the snake hits a wall
        }

        // Check if the snake collides with itself (game over)
        for (int i = snakeLength; i > 0; i--) {
            if (snakeXCoordinates[0] == snakeXCoordinates[i] && snakeYCoordinates[0] == snakeYCoordinates[i]) {
                isGameActive = false;  // End the game if the snake collides with itself
                break;
            }
        }
    }

    // Method to render the game elements (snake, food, score) on the screen
    @Override
    protected void paintComponent(Graphics graphicsContext) {
        super.paintComponent(graphicsContext);

        if (isGameActive) {
            // Draw the snake
            graphicsContext.setColor(Color.green); // Set the snake color to green
            for (int i = 0; i < snakeLength; i++) {
                graphicsContext.fillRect(snakeXCoordinates[i] * TILE_SIZE, snakeYCoordinates[i] * TILE_SIZE, TILE_SIZE, TILE_SIZE);
            }

            // Draw the food
            graphicsContext.setColor(Color.red); // Set the food color to red
            graphicsContext.fillRect(foodXCoordinate * TILE_SIZE, foodYCoordinate * TILE_SIZE, TILE_SIZE, TILE_SIZE);

            // Draw the score at the top of the screen
            drawScore(graphicsContext);
        } else {
            // Display the "Game Over" message and final score
            displayGameOver(graphicsContext);
        }
    }

    // Method to display the score at the top of the screen
    private void drawScore(Graphics graphicsContext) {
        String scoreText = "Score: " + score;
        Font font = new Font("Helvetica", Font.BOLD, 30); // Set the font for the score text
        FontMetrics metrics = graphicsContext.getFontMetrics(font);  // Get the font metrics to center the score text

        graphicsContext.setColor(Color.white); // Set the text color to white
        graphicsContext.setFont(font);          // Set the font for the text
        graphicsContext.drawString(scoreText, (GAME_WIDTH - metrics.stringWidth(scoreText)) / 2, metrics.getHeight());
    }

    // Method to display the "Game Over" message and final score
    private void displayGameOver(Graphics graphicsContext) {
        String gameOverMessage = "Game Over";
        String scoreMessage = "Score: " + score;
        Font font = new Font("Helvetica", Font.BOLD, 50);  // Set a larger font for "Game Over"
        FontMetrics metrics = graphicsContext.getFontMetrics(font);

        graphicsContext.setColor(Color.white);  // Set the text color to white
        graphicsContext.setFont(font);          // Set the font for the message text
        graphicsContext.drawString(gameOverMessage, (GAME_WIDTH - metrics.stringWidth(gameOverMessage)) / 2, GAME_HEIGHT / 2 - 50);
        graphicsContext.drawString(scoreMessage, (GAME_WIDTH - metrics.stringWidth(scoreMessage)) / 2, GAME_HEIGHT / 2 + 50);

        resetButton.setVisible(true);  // Show the reset button when the game is over
    }

    // Key input handler for controlling the snake's movement (up, down, left, right)
    private class KeyInputHandler extends KeyAdapter {

        //initalize variables to prevent the player going in on itself
        int lastSnakeXCoordinates = 2147483647;
        int lastSnakeYCoordinates = 2147483647;

        @Override
        public void keyPressed(KeyEvent keyEvent) {
            int keyPressed = keyEvent.getKeyCode(); //get key pressed

            // Prevents player from making the snake fold in on itself
            if(lastSnakeXCoordinates != snakeXCoordinates[0] || lastSnakeYCoordinates != snakeYCoordinates[0] ) {

                lastSnakeXCoordinates = snakeXCoordinates[0];
                lastSnakeYCoordinates = snakeYCoordinates[0];

                // Prevent the snake from reversing direction (e.g., can't go left if moving right)
                if (keyPressed == KeyEvent.VK_A || keyPressed == KeyEvent.VK_LEFT) {
                    if (!movingRight) {
                        movingLeft = true;
                        movingUp = false;
                        movingDown = false;
                    }
                }
                if (keyPressed == KeyEvent.VK_D || keyPressed == KeyEvent.VK_RIGHT) {
                    if (!movingLeft) {
                        movingRight = true;
                        movingUp = false;
                        movingDown = false;
                    }
                }
                if (keyPressed == KeyEvent.VK_W || keyPressed == KeyEvent.VK_UP) {
                    if (!movingDown) {
                        movingUp = true;
                        movingRight = false;
                        movingLeft = false;
                    }
                }
                if (keyPressed == KeyEvent.VK_S || keyPressed == KeyEvent.VK_DOWN) {
                    if (!movingUp) {
                        movingDown = true;
                        movingRight = false;
                        movingLeft = false;
                    }
                }
            }
        }
    }

    // Main method to start the game
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
}
