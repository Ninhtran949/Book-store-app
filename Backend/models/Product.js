const mongoose = require('mongoose');

const productSchema = new mongoose.Schema({

  
  codeCategory: {
    type: String,
    required: true,
  },
  codeProduct: {
    type: String,
    required: true,
  },
  imgProduct: {
    type: String,
    default: '',
  },
  nameProduct: {
    type: String,
    required: true,
  },
  priceProduct: {
    type: String,
    required: true,
  },
  userPartner: {
    type: String,
    required: true,
  },
}, { timestamps: true });

module.exports = mongoose.model('Product', productSchema);
