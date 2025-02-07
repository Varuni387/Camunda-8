# Clothing Recommendation System

## Project Overview
The **Clothing Recommendation System** is a **Camunda 8** BPMN workflow that suggests appropriate clothing based on the **weather** (summer, winter, rainy) and **activity** (inside, outside). This project leverages a **DMN table** to make automated decisions and a **service task** to process recommendations.

## Tech Stack
- **Camunda 8** (Zeebe, Tasklist, Operate)
- **Spring Boot** (for backend integration)
- **Java** (for service task implementation)
- **DMN (Decision Model and Notation)** (for recommendation logic)
- **Forms** (for user input)

## Workflow Overview
1. **User Enters Weather & Activity Details**  
   - A **User Task** with a form allows users to input:
     - Weather: **Summer, Winter, Rainy**
     - Activity: **Inside, Outside**  

2. **Decision Making via DMN Table**  
   - A **DMN Decision Table** determines the appropriate clothing based on inputs.  

3. **Processing Recommendation**  
   - A **Service Task** fetches the recommendation from the DMN table and prepares the output.  

4. **Display Recommended Clothing**  
   - The recommendation is displayed to the user in the next **User Task**.  

5. **End Process**  
   - The workflow completes after showing the clothing suggestion.

## Getting Started

### Prerequisites
- Java 17+
- c8run
- Camunda 8 SaaS or Self-Managed Setup
- IntelliJ IDEA or VS Code

### Installation Steps
1. **Clone the Repository**
  
2. **Start c8run**
   ```sh
   https://docs.camunda.io/docs/self-managed/setup/deploy/local/c8run/

3. **Deploy the forms and start BPMN**
