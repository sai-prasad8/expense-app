Objectives:
(Spending by time (and category))
1. Weekly and monthly spending trends.
2. Bar graph of spending by category.
3. Budget progress.

Design schema to store transaction (and category data).
Take transaction data from email and store in elastic search.

Requirements:

1. User Registration through online portal
2. Option to add/edit transactions
3. Categorize (using mcc codes)
4. Generate daily weekly and monthly reports (must include
    graphs    and statistics like avg,delta inc/dec )
5. Kibana scrips to generate these stats(have to decide on types of graphs).
6. daily report stats with spending and comparision with      previous    days followed a bar graph showing this stat. 
    Prediction/trajectory for this week.
    Show detailed spending category wise.
7. 

Basic Architechture:

1. elastic search Aws Rds instance stores the transaction data of users.
2. Kibana instance(s) talk to rds.
3. One instance parses data from email and store it in rds  
4. the main server runs a script daily that tells kibana to generate report. After generating report the kibana instance mails the report.
(do i need to seperate server and kibana instance?)
5. use load balancer if necessary.

Database Schema:

elastic serach document:
    userid
    comments
    amount (.00 precision is enough)
    reciever info(?)
    category ()
    date
    (optional -how to implement more filters?)

Server design:
    We need access daily transactions in email 
    Store and categorize them
    Generate daily report from this data.
    Send the report to user via email.
    these scripts need to run daily


    <!-- i need to learn about accessing email and sending mails. -->
    




