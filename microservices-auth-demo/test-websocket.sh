#!/bin/bash

echo "Testing WebSocket connection to chat service..."
echo "Opening chat service page in your default browser..."

# Try to open the chat page using various browser opener commands
if command -v xdg-open &> /dev/null; then
    xdg-open http://localhost:8084/index.html
elif command -v open &> /dev/null; then
    open http://localhost:8084/index.html
elif command -v start &> /dev/null; then
    start http://localhost:8084/index.html
else
    echo "Could not open browser automatically."
    echo "Please open http://localhost:8084/index.html in your browser to test the WebSocket connection."
fi

# Print help information
echo ""
echo "WebSocket Configuration Information:"
echo "------------------------------------"
echo "STOMP endpoint: ws://localhost:8084/ws"
echo "Message destination prefix: /app"
echo "Message broker topic: /topic/public"
echo "Send chat message: /app/chat.sendMessage"
echo "Add user: /app/chat.addUser"
echo ""
echo "You can use the web interface or a WebSocket client like Postman to test the connection." 