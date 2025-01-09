const mongoose = require('mongoose');

const partnerSchema = new mongoose.Schema({
  
  addressPartner: String,
  idPartner: Number,
  imgPartner: { type: String, default: '' },
  namePartner: String,
  passwordPartner: String,
  userPartner: String
});

module.exports = mongoose.model('Partner', partnerSchema);
