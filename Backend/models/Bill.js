const mongoose = require('mongoose');

const cartSchema = new mongoose.Schema({
  
  idCart: Number,
  idCategory: Number,
  idPartner: String,
  idProduct: Number,
  imgProduct: { type: String, default: '' },
  nameProduct: String,
  numberProduct: Number,
  priceProduct: Number,
  totalPrice: Number,
  userClient: String
});

const billSchema = new mongoose.Schema({
  
  Cart: [cartSchema],
  dayOut: String,
  idBill: Number,
  idClient: String,
  idPartner: String,
  status: String,
  timeOut: String,
  total: Number
});

module.exports = mongoose.model('Bill', billSchema);
