#!/bin/sh

. /home/sdc/scripts/cassandra-env_test.sh  # Replace with the actual path to your env file

# Ensure necessary variables are defined
if [ -z "$CASSANDRA_IP" ] || [ -z "$CS_PORT" ] || [ -z "$SDC_USER" ] || [ -z "$SDC_PASSWORD" ]; then
    echo "One or more required environment variables are missing!"
    exit 1
fi

# Function to test CS_PASSWORD with cqlsh
test_password() {
  local password=$1
  echo "Testing password: $password"
  echo "SELECT release_version FROM system.local;" | cqlsh ${CASSANDRA_IP} ${CS_PORT} -u cassandra -p "$password" > /dev/null 2>&1
  return $?  # Returns 0 if successful, non-zero otherwise
}

# Try connecting with the default passwords
if test_password "onap123#@!"; then
  export CS_PASSWORD="onap123#@!"
elif test_password "cassandra"; then
  export CS_PASSWORD="cassandra"
else
  echo "Failed to connect to Cassandra with the tested passwords." >&2
  exit 1
fi

echo "Using CS_PASSWORD: $CS_PASSWORD"

# Write the detected password to a temporary environment file
echo "export CS_PASSWORD=\"$CS_PASSWORD\"" 
