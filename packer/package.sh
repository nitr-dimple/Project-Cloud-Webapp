#!/bin/bash
sleep 30
sudo apt-get update
sudo apt-get upgrade -y
sudo apt-get install nginx -y
sudo apt-get clean

sudo apt install openjdk-11-jre-headless -y
sudo apt install maven -y
# sudo apt install mysql-server -y
sudo apt install tomcat9 tomcat9-admin -y
sudo ufw allow from any to any port 8080 proto tcp
sudo apt install mysql-client -y
# sudo variable1=$(echo "ALTER USER 'root'@'localhost' IDENTIFIED WITH mysql_native_password BY 'root'; exit" | sudo mysql)
pwd
ls -la
sudo mv /tmp/SpringBootApplication-0.0.1-SNAPSHOT.war ~/SpringBootApplication-0.0.1-SNAPSHOT.war
sudo mv ~/SpringBootApplication-0.0.1-SNAPSHOT.war ~/webapp.war
sudo cp ~/webapp.war /var/lib/tomcat9/webapps