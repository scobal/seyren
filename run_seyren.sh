#!/usr/bin/env bash

export GRAPHITE_URL='' #The location of your Graphite server. Default: http://localhost:80
export GRAPHITE_USERNAME='' #The HTTP Basic auth username for the Graphite server. Default: ``
export GRAPHITE_PASSWORD='' #The HTTP Basic auth password for the Graphite server. Default: ``
export GRAPHITE_KEYSTORE='' #The HTTP KeyStore path for the https Graphite server. Default: ``
export GRAPHITE_KEYSTORE_PASSWORD='' #The HTTP KeyStore password for the HTTPS Graphite server. Default: ``
export GRAPHITE_TRUSTSTORE='' #The HTTP TrustStore path for the https Graphite server. Default: ``
export MONGO_URL='mongodb://localhost:27017/seyren' #The Mongo connection string.
export SEYREN_PORT='8080' # Port for seyren to run on
export SEYREN_URL='http://localhost:8080/seyren' #The location of your Seyren instance.

export SMTP_HOST='localhost' #The smtp server to send email notifications from. Default: localhost
export SMTP_PORT='25' #The smtp server port.
export SMTP_FROM='alert@seyren' #The from email address for sending out notifications.
export SMTP_USERNAME='' #The smtp server username if authenticated SMTP is used. Default: ``
export SMTP_PASSWORD='' #The smtp server password if authenticated SMTP is used. Default: ``
export SMTP_PROTOCOL='smtp' #The smtp server protocol if authenticated SMTP is used.

export HIPCHAT_AUTHTOKEN='' #The hipchat api auth token. Default: ``
export HIPCHAT_USERNAME='Seyren Alert' #The username that messages will be sent from.

export PAGERDUTY_DOMAIN='' #The PagerDuty domain to be notified. Default: ``
export PAGERDUTY_USERNAME='' #The PagerDuty API username. Default: ``
export PAGERDUTY_PASSWORD='' #The PagerDuty API Password. Default: ``

export HUBOT_URL='' #The location where Hubot is running. Default ``

export FLOWDOCK_EXTERNAL_USERNAME='Seyren' #The username that messages will be sent from to a flow.
export FLOWDOCK_TAGS='' #Special tags to add to all messages. Default: ``
export FLOWDOCK_EMOJIS='' #Mapping between state and emojis unicode. Default: ``

java -jar seyren-web/target/dependency/jetty-runner.jar --port $SEYREN_PORT seyren-web/target/*.war
