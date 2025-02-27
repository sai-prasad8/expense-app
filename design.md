# Expense App


## Objective:
Design an expense tracker application that sources spending data directly from email accounts.(we will use gmail api so, it will work with gmails only.)

This expense tracker should:

1. Automatically fetch transaction data from email accounts.
2. Categorize transactions based on merchant information.
3. Provide comprehensive spending analysis and reports.

## Requirements:

1. **User Registration**: Users should be able to register through an online portal.
2. **Transaction Management**: Provide options to add/edit transactions.
3. **Categorization**: Categorize transactions using MCC (Merchant Category Code) codes.
4. **Report Generation**: Generate daily, weekly, and monthly reports with graphs and statistics such as averages, and delta increases/decreases.
5. **Spending Analysis**:
   - Weekly and monthly spending trends.
   - Bar graph visualization of spending by category.
   - Budget progress tracking

## Basic Architechture:

1. **Elasticsearch**: Elasticsearch RDS instances store the transaction data of users.
2. **Kibana Integration**: Kibana instance(s) communicate with RDS for data visualization and analysis. 
    ![basic architecture image](/designassets/basicarchitecture.png)

## Database Schema:

### Elasticsearch Document:
- `userid`: Unique user identifier.
- `transaction_id`: Unique id for each transaction.
- `comments`: Additional transaction notes.
- `currency`: Currency of transaction.
- `amount`: Transaction amount (up to two decimal places).
- `type`: Transaction type (expense or income).
- `receiver_info`: Recipient information (optional).
- `category`: Transaction category.
- `date`: Transaction date.
- `time`: Transaction time.
- `created_at`: insertion time
- `updated_at`: updation time
- *(Optional)* Additional Filters: Explore metadata for advanced filtering options.

## Server Design:

   **Handling Emails**
   1. Use AWS event bridge to schedule daily execution.
   2. Use Lambda functions to run 'user details fetcher' and   'Emails fetcher and parser'.
   3. Use AWS SQS for queues inbetween the lamdba functions.

   **Email Fetcher and Parser:**
   this component retrieves the user/client gmail along with access token from 'user details fetcher' via queue.
   It uses gmail api to read emails. It filters and searches for mails sent from bank emails.

   The email body is then parsed using a set of regex patterns corresponding to the bank templates to extract necessary data: amount, currency, transaction type, date, time, category, receiver info.
   (The bank emails and their regex patterns are stored in a configuration file within the executable JAR.)

   The extracted data is stored in Elasticsearch, and the user_id/email is added to the queue for the reporter component.


1. **Accessing Transactions**:
   - Fetch daily emails for transactions.
   - Parse emails to extract transaction details.

2. **Storing and Categorizing**:
   - Store data in Elasticsearch.
   - Categorize transactions based on predefined rules.

3. **Generating Reports**:
   - Compile daily transaction data.
   - Create reports with graphs and statistics.

4. **Sending Reports via Email**:
   - Automate email dispatch of reports to users.

5. **Automation**:
   - Schedule daily execution of scripts.

