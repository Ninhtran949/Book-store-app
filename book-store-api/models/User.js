const mongoose = require('mongoose');

// Define User schema
const userSchema = new mongoose.Schema({

  
  
  address: {
    type: String,
    required: true
  },
  id: {
    type: String,
    required: true
  },
  name: {
    type: String,
    required: true
  },
  password: {
    type: String,
    required: true
  },
  phoneNumber: {
    type: String,
    required: true
  },
  strUriAvatar: {
    type: String,
    default: ''
  }
});

// Export User model
module.exports = mongoose.model('User', userSchema);
