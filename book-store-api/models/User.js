const mongoose = require('mongoose');
const CryptoJS = require('crypto-js');

const secretKey = process.env.ENCRYPTION_SECRET_KEY; // khóa bí mật từ biến môi trường

// Define User schema
const userSchema = new mongoose.Schema({
  address: {
    type: String,
    required: true,
    set: encrypt,
    get: decrypt
  },
  id: {
    type: String,
    required: true,

  },
  name: {
    type: String,
    required: true,
    set: encrypt,
    get: decrypt
  },
  password: {
    type: String,
    required: true,
    set: encrypt,
    get: decrypt
  },
  phoneNumber: {
    type: String,
    required: true
  },
  strUriAvatar: {
    type: String,
    default: '',
    set: encrypt,
    get: decrypt
  }
}, { toJSON: { getters: true } });

// Function to encrypt data
function encrypt(value) {
  return CryptoJS.AES.encrypt(value, secretKey).toString();
}

// Function to decrypt data
function decrypt(value) {
  const bytes = CryptoJS.AES.decrypt(value, secretKey);
  return bytes.toString(CryptoJS.enc.Utf8);
}

// Export User model
module.exports = mongoose.model('User', userSchema);
