package org.example;

import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.worker.JobWorker;

import java.util.Map;

public class chooseClothes {
    public static void main(String[] args) {
        // Initialize the Zeebe client
        try (ZeebeClient client = ZeebeClient.newClientBuilder()
                .gatewayAddress("localhost:26500") // Replace with your Zeebe gateway address
                .usePlaintext() // For local development
                .build()) {

            System.out.println("Connected to Zeebe.");

            // Register a worker for the "suggest-top" job type
            JobWorker worker = client.newWorker()
                    .jobType("suggest-top")  // Ensure this matches the job type in your BPMN Service Task
                    .handler((jobClient, job) -> {
                        try {
                            // Retrieve process variables
                            Map<String, Object> variables = job.getVariablesAsMap();
                            String Weather = (String) variables.get("Weather");
                            String Activity = (String) variables.get("Activity");

                            // Check the values of the variables for debugging purposes
                            System.out.println("Received variables - Weather: " + Weather + ", Activity: " + Activity);

                            String suitableTop = "";
                            if ("Hot".equals(Weather) && "Inside".equals(Activity)) {
                                suitableTop = "T-shirt";
                            } else if ("Cold".equals(Weather) && "Inside".equals(Activity)) {
                                suitableTop = "Full sleeves T-shirt";
                            } else if ("Hot".equals(Weather) && "Outside".equals(Activity)) {
                                suitableTop = "Cotton T-shirt";
                            } else if ("Cold".equals(Weather) && "Outside".equals(Activity)) {
                                suitableTop = "Sweater is must";
                            }

                            // Print the determined suitable top
                            System.out.println("The suitable top is: " + suitableTop);

                            // Complete the job and send back the result
                            jobClient.newCompleteCommand(job.getKey())
                                    .variables(Map.of("suitableTop", suitableTop))
                                    .send()
                                    .join();

                            System.out.println("Service task completed successfully for job type: " + job.getType());
                        } catch (Exception e) {
                            System.err.println("Error processing job: " + e.getMessage());
                            e.printStackTrace();

                            // Fail the job if something goes wrong
                            jobClient.newFailCommand(job.getKey())
                                    .retries(job.getRetries() - 1)
                                    .errorMessage(e.getMessage())
                                    .send()
                                    .join();
                        }
                    })
                    .open();

            System.out.println("Worker started for job type: suggest-top");

            // Keep the worker running
            Thread.currentThread().join();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
