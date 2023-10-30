package Mobile;

import java.io.*;
import java.util.*;

public class CheckpointExample {
    // Function to perform a computational task
    public static void performTask(int taskId) {
        String checkpointFile = "checkpoint_" + taskId + ".txt";

        // Check if a checkpoint file exists for this task
        File file = new File(checkpointFile);
        if (file.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(checkpointFile))) {
                // If checkpoint exists, load the state from the file
                String state = reader.readLine();
                // Resume computation from the loaded state
                System.out.println("Resuming task " + taskId + " from checkpoint: " + state);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            // Start computation from scratch
            String state = "Start";
            System.out.println("Starting task " + taskId + " from scratch");
        }

        // Perform the computational task
        for (int i = 1; i <= 5; i++) {
            String state = "Iteration " + i;
            System.out.println("Task " + taskId + ": " + state);

            // Checkpoint the task state every 2 iterations
            if (i % 2 == 0) {
                try (BufferedWriter writer = new BufferedWriter(new FileWriter(checkpointFile))) {
                    writer.write(state);
                    System.out.println("Checkpoint saved for task " + taskId);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            try {
                Thread.sleep(1000); // Simulating some computational work
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        // Clean up the checkpoint file after task completion
        if (file.exists()) {
            if (file.delete()) {
                System.out.println("Checkpoint file removed for task " + taskId);
            }
        }
    }

    public static void main(String[] args) {
        List<Integer> taskIds = Arrays.asList(1, 2, 3);

        for (int taskId : taskIds) {
            performTask(taskId);
            System.out.println("Task " + taskId + " completed\n");
        }
    }
}
