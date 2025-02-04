const express = require('express');
const router = express.Router();
const Cart = require('../models/Cart');

// Nhận `io` như tham số
module.exports = (io) => {
  
  // Lấy Cart theo userClient
  router.get('/user/:userClient', async (req, res) => {
    try {
      const carts = await Cart.find({ userClient: req.params.userClient });
      if (carts.length === 0) {
        return res.status(404).json({ message: 'No carts found for this userClient' });
      }
      res.status(200).json(carts);
    } catch (err) {
      res.status(500).json({ message: err.message });
    }
  });

  // Tạo Cart mới
  router.post('/', async (req, res) => {
    const cart = new Cart({
      idProduct: req.body.idProduct,
      idCart: req.body.idCart,
      idCategory: req.body.idCategory,
      imgProduct: req.body.imgProduct,
      idPartner: req.body.idPartner,
      nameProduct: req.body.nameProduct,
      userClient: req.body.userClient,
      priceProduct: req.body.priceProduct,
      numberProduct: req.body.numberProduct,
      totalPrice: req.body.totalPrice
    });

    try {
      const newCart = await cart.save();
      io.emit('cartCreated', newCart); // Phát sự kiện khi tạo Cart
      res.status(201).json(newCart);
    } catch (err) {
      res.status(400).json({ message: err.message });
    }
  });

  // Cập nhật Cart theo userClient
  router.patch('/user/:userClient', async (req, res) => {
    try {
      const cart = await Cart.findOne({ userClient: req.params.userClient });
      if (!cart) {
        return res.status(404).json({ message: 'Cart not found' });
      }

      // Cập nhật các trường
      if (req.body.idProduct !== undefined) cart.idProduct = req.body.idProduct; // Cập nhật idProduct
      if (req.body.idCategory !== undefined) cart.idCategory = req.body.idCategory; // Cập nhật idCategory
      if (req.body.imgProduct !== undefined) cart.imgProduct = req.body.imgProduct; // Cập nhật imgProduct
      if (req.body.idPartner !== undefined) cart.idPartner = req.body.idPartner; // Cập nhật idPartner
      if (req.body.nameProduct !== undefined) cart.nameProduct = req.body.nameProduct; // Cập nhật nameProduct
      if (req.body.userClient !== undefined) cart.userClient = req.body.userClient; // Cập nhật userClient
      if (req.body.priceProduct !== undefined) cart.priceProduct = req.body.priceProduct; // Cập nhật priceProduct
      if (req.body.numberProduct !== undefined) cart.numberProduct = req.body.numberProduct; // Cập nhật numberProduct
      if (req.body.totalPrice !== undefined) cart.totalPrice = req.body.totalPrice; // Cập nhật totalPrice

      const updatedCart = await cart.save();
      io.emit('cartUpdated', updatedCart); // Phát sự kiện khi cập nhật Cart
      res.status(200).json(updatedCart);
    } catch (err) {
      res.status(400).json({ message: err.message });
    }
  });

// Cập nhật Cart theo IdCart
router.patch('/idCart/:idCart', async (req, res) => {
  try {
      // Tìm giỏ hàng theo idCart
      const cart = await Cart.findOne({ idCart: req.params.idCart }); 
      if (!cart) {
          return res.status(404).json({ message: 'Cart not found' });
      }

      //Cập nhật các trường nếu có trong body request
      if (req.body.idProduct !== undefined) cart.idProduct = req.body.idProduct; // Cập nhật idProduct
      if (req.body.idCategory !== undefined) cart.idCategory = req.body.idCategory; // Cập nhật idCategory
      if (req.body.imgProduct !== undefined) cart.imgProduct = req.body.imgProduct; // Cập nhật imgProduct
      if (req.body.idPartner !== undefined) cart.idPartner = req.body.idPartner; // Cập nhật idPartner
      if (req.body.nameProduct !== undefined) cart.nameProduct = req.body.nameProduct; // Cập nhật nameProduct
      if (req.body.userClient !== undefined) cart.userClient = req.body.userClient; // Cập nhật userClient
      if (req.body.priceProduct !== undefined) cart.priceProduct = req.body.priceProduct; // Cập nhật priceProduct
      if (req.body.numberProduct !== undefined) cart.numberProduct = req.body.numberProduct; // Cập nhật numberProduct
      if (req.body.totalPrice !== undefined) cart.totalPrice = req.body.totalPrice; // Cập nhật totalPrice

      // Lưu thay đổi vào cơ sở dữ liệu
      const updatedCart = await cart.save();

      // Phát sự kiện khi cập nhật Cart
      io.emit('cartUpdated', updatedCart);
      
      // Trả về giỏ hàng đã được cập nhật
      res.status(200).json(updatedCart);
  } catch (err) {
      res.status(400).json({ message: err.message });
  }
});


  // Xóa Cart theo userClient
  router.delete('/user/:userClient', async (req, res) => {
    try {
      const cart = await Cart.findOne({ userClient: req.params.userClient });
      if (!cart) {
        return res.status(404).json({ message: 'Cart not found' });
      }

      await cart.deleteOne();
      // io.emit('cartDeleted', { userClient: req.params.userClient }); // Phát sự kiện khi xóa Cart
      res.status(200).json({ message: 'Cart deleted' });
    } catch (err) {
      res.status(500).json({ message: err.message });
    }
  });

    // Xóa Cart theo idcart
    router.delete('/:idCart', async (req, res) => {
      try {
        const cart = await Cart.findOne({ idCart: req.params.idCart });
        if (!cart) {
          return res.status(404).json({ message: 'Cart not found' });
        }
  
        await cart.deleteOne();
        io.emit('cartDeleted', { userClient: req.params.userClient }); // Phát sự kiện khi xóa Cart
        res.status(200).json({ message: 'Cart deleted' });
      } catch (err) {
        res.status(500).json({ message: err.message });
      }
    });

  return router;
};
