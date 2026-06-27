import java.io.*;
import java.util.Locale;

/**
 * Entry point of GigMatch Pro.
 * <p>
 * Reads commands from the input file, sends each command to Core,
 * and writes Core’s responses to the output file.
 *
 * @author Gülsüm Yazıcı
 * @studentId 2023400072
 */
public class Main {

    /** Single Core instance that stores all platform data and executes all operations. */
    private static final Core core = new Core();

    /**
     * Initializes I/O, reads the input line by line, and processes each command.
     */
    public static void main(String[] args) {
        Locale.setDefault(Locale.US);

        if (args.length != 2) {
            System.err.println("Usage: java Main <input_file> <output_file>");
            System.exit(1);
        }

        String inputFile  = args[0];
        String outputFile = args[1];

        try (BufferedReader reader = new BufferedReader(new FileReader(inputFile));
             BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile))) {

            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (!line.isEmpty()) {
                    processCommand(line, writer);
                }
            }

        } catch (IOException e) {
            System.err.println("Error reading/writing files: " + e.getMessage());
        }
    }

    /**
     * Parses a single command, dispatches it to the appropriate Core method,
     * and writes the returned output string into the output file.
     */
    private static void processCommand(String command, BufferedWriter writer)
            throws IOException {

        String[] parts = command.split("\\s+");
        String op = parts[0];
        String result = "";

        try {
            switch (op) {
                case "register_customer":
                    result = core.registerCustomer(parts[1]);
                    break;

                case "register_freelancer":
                    result = core.registerFreelancer(
                            parts[1], parts[2], Integer.parseInt(parts[3]),
                            Integer.parseInt(parts[4]), Integer.parseInt(parts[5]),
                            Integer.parseInt(parts[6]), Integer.parseInt(parts[7]),
                            Integer.parseInt(parts[8])
                    );
                    break;

                case "request_job":
                    result = core.requestJob(parts[1], parts[2], Integer.parseInt(parts[3]));
                    break;

                case "employ_freelancer":
                    result = core.employ(parts[1], parts[2]);
                    break;

                case "complete_and_rate":
                    result = core.completeAndRate(parts[1], Integer.parseInt(parts[2]));
                    break;

                case "cancel_by_freelancer":
                    result = core.cancelByFreelancer(parts[1]);
                    break;

                case "cancel_by_customer":
                    result = core.cancelByCustomer(parts[1], parts[2]);
                    break;

                case "blacklist":
                    result = core.blacklist(parts[1], parts[2]);
                    break;

                case "unblacklist":
                    result = core.unblacklist(parts[1], parts[2]);
                    break;

                case "change_service":
                    result = core.changeService(parts[1], parts[2], Integer.parseInt(parts[3]));
                    break;

                case "simulate_month":
                    result = core.simulateMonth();
                    break;

                case "query_freelancer":
                    result = core.queryFreelancer(parts[1]);
                    break;

                case "query_customer":
                    result = core.queryCustomer(parts[1]);
                    break;

                case "update_skill":
                    result = core.updateSkill(
                            parts[1],
                            Integer.parseInt(parts[2]), Integer.parseInt(parts[3]),
                            Integer.parseInt(parts[4]), Integer.parseInt(parts[5]),
                            Integer.parseInt(parts[6])
                    );
                    break;

                default:
                    result = "Unknown command: " + op;
            }

            writer.write(result);
            writer.write("\r\n");

        } catch (Exception ex) {
            writer.write("Error processing command: " + command);
            writer.write("\r\n");
        }
    }
}
