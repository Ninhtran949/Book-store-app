const mongoose = require('mongoose');

const cartSchema = new mongoose.Schema({
  
  idProduct: { type: Number, default: '0' },
  idCart: { type: Number, default: '0' },
  idCategory: { type: Number, default: '0' },
  imgProduct: { type: String, default: '0' },
  idPartner: { type: String, default: '0' },
  nameProduct: { type: String, default: '0' },
  userClient: { type: String, default: '0'},
  priceProduct: { type: Number, default: '0' },
  numberProduct: { type: Number, default: '0' },
  totalPrice: { type: Number, default: '0' }
});

module.exports = mongoose.model('Cart', cartSchema);
