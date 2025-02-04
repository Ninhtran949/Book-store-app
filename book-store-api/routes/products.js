const express = require('express');
const router = express.Router();
const Product = require('../models/Product'); // Đảm bảo đường dẫn đúng đến model Product

// Lấy tất cả sản phẩm
router.get('/', async (req, res) => {
  try {
      console.log("Fetching all products...");
      const products = await Product.find();
      // console.log("Products fetched:", products);
      if (products.length === 0) return res.status(404).json({ message: "No products found" });
      res.json(products);
  } catch (err) {
      console.error("Error fetching products:", err);
      res.status(500).json({ message: err.message });
  }
});


// Lấy một sản phẩm cụ thể theo ID
router.get('/:id', getProduct, (req, res) => {
  res.json(res.product);
});

// Thêm một sản phẩm mới
router.post('/', async (req, res) => {
  const product = new Product({
    codeCategory: req.body.codeCategory,
    codeProduct: req.body.codeProduct,
    imgProduct: req.body.imgProduct,
    nameProduct: req.body.nameProduct,
    priceProduct: req.body.priceProduct,
    userPartner: req.body.userPartner
  });

  try {
    const newProduct = await product.save();
    res.status(201).json(newProduct);
  } catch (err) {
    res.status(400).json({ message: err.message });
  }
});
//update
router.patch('/:codeProduct', async (req, res) => {
  try {
    const codeProduct = req.params.codeProduct;
    
    // Tìm sản phẩm theo mã sản phẩm
    const product = await Product.findOne({ codeProduct: codeProduct });
    if (!product) return res.status(404).json({ message: 'Product not found' });

    // Cập nhật các trường nếu có trong request body
    if (req.body.codeCategory !== undefined) {
      product.codeCategory = req.body.codeCategory;
    }
    if (req.body.codeProduct !== undefined) {
      product.codeProduct = req.body.codeProduct;
    }
    if (req.body.imgProduct !== undefined) {
      product.imgProduct = req.body.imgProduct;
    }
    if (req.body.nameProduct !== undefined) {
      product.nameProduct = req.body.nameProduct;
    }
    if (req.body.priceProduct !== undefined) {
      product.priceProduct = req.body.priceProduct;
    }
    if (req.body.userPartner !== undefined) {
      product.userPartner = req.body.userPartner;
    }

    // Lưu lại sản phẩm đã cập nhật
    const updatedProduct = await product.save();
    res.json(updatedProduct);
  } catch (err) {
    res.status(400).json({ message: err.message });
  }
});

// xóa theo codeproduct
router.delete('/:codeProduct', async (req, res) => {
  try {
    const codeProduct = req.params.codeProduct;

    // Tìm sản phẩm theo mã sản phẩm
    const product = await Product.findOne({ codeProduct: codeProduct });
    if (!product) return res.status(404).json({ message: 'Product not found' });

    // Xóa sản phẩm
    await product.deleteOne();
    res.json({ message: 'Deleted Product' });
  } catch (err) {
    res.status(500).json({ message: err.message });
  }
});


// Middleware để lấy sản phẩm theo ID
async function getProduct(req, res, next) {
  let product;
  try {
    product = await Product.findById(req.params.id);
    if (product == null) {
      return res.status(404).json({ message: 'Cannot find product' });
    }
  } catch (err) {
    return res.status(500).json({ message: err.message });
  }
  res.product = product;
  next();
}

router.get('/category/:codeCategory', async (req, res) => {
  try {
      const products = await Product.find({ codeCategory: req.params.codeCategory });
      if (products.length === 0) {
          return res.status(404).json({ message: "No products found for this category" });
      }
      res.json(products);
  } catch (err) {
      res.status(500).json({ message: err.message });
  }
});


module.exports = router;
