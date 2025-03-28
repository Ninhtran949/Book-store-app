const WebSocket = require('ws');

const clients = new Map();

function setupWebSocket(server) {
    const wss = new WebSocket.Server({ server });

    wss.on('connection', (ws) => {
        console.log('A new client connected!');

        ws.on('message', (data) => {
            const message = JSON.parse(data);
            console.log('Received from client:', message);

            if (message.type === 'setName') {
                const nameExists = Array.from(clients.values()).includes(message.name);

                if (nameExists) {
                    ws.send(JSON.stringify({ type: 'error', message: 'Username already exists. Please choose a different name.' }));
                } else {
                    ws.send(JSON.stringify({ type: 'success' }));
                    clients.set(ws, message.name);
                    const welcomeMessage = ` has joined the chat!`;

                    wss.clients.forEach((client) => {
                        if (client.readyState === WebSocket.OPEN) {
                            client.send(JSON.stringify({ type: 'serverMessage', name: message.name, message: welcomeMessage }));
                        }
                    });
                }
            }

            if (message.type === 'chat') {
                const senderName = clients.get(ws);
                const chatMessage = `${message.text}`;

                wss.clients.forEach((client) => {
                    if (client.readyState === WebSocket.OPEN) {
                        client.send(JSON.stringify({ type: 'chat', sender: senderName, message: chatMessage }));
                    }
                });
            }
        });

        ws.on('close', () => {
            const name = clients.get(ws);

            if (name) {
                wss.clients.forEach((client) => {
                    if (client.readyState === WebSocket.OPEN) {
                        client.send(JSON.stringify({ type: 'serverMessage', message: `${name} has left the chat.` }));
                    }
                });
                console.log(`${name} disconnected`);
                clients.delete(ws);
            }
        });
    });

    console.log('WebSocket server is running');
}

module.exports = setupWebSocket;