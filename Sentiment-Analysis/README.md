# Sentiment Analysis using Llama, Spring Boot and Camunda 8

## Project Overview
The **Sentiment Analysis System** is a **Camunda 8** BPMN workflow designed to analyze the sentiment of user-provided text. This project processes user input, performs sentiment analysis using Llama, and returns a structured JSON response.

## Tech Stack
- **Camunda 8** (Zeebe, Tasklist, Operate)
- **Spring Boot** (for backend integration)
- **Java** (for service task implementation)
- **Llama (Ollama API)** (for sentiment analysis)

## Workflow Overview
1. **User Submits Prompt**  
   - A **User Task** allows users to enter a sentence for sentiment analysis.  

2. **Perform Sentiment Analysis**  
   - A **Service Task** calls the Llama model to analyze sentiment.  
   - The response is formatted as a JSON object.  

3. **Display Results**  
   - The JSON object is stored and displayed back to the user via a **User Task**.  

4. **End Process**  
   - The workflow ends after showing the analysis results.  

## Getting Started

### Prerequisites
- Java 17+
- c8run  
- Camunda 8 SaaS or Self-Managed Setup  
- IntelliJ IDEA or VS Code  
- Ollama installed and running (`ollama serve`)  

### Installation Steps
1. **Clone the Repository**
   ```sh
   git clone https://github.com/Varuni387/sentiment-analysis-system.git
   cd sentiment-analysis-system

2. **Start c8run**
   ```sh
   https://docs.camunda.io/docs/self-managed/setup/deploy/local/c8run/
3.**Deploy BPMN and start**
