# Expense App


## Objective:
Design an expense tracker application that sources spending data directly from email accounts.

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
- `comments`: Additional transaction notes.
- `currency`: Currency of transaction.
- `amount`: Transaction amount (up to two decimal places).
- `type`: Transaction type (expense or income).
- `receiver info`: Recipient information (optional).
- `category`: Transaction category.
- `date`: Transaction date.
- `time`: Transaction time.
- *(Optional)* Additional Filters: Explore metadata for advanced filtering options.

## Server Design:

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

