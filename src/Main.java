import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;

public class Main {

    // Set of domains to check
    // Use a hashset in case I need to add more domains in the future
    private static final Set<String> targetDomains = new HashSet<>();

    // Flag to ensure "no" is printed only once
    private static boolean printedNo = false;


    //main method to run whole application
    public static void main(String[] args) throws IOException {

        //Checks to see if the domain checker can run
        domainRunningChecker();

        // Domains used for waiting on a meeting
        // This could change but currently works as of 29/12/24;
        targetDomains.add(".relay.teams.microsoft.com");


        // Repeating the tcpdump process infinitely
        while (true) {
            runTcpdump();
        }
    }

    private static void runTcpdump() throws IOException {

        // Start the process tcpdump
        Process runTcpdump = Runtime.getRuntime().exec("sudo tcpdump -i any port 53");// May need to run as a root user

        // Get the output from the process
        BufferedReader readTcpdump = new BufferedReader(new InputStreamReader(runTcpdump.getInputStream()));
        String domainLine;

        //cycles through each domain detected
        while ((domainLine = readTcpdump.readLine()) != null) {

            // Check if line contains a DNS query for one of the target domains
            if (!printedNo) {
                if (containsTargetDomain(domainLine)) {
                    printedNo = true; // Ensure it only runs once
                    SnakeGame.start();
                }
            }

            // Domain to see if you have been let into a meeting
            // This could change but currently works as of 29/12/24
            // This exits the application once you are connect to your meeting
            if(domainLine.contains("emea.pptservicescast.officeapps.live.com")){
                SnakeGame.end();//Calls the method to end the game
                printedNo = false;
            }
        }
    }

    // Method to check if the line contains a target domain (google.com or example.com)
    private static boolean containsTargetDomain(String domainLine) {
        for (String domain : targetDomains) {
            if (domainLine.contains(domain)) {
                return true;  // Domain found in the line
            }
        }
        return false;
    }

    //Checks if it is safe to run the domain checker
    private static void domainRunningChecker() {

        //checks if the os is linux
        if(!System.getProperty("os.name").equals("Linux")){
            System.err.println("This detection only works for Linux");
            SnakeGame.start();
            throw new RuntimeException();//stops the game from launching multiple times
        }


        try {
            //checks if it has sudo permissions
            Process sudo = Runtime.getRuntime().exec("sudo ls"); // runs any sudo command
            BufferedReader sudoReader = new BufferedReader(new InputStreamReader(sudo.getInputStream()));

            if (sudoReader.readLine().contains("sudo")) {
                throw new RuntimeException();
            }

        } catch(IOException e){
            System.err.println("This detection doesn't work with Flatpaks");
            SnakeGame.start();
            throw new RuntimeException();//stops the game from launching multiple times

        }catch (RuntimeException e) {
            System.err.println("This detection must be run with sudo");
            SnakeGame.start();
            throw new RuntimeException();//stops the game from launching multiple times
        }
    }
}
