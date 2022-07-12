## **Rate Limit And Queue API Example**

#### Architecture Flow:

![Diagram](src/main/resources/templates/diagram.png)

The application basically consists of an API that receives POST requests and tries resending them to a third party postman API, but limiting them in the client side (This API), so that some of them are able to reach the destination postman API, depending on the configured environment variables. Th remaining requests are enqueued in a Kafka queue, and polled again when the API is ready to receive more requests. These polled requests will be sent in a rate that will be not greater than the capacity of the API (rate-limiter limit), so that this last one won't be overloaded. The sequence will continue until all the pending enqueued requests are equal to 0.

It is also worthy to mention that the API uses web security configurations, so one needs to be authenticated before sending requests for sending requests to the postman API through this API.

In order to track the processing of the API, for this use case there is a file created, in which the application will read the sent requests, so that one can track the requests sent to the postman API as well as the state of the service (enqueued requests and sent ones)

#### How to use it:

1. navigate to the root directory of the application.

2. run `docker compose up -d`and wait until the images are loaded successfully.

3. run the spring boot application.

4. In order to send requests to the application, one needs to be authenticated, and those authentication permissions and claims will be verified through a JWT token, which will be needed in the subsequent requests.

5. To do so sen the following request:
   
   `curl --location --request POST 'http://localhost:8080/v0/auth' \`
   
   `--header 'Content-Type: application/json' \`
   
   `--data-raw '{`
   
   `"username":"admin",`
   
   `"password":"password"`
   
   `}'`
   
   Just remember that since this is a test the the user and passwords are provided here in these documentation and are not being generated through a 3ed party SSO or Oauth2 provider like Okta, but for the service itself. 

6. Once received the response from the last step, grab the generated JWT and send it as a header in the following request:
   
   `curl --location --request POST 'http://localhost:8080/v0/accept' \`
   
   `--header 'Authorization: Bearer <The JWT token>`
   
   to send the POST request to the running API

7. Note: The application in the LimiterQueueExampleApplication, lines 42 to 44 is running a demonstration of the functionality of the application by mimicking the sending of 30 requests to the controller, and seeing how the API behaves when a real request is sent. To see the results of that, navigate to `<root directory of this application>/src/main/resources/test.txt`.
   
   

Thank you!
