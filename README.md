# webapp
#### Name: Dimpleben Kanjibhai Patel<br/>

### Programming Language
- Java (Jdk 11)

### Framework
- SpringBoot

### Tools
- Postman
- IntelliJ IDEA Ultimate Edition


### Steps to run the repository:
1. Install java 11.
2. Install Tomcat 9
3. Clone the repository using below command
     ```
     git@github.com:Dimple1423/webapp.git
     ```
4. Open cmd and navigate it to SpringBootApplication folder
5. run below commnad to clean the mvn build
     ```
     mvn clean
     ```
6. run the below command to create war file
     ```
     mvn package
     ```
7. move the created war file from SpringBootApplication/target to webapps folder of the tomcat
8. run the tomcat
9. Open below url in postman to check the output
   ```
   http://localhost:8080/webapp/healthz
   ```
### APIs

     post api
     http://localhost:8080/webapp/v1/account

     get api
     http://localhost:8080/webapp/v1/account/{accountid}

     put api
     http://localhost:8080/webapp/v1/account/{accountid}

     post api
     http://localhost:8080/webapp/v1/documents

     get api
     http://localhost:8080/webapp/v1/documents

     get api
     http://localhost:8080/webapp/v1/documents/{doc_id}

     delete api
     http://localhost:8080/webapp/v1/documents/{doc_id}
     
