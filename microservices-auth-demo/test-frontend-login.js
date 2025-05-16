// Simple test script to simulate frontend login
const axios = require('axios');

async function testLogin() {
  console.log('Testing login functionality...');
  
  try {
    // Test login with valid credentials
    const loginResponse = await axios.post('http://localhost:8080/api/auth/authenticate', {
      username: 'testuser4',
      password: 'password123'
    }, {
      headers: {
        'Content-Type': 'application/json',
        'Accept': 'application/json'
      }
    });
    
    console.log('Login successful!');
    console.log('Token received:', loginResponse.data.token);
    console.log('User info:', {
      username: loginResponse.data.username,
      email: loginResponse.data.email,
      roles: loginResponse.data.roles
    });
    
    // Test accessing a protected endpoint with the token
    try {
      const protectedResponse = await axios.get('http://localhost:8080/api/chat/private/status', {
        headers: {
          'Authorization': `Bearer ${loginResponse.data.token}`
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
    console.error('Login failed:', error.message);
    if (error.response) {
      console.error('Status:', error.response.status);
      console.error('Data:', error.response.data);
    }
  }
}

// Run the test
testLogin(); 