# Employee Expense Reimbursement System

## Project Overview
The **Employee Expense Reimbursement System** is a **Camunda 8** BPMN workflow designed to streamline the process of submitting, reviewing, and approving employee expenses. This project automates the approval flow based on predefined rules.
## 🛠 Tech Stack
- **Camunda 8** (Zeebe, Tasklist, Operate)
- **Spring Boot** (for backend integration)
- **Java** (for service task implementation)

## Workflow Overview
1. **Employee Submits Expense**  
   - A **User Task** allows employees to enter details like money spent for each category.  
   
2. **100% reimbursement**  
   - If the amount is **below the defined threshold**, it will be reimbursed.  
   - If higher, only the maximum defined amount is reimbursed.  

4. **Notification to Employee**  
   - The employee is notified of how much amount is being reimbursed.  

5. **End Process**  
   - The workflow ends successfully after the expense is reimbursed.

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
   
