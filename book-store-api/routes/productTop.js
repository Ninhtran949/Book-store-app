const express = require('express');
const router = express.Router();
const ProductTop = require('../models/ProductTop');


// GET all ProductTops
router.get('/', async (req, res) => {
  try {
    const productTops = await ProductTop.find();
    res.json(productTops);
  } catch (err) {
    res.status(500).json({ message: err.message });
  }
});

// GET a single ProductTop by ID
router.get('/:id', async (req, res) => {
  try {
    const productTop = await ProductTop.findById(req.params.id);
    if (!productTop) return res.status(404).json({ message: 'ProductTop not found' });
    res.json(productTop);
  } catch (err) {
    res.status(500).json({ message: err.message });
  }
});

// CREATE a new ProductTop
router.post('/productTop/create', async (req, res) => {
  const productTop = new ProductTop({
    amountProduct: req.body.amountProduct,
    idCategory: req.body.idCategory,
    idProduct: req.body.idProduct
  });

  try {
    const newProductTop = await productTop.save();
    res.status(201).json(newProductTop);
  } catch (err) {
    res.status(400).json({ message: err.message });
  }
});

// Cập nhật một ProductTop bằng Idproduct
router.patch('/idProduct/:idProduct', async (req, res) => {
  
  try {
    const idProduct = req.params.idProduct;
    const productTop = await ProductTop.findOne({ idProduct: idProduct });

    if (!productTop) return res.status(404).json({ message: 'ProductTop not found' });

    // Cập nhật các trường nếu có trong body
    if (req.body.amountProduct !== undefined) productTop.amountProduct = req.body.amountProduct;
    if (req.body.idCategory !== undefined) productTop.idCategory = req.body.idCategory;
    // if (req.body.idProduct !== undefined) productTop.idProduct = req.body.idProduct;

    const updatedProductTop = await productTop.save();
    res.json(updatedProductTop);
  } catch (err) {
    res.status(400).json({ message: err.message });
  }
});

// DELETE a ProductTop by IDProcduct
router.delete('/delete/:idProduct', async (req, res) => {
  try {
    const idProduct = req.params.idProduct;
    const productTop = await ProductTop.findOne({ idProduct: idProduct });

    if (!productTop) return res.status(404).json({ message: 'ProductTop not found' });

    await productTop.deleteOne();
    res.json({ message: 'Deleted ProductTop' });
  } catch (err) {
    res.status(500).json({ message: err.message });
  }
});

module.exports = router;
