package org.example;

import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.worker.JobWorker;

import java.util.Map;

public class expenses {
    public static void main(String[] args) {
        // Initialize the Zeebe client
        try (ZeebeClient client = ZeebeClient.newClientBuilder()
                .gatewayAddress("localhost:26500") // Replace with your Zeebe gateway address
                .usePlaintext() // For local development
                .build()) {

            System.out.println("Connected to Zeebe.");

            // Register a worker for the "expenses" job type
            JobWorker worker = client.newWorker()
                    .jobType("expenses") // Ensure this matches the job type in your BPMN Service Task
                    .handler((jobClient, job) -> {
                        try {
                            // Retrieve process variables
                            Map<String, Object> variables = job.getVariablesAsMap();

                            // Extract variables with default values to handle nulls
                            String employeeName = (String) variables.getOrDefault("employeeName", "Unknown");
                            String employeeId = (String) variables.getOrDefault("employeeID", "Unknown");
                            String arrivalDate = (String) variables.getOrDefault("arrivalDate", "");
                            String departureDate = (String) variables.getOrDefault("departureDate", "");
                            String travelType = (String) variables.getOrDefault("travelType", "Domestic");
                            Double travelExpenditure = variables.get("travelExpenditure") != null
                                    ? ((Number) variables.get("travelExpenditure")).doubleValue()
                                    : 0.0;
                            Double stayExpenditure = variables.get("stayExpenditure") != null
                                    ? ((Number) variables.get("stayExpenditure")).doubleValue()
                                    : 0.0;
                            Double mealExpenditure = variables.get("mealExpenditure") != null
                                    ? ((Number) variables.get("mealExpenditure")).doubleValue()
                                    : 0.0;
                            Double miscellaneous = variables.get("miscellaneous") != null
                                    ? ((Number) variables.get("miscellaneous")).doubleValue()
                                    : 0.0;

                            // Calculate reimbursement logic
                            Double amountToBePaid = 0.0;
                            if ("Domestic".equals(travelType)) {
                                if (travelExpenditure > 20000) {
                                    amountToBePaid += travelExpenditure - 20000;
                                }
                                if (stayExpenditure > 15000) {
                                    amountToBePaid += stayExpenditure - 15000;
                                }
                                if (mealExpenditure > 10000) {
                                    amountToBePaid += mealExpenditure - 10000;
                                }
                                if (miscellaneous > 5000) {
                                    amountToBePaid += miscellaneous - 5000;
                                }
                            } else if ("International".equals(travelType)) {
                                if (travelExpenditure > 50000) {
                                    amountToBePaid += travelExpenditure - 50000;
                                }
                                if (stayExpenditure > 25000) {
                                    amountToBePaid += stayExpenditure - 25000;
                                }
                                if (mealExpenditure > 20000) {
                                    amountToBePaid += mealExpenditure - 20000;
                                }
                                if (miscellaneous > 10000) {
                                    amountToBePaid += miscellaneous - 10000;
                                }
                            }

                            // Log variables for debugging
                            System.out.println("Received variables:");
                            System.out.println("Employee Name: " + employeeName);
                            System.out.println("Employee ID: " + employeeId);
                            System.out.println("Departure Date: " + departureDate);
                            System.out.println("Arrival Date: " + arrivalDate);
                            System.out.println("Travel Type: " + travelType);
                            System.out.println("Travel Expense: " + travelExpenditure);
                            System.out.println("Stay Expense: " + stayExpenditure);
                            System.out.println("Meal Expense: " + mealExpenditure);
                            System.out.println("Miscellaneous Expense: " + miscellaneous);
                            if(amountToBePaid == 0.0) {
                                System.out.println("Entire expense will be reimbursed");
                            }
                            else {
                                System.out.println("Total Amount to be paid by employee: " + amountToBePaid);
                            }

                            // Complete the job and send back the result
                            jobClient.newCompleteCommand(job.getKey())
                                    .variables(Map.of(
                                            "amountToBePaid", amountToBePaid,
                                            "employeeName", employeeName,
                                            "employeeId", employeeId
                                    ))
                                    .send()
                                    .join();

                            System.out.println("Service task completed successfully for job type: " + job.getType());
                        } catch (Exception e) {
                            // Log the exception stack trace
                            e.printStackTrace();

                            // Provide a default error message if e.getMessage() is null
                            String errorMessage = e.getMessage() != null ? e.getMessage() : "Unknown error occurred";

                            // Fail the job with retries and the error message
                            int retries = Math.max(job.getRetries() - 1, 0);
                            jobClient.newFailCommand(job.getKey())
                                    .retries(retries)
                                    .errorMessage(errorMessage)
                                    .send()
                                    .join();
                        }
                    })
                    .open();

            System.out.println("Worker started for job type: expenses");

            // Keep the worker running
            Thread.currentThread().join();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
