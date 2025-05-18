const WebSocket = require('ws');

// Test different WebSocket endpoints
const endpoints = [
    'ws://localhost:8084/ws',
    'ws://localhost:8080/ws',
    'ws://localhost:8082/ws',
    'ws://localhost:8084/ws/info',
    'ws://localhost:8084/api/chat/ws'
];

function testEndpoint(url) {
    console.log(`Testing connection to: ${url}`);
    
    const ws = new WebSocket(url);
    
    ws.on('open', function open() {
        console.log(`[SUCCESS] Connected to ${url}`);
        
        // Send a simple message
        const message = {
            type: 'JOIN',
            sender: 'test-script',
            content: 'Test message'
        };
        
        ws.send(JSON.stringify(message));
        console.log(`Sent message to ${url}`);
        
        // Close after 2 seconds
        setTimeout(() => {
            ws.close();
            console.log(`Closed connection to ${url}`);
        }, 2000);
    });
    
    ws.on('message', function incoming(data) {
        console.log(`Received from ${url}: ${data}`);
    });
    
    ws.on('error', function error(err) {
        console.log(`[ERROR] Could not connect to ${url}: ${err.message}`);
        
        // Close on error
        try {
            ws.close();
        } catch (e) {
            // Ignore
        }
    });
    
    ws.on('close', function close(code, reason) {
        console.log(`[CLOSED] Connection to ${url} closed with code ${code}${reason ? `, reason: ${reason}` : ''}`);
    });
    
    // Set a timeout to prevent hanging
    setTimeout(() => {
        if (ws.readyState !== WebSocket.OPEN) {
            console.log(`[TIMEOUT] Connection attempt to ${url} timed out`);
            try {
                ws.close();
            } catch (e) {
                // Ignore
            }
        }
    }, 5000);
}

// Test each endpoint with a delay between attempts
endpoints.forEach((endpoint, index) => {
    setTimeout(() => {
        testEndpoint(endpoint);
    }, index * 6000); // 6 seconds between each test
});

console.log("WebSocket test script started"); 