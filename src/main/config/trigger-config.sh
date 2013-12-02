#!/bin/bash
#
# Config file for trigger-on-new-batch.sh
#
# Author: jrg
#

# Where the received newspaper batches are placed
path_to_dir_of_batches='/batches'

# Fedora location
url_to_doms='${fedora.server}'

# Username for calls to DOMS
doms_username='${fedora.admin.username}'

# Password for calls to DOMS
doms_password='${fedora.admin.password}'

# Location of PID generator
url_to_pid_gen='${pidgenerator.location}'

# Name of the trigger-script
trigger_name='register-batch-trigger'

# Location of var folder for storing processed batches
donedir="$HOME/var/batches-done"