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
sudo wget https://s3.amazonaws.com/amazoncloudwatch-agent/debian/amd64/latest/amazon-cloudwatch-agent.deb
sudo dpkg -i -E ./amazon-cloudwatch-agent.deb
# sudo variable1=$(echo "ALTER USER 'root'@'localhost' IDENTIFIED WITH mysql_native_password BY 'root'; exit" | sudo mysql)
pwd
ls -la
sudo mv /tmp/iptable.service /etc/systemd/system/iptable.service
sudo mv /tmp/amazon-cloudwatch-agent.json /opt/amazon-cloudwatch-agent.json
sudo mv /tmp/SpringBootApplication-0.0.1-SNAPSHOT.war ~/SpringBootApplication-0.0.1-SNAPSHOT.war
sudo mv ~/SpringBootApplication-0.0.1-SNAPSHOT.war ~/webapp.war
sudo rm -rf /var/lib/tomcat9/webapps/ROOT
sudo cp ~/webapp.war /var/lib/tomcat9/webapps
sudo mv /var/lib/tomcat9/webapps/webapp.war /var/lib/tomcat9/webapps/ROOT.war
sudo chmod -R 755 /etc/systemd/system/iptable.service
sudo systemctl daemon-reload
sudo systemctl enable iptable.service
sudo systemctl start iptable.service