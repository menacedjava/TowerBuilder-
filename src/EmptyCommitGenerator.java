import java.io.*;
import java.nio.file.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;

/**
 * Empty Commit Generator for GitHub Repositories
 * Automatically creates empty commits for each day in a date range and pushes to remote
 */
public class EmptyCommitGenerator {

    // Configuration - Update these values before running
    private static final String REPO_PATH = "C:/Users/Acer Nitro/PycharmProjects/Jarvis_ai";
    private static final String REPO_URL = "https://github.com/menacedjava/TowerBuilder-.git";
    private static final String BRANCH = "master";
    private static final String START_DATE = "2026-01-01";
    private static final String END_DATE = "2026-01-31";
    private static final long DELAY_MS = 10;
    private static final boolean USE_CREDENTIAL_HELPER = true;
    private static final boolean OPTIONAL_PUSH_WITH_TOKEN = false;

    // Constants
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final String GITHUB_TOKEN_ENV_VAR = "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";

    public static void main(String[] args) {
        try {
            System.out.println("🚀 Starting Empty Commit Generator...");
            System.out.println("Repository: " + REPO_URL);
            System.out.println("Date Range: " + START_DATE + " to " + END_DATE);
            System.out.println("Branch: " + BRANCH);
            System.out.println("----------------------------------------");

            // Validate configuration
            validateConfiguration();

            // Change to repository directory
            changeToRepoDirectory();

            // Verify we're in a git repository
            verifyGitRepository();

            // Generate commits for each day in the range
            generateEmptyCommits();

            // Push to remote
            pushToRemote();

            System.out.println("✅ Successfully completed all operations!");

        } catch (Exception e) {
            System.err.println("❌ Error: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static void validateConfiguration() {
        if (REPO_PATH == null || REPO_PATH.trim().isEmpty()) {
            throw new IllegalArgumentException("REPO_PATH must be specified");
        }

        if (!Files.exists(Paths.get(REPO_PATH))) {
            throw new IllegalArgumentException("Repository path does not exist: " + REPO_PATH);
        }

        if (START_DATE == null || END_DATE == null) {
            throw new IllegalArgumentException("START_DATE and END_DATE must be specified");
        }

        try {
            LocalDate start = LocalDate.parse(START_DATE, DATE_FORMATTER);
            LocalDate end = LocalDate.parse(END_DATE, DATE_FORMATTER);

            if (start.isAfter(end)) {
                throw new IllegalArgumentException("START_DATE cannot be after END_DATE");
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid date format. Use YYYY-MM-DD format");
        }

        if (OPTIONAL_PUSH_WITH_TOKEN && !USE_CREDENTIAL_HELPER) {
            String token = System.getenv(GITHUB_TOKEN_ENV_VAR);
            if (token == null || token.trim().isEmpty()) {
                throw new IllegalStateException(
                        "OPTIONAL_PUSH_WITH_TOKEN is true but " + GITHUB_TOKEN_ENV_VAR +
                                " environment variable is not set. Please set your GitHub token."
                );
            }
        }
    }

    private static void changeToRepoDirectory() {
        try {
            Path repoPath = Paths.get(REPO_PATH).toAbsolutePath();
            System.out.println("Changing to repository directory: " + repoPath);
            System.setProperty("user.dir", repoPath.toString());
        } catch (Exception e) {
            throw new RuntimeException("Failed to change to repository directory: " + e.getMessage(), e);
        }
    }

    private static void verifyGitRepository() {
        try {
            ProcessBuilder pb = createProcessBuilder("git", "status");
            Process process = pb.start();
            int exitCode = process.waitFor();

            if (exitCode != 0) {
                throw new RuntimeException("Not a git repository or git command failed");
            }
            System.out.println("✅ Verified git repository");
        } catch (Exception e) {
            throw new RuntimeException("Failed to verify git repository: " + e.getMessage(), e);
        }
    }

    private static void generateEmptyCommits() {
        LocalDate startDate = LocalDate.parse(START_DATE, DATE_FORMATTER);
        LocalDate endDate = LocalDate.parse(END_DATE, DATE_FORMATTER);

        System.out.println("Generating empty commits from " + startDate + " to " + endDate);

        LocalDate currentDate = startDate;
        int commitCount = 0;

        while (!currentDate.isAfter(endDate)) {
            try {
                String dateStr = currentDate.format(DATE_FORMATTER);
                String commitMessage = "Commit " + dateStr;
                String commitDate = dateStr + "T12:00:00";

                System.out.println("📅 Processing date: " + dateStr);

                // Create empty commit with specific date
                ProcessBuilder commitPb = createProcessBuilder(
                        "git", "commit", "--allow-empty",
                        "--date=" + commitDate,
                        "-m", commitMessage
                );

                Process commitProcess = commitPb.start();
                int commitExitCode = commitProcess.waitFor();

                if (commitExitCode == 0) {
                    System.out.println("✅ Created empty commit for " + dateStr);
                    commitCount++;
                } else {
                    // Read error stream for more details
                    BufferedReader errorReader = new BufferedReader(
                            new InputStreamReader(commitProcess.getErrorStream())
                    );
                    StringBuilder error = new StringBuilder();
                    String line;
                    while ((line = errorReader.readLine()) != null) {
                        error.append(line).append("\n");
                    }
                    System.err.println("⚠️  Failed to create commit for " + dateStr + ": " + error.toString());
                }

                // Delay between commits
                if (DELAY_MS > 0 && !currentDate.equals(endDate)) {
                    TimeUnit.MILLISECONDS.sleep(DELAY_MS);
                }

                currentDate = currentDate.plusDays(1);

            } catch (Exception e) {
                System.err.println("❌ Error processing date " + currentDate + ": " + e.getMessage());
                // Continue with next date despite errors
                currentDate = currentDate.plusDays(1);
            }
        }

        System.out.println("✅ Generated " + commitCount + " empty commits");
    }

    private static void pushToRemote() {
        try {
            System.out.println("🚀 Pushing commits to remote repository...");

            if (OPTIONAL_PUSH_WITH_TOKEN && !USE_CREDENTIAL_HELPER) {
                pushWithToken();
            } else {
                pushWithCredentialHelper();
            }

            System.out.println("✅ Successfully pushed to remote repository");

        } catch (Exception e) {
            throw new RuntimeException("Failed to push to remote: " + e.getMessage(), e);
        }
    }

    private static void pushWithCredentialHelper() throws Exception {
        ProcessBuilder pb = createProcessBuilder("git", "push", "origin", BRANCH);
        Process process = pb.start();

        // Capture output
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line;
        while ((line = reader.readLine()) != null) {
            System.out.println("git push: " + line);
        }

        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new RuntimeException("git push failed with exit code: " + exitCode);
        }
    }

    private static void pushWithToken() throws Exception {
        String token = System.getenv(GITHUB_TOKEN_ENV_VAR);
        if (token == null || token.trim().isEmpty()) {
            throw new IllegalStateException("GitHub token not found in environment variable: " + GITHUB_TOKEN_ENV_VAR);
        }

        // Get remote URL and modify it to include token
        ProcessBuilder remotePb = createProcessBuilder("git", "remote", "get-url", "origin");
        Process remoteProcess = remotePb.start();

        BufferedReader remoteReader = new BufferedReader(new InputStreamReader(remoteProcess.getInputStream()));
        String remoteUrl = remoteReader.readLine().trim();
        remoteProcess.waitFor();

        // Modify URL to include token for authentication
        String pushUrl = remoteUrl.replace("https://", "https://" + token + "@");

        System.out.println("Using token-based authentication for push");

        ProcessBuilder pushPb = createProcessBuilder("git", "push", pushUrl, BRANCH);
        Process pushProcess = pushPb.start();

        int exitCode = pushProcess.waitFor();
        if (exitCode != 0) {
            throw new RuntimeException("Token-based git push failed with exit code: " + exitCode);
        }
    }

    private static ProcessBuilder createProcessBuilder(String... command) {
        ProcessBuilder pb = new ProcessBuilder(command);
        pb.directory(new File(REPO_PATH));

        // For better error handling, we'll capture streams manually
        pb.redirectErrorStream(false);

        return pb;
    }

    /**
     * Utility method to check if running on Windows
     */
    private static boolean isWindows() {
        return System.getProperty("os.name").toLowerCase().contains("windows");
    }
}