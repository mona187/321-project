#!/bin/bash
# waits 30 x 5 = 150 = 2.5 minutes for the server to boot 
echo "Waiting for backend to be up..."
success=0
for i in {1..30}; do
    if curl -fs http://3.135.231.73:3000/health; then
        printf "\n~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\nServer is up!\n~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n"
        success=1
        break
    fi
    printf "Still waiting... (%d/30)\n" "$i"
    sleep 5
done

# if server does not boot we quit out
if [ "$success" -ne 1 ]; then
    printf "\nERROR: Server did not start within %d attempts (%d seconds).\n" 30 $((30 * 5))
    exit 1
fi