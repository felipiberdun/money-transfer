# Money Transfer

## About
---
This is a simple application to create accounts and manage money transfer between them.
It was created using Kotlin language and Micronaut framework in a reactive model.
Spek2 and Mockk were used to write the tests.


## Starting up the application
---

1. Download this project or git clone it using `git clone git@github.com:felipiberdun/money-transfer.git`;
1. Open project's root folder and run `gradlew clean build` to compile and generate the artifact;
1. Start the application using `java -jar build/libs/money-transfer-0.1-all.jar`;
1. The message `Startup completed in Xms. Server Running: http://localhost:8080` signals that the application is up and running;

## Routes
---

### Creating an account
#### Request
POST - http://localhost:8080/accounts

```json
{
	"owner":"Account owner name"
}
```
#### Response
201 Created - Location header will provide the new resource

---
### Retrieving account
#### Request
GET - http://localhost:8080/accounts/{accountId}

#### Response
200 OK
```json
{
  "id": "33accfc6-9e94-4c22-ad1d-24151cfc8de7",
  "owner": "Account owner name",
  "creationDate": "2019-12-01T20:09:35.163"
}
```

---
### Querying an account current balance 
#### Request
GET - http://localhost:8080/accounts/{accountId}/balance

#### Response
```json
23.0
```

---
### Retrieving account's transactions
#### Request
GET - http://localhost:8080/accounts/{accountId}/transactions

#### Response
200 OK
```json
[
    {
        "id": "92a2d0f3-d77a-49c1-a967-3cc47cdf40eb",
        "to": "33accfc6-9e94-4c22-ad1d-24151cfc8de7",
        "amount": 23.0,
        "date": "2019-12-01T20:10:10.275",
        "type": "DEPOSIT"
    }
]
```

---
### Depositing money into an account
#### Request
POST - http://localhost:8080/accounts/{accountId}/deposits

```json
{
	"amount": 23
}
```

#### Response
201 Created - Location header will provide the new resource

---
### Withdrawing money from account
#### Request
POST - http://localhost:8080/accounts/{accountId}/withdraws

```json
{
	"amount": 1
}
```

#### Response
201 Created - Location header will provide the new resource
 

---
### Transferring money from account to another
#### Request
POST - http://localhost:8080/accounts/{accountId}/transfers

```json
{
	"amount": 40,
	"to": "6c6872a4-9994-44a7-b709-652c296449d5"
}
```

### Response
201 Created - Location header will provide the new resource

