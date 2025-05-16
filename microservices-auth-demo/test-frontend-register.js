// Simple test script to simulate frontend registration
const axios = require('axios');

async function testRegistration() {
  console.log('Testing registration functionality...');
  
  // Generate a unique username to avoid conflicts
  const uniqueId = Math.floor(Math.random() * 10000);
  const username = `testuser${uniqueId}`;
  const email = `testuser${uniqueId}@example.com`;
  const password = 'password123';
  
  try {
    // Test registration with new credentials
    const registerResponse = await axios.post('http://localhost:8080/api/auth/register', {
      username,
      email,
      password
    }, {
      headers: {
        'Content-Type': 'application/json',
        'Accept': 'application/json'
      }
    });
    
    console.log('Registration successful!');
    console.log('Token received:', registerResponse.data.token);
    console.log('User info:', {
      username: registerResponse.data.username,
      email: registerResponse.data.email,
      roles: registerResponse.data.roles
    });
    
    // Test accessing a protected endpoint with the token
    try {
      const protectedResponse = await axios.get('http://localhost:8080/api/chat/private/status', {
        headers: {
          'Authorization': `Bearer ${registerResponse.data.token}`
        }
      });
      
      console.log('\nAccessing protected endpoint successful!');
      console.log('Response:', protectedResponse.data);
    } catch (protectedError) {
      console.error('\nError accessing protected endpoint:', protectedError.message);
      if (protectedError.response) {
        console.error('Status:', protectedError.response.status);
        console.error('Data:', protectedError.response.data);
      }
    }
    
  } catch (error) {
    console.error('Registration failed:', error.message);
    if (error.response) {
      console.error('Status:', error.response.status);
      console.error('Data:', error.response.data);
    }
  }
}

// Run the test
testRegistration(); 