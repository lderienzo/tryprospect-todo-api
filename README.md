# 👋 Welcome

Hi - thank you for applying to Prospect! And thank you for doing this 
test.

We'd like to keep this simple and jump straight to code instead of
doing a "HR screening" 🙂.

# ✅ The Project

The application enclosed is the REST API for a simple todo list app.

It has a few basic functionalities -- get the list, create a new task,
and delete a task.

This application is built using Java 8 and Dropwizard. The application
also uses PostgreSQL for storing the tasks.

# 📝 The Task

Your task (pun intended) is to fix a bug, add an enhancement, and a new
feature.

**The Feature**

We have provided you with an endpoint that uses NLP to extract due dates
from a body of text. 

Your task is to utilize that endpoint to infer due dates on tasks and
return them with the rest of the task details.

You can use this endpoint by calling:

```
GET http://interview.tryprospect.co/enrichment/date?text=<task text goes here>
```

Here is an example request and response:

```
GET http://interview.tryprospect.co/enrichment/date?text=get%20some%20milk%20before%20next%20weekend

{
    "fullText": "get some milk before Jun 1",
    "parsedText": "Jun 1",
    "date": 1559424504961,
    "textEdited": true,
    "hasTime": false
}
```

Due dates are optional; not all tasks will have due dates in them.

Here are a few supported examples:

- call the bank tomorrow at 12p
- buy milk on Sunday
- buy new snow boots before fall starts

Just a heads up: this endpoint uses a beefy NLP library and API calls
responses will be slow.
  
**The Enhancement**

Add an endpoint to update an existing task. Some operations that this
should support:

- marking a task as completed (or uncompleted)
- editing the task text
- changing the due date

**The Bug**

You have to find this one! 🙂

# ⭐️ Extra Details

At the bare minimum we'll be evaluating how well you can fix the bug and
add the new functionality.

Bonus points if you can keep all API calls under 500ms (hint: you don't
have to wait for the NLP endpoint to return right away; you can let that
happen in the background).

Anything else that you do will give you extra bonus points. We don't
want to share ideas here because this is entirely up to you!

# 🎯 Getting Started and Submission

To get started, simply clone this repository and start editing the
code.

Once you are ready to submit, please submit a Pull Request via GitHub
a description of the changes you have made.

# 🚀 Testing and Running Your Code

**Building and Running**

The quickest way to test is to run the following commands:

```
# Build application
mvn clean install

# Start application
java -jar target/todo-1.0-SNAPSHOT.jar server config.yml
```

This will run the server on http://localhost:8080

**Database**

We have included a docker-compose to start your own PostgreSQL instance
locally.

Make sure you have Docker installed. Then, run:

```
docker-compose up -d
```

This will start your PostgreSQL instance.

# ❓ Help

If you're stuck or need help, please email aamir@tryprospect.com.

Thanks again and all the best!
