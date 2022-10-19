#!/bin/bash
sleep 30
sudo apt-get update
sudo apt-get upgrade -y
sudo apt-get install nginx -y
sudo apt-get clean

sudo apt install openjdk-11-jre-headless -y
sudo apt install maven -y
sudo apt install mysql-server -y
sudo variable1=$(echo "ALTER USER 'root'@'localhost' IDENTIFIED WITH mysql_native_password BY 'root'; exit" | sudo mysql)
pwd
ls -la
mv /tmp/webservice.service /etc/systemd/system/webservice.service
sudo systemctl enable webservice.service
sudo systemctl start webservice.service