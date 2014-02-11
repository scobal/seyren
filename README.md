Practo's Alerting Dashboard
===================

This git repository is a forked instance of the orignal git repository https://github.com/scobal/seyren. An  additional change has been made to this git repository. The practo's version of seyren accept's an extra parameter called time threshold.This extra parameter makes Seyren initiates an alarm only when the time limit in error or warn state crosses the time threshold.The input to time threshold is taken in minutes.This was developed by Naveed Mohad Abdul http://www.linkedin.com/pub/naveed-mohad-abdul/25/268/b8 as an internship project at Practo https://www.practo.com/. 

Prerequisites
----------------------------
 * Graphite 
 * MongoDB
 * Maven
 * Collectd


Installing Graphite is a tedious task.To make things simpler there is a portable vagrant graphite instance at https://github.com/Jimdo/vagrant-statsd-graphite-puppet.

All other prerequisites can be downloaded from the package manager of any linux distro.

Configure your Collectd to write to Graphite.


Running on Linux or Mac
----------------------------
For instructions on Running Practo's version of Seyren go to https://github.com/scobal/seyren.
