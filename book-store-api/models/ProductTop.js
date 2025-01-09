const mongoose = require('mongoose');

// Define ProductTop schema
const productTopSchema = new mongoose.Schema({

  
  
  amountProduct: {
    type: Number,
    required: true
  },
  idCategory: {
    type: Number,
    required: true
  },
  idProduct: {
    type: Number,
    required: true
  }
});

// Export ProductTop model
module.exports = mongoose.model('ProductTop', productTopSchema);
