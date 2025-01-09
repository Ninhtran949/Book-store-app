const express = require('express');
const router = express.Router();
const Bill = require('../models/Bill');

// Nhận io như tham số
module.exports = (io) => {
  // Lấy tất cả Bill
  router.get('/', async (req, res) => {
    try {
      const bills = await Bill.find();
      res.status(200).json(bills);
    } catch (err) {
      res.status(500).json({ message: err.message });
    }
  });

  // Lấy CART theo idBill
  router.get('/cart/:idBill', async (req, res) => {
    try {
      const bill = await Bill.findOne({ idBill: req.params.idBill });
      if (!bill) {
        return res.status(404).json({ message: 'Bill not found' });
      }
      res.status(200).json(bill.Cart); // Lấy CART trong BILL
    } catch (err) {
      res.status(500).json({ message: err.message });
    }
  });

    // Lấy CART theo idBill
    router.get('/:idBill/cart', async (req, res) => {
      try {
        const bill = await Bill.findOne({ idBill: req.params.idBill });
        if (!bill) {
          return res.status(404).json({ message: 'Bill not found' });
        }
        res.status(200).json(bill.Cart); // Lấy CART trong BILL
      } catch (err) {
        res.status(500).json({ message: err.message });
      }
    });

  // Lấy bill theo idBill
  router.get('/:idBill', async (req, res) => {
    try {
      const bill = await Bill.findOne({ idBill: req.params.idBill });
      if (!bill) {
        return res.status(404).json({ message: 'Bill not found' });
      }
      res.status(200).json(bill); 
    } catch (err) {
      res.status(500).json({ message: err.message });
    }
  });

// Lấy bill theo idPartner and status "Yes"
router.get('/idPartner/:idPartner', async (req, res) => {
  try {
    const idPartner = req.params.idPartner; // Sử dụng req.params thay vì req.query
    const bills = await Bill.find({ idPartner: idPartner, status: "Yes" });
    
    if (!bills || bills.length === 0) {
      return res.status(404).json({ message: 'Bills not found' });
    }
    
    res.status(200).json(bills); 
  } catch (err) {
    res.status(500).json({ message: err.message });
  }
});

  // Tạo Bill mới
  router.post('/addBill', async (req, res) => {
    // Kiểm tra kiểu dữ liệu
    if (typeof req.body.idBill !== 'number') {
      return res.status(400).json({ message: 'idBill must be a number' });
    }
    
    const bill = new Bill({
      Cart: req.body.Cart,
      dayOut: req.body.dayOut,
      idBill: req.body.idBill,
      idClient: req.body.idClient,
      idPartner: req.body.idPartner,
      status: req.body.status,
      timeOut: req.body.timeOut,
      total: req.body.total
    });

    try {
      const newBill = await bill.save();
      // Phát sự kiện "billCreated" cho client
      io.emit('billCreated', newBill);
      res.status(201).json(newBill);
    } catch (err) {
      res.status(400).json({ message: err.message });
    }
  });

  router.patch('/:idBill/status', async (req, res) => {
    try {
      const bill = await Bill.findOne({ idBill: req.params.idBill });
      if (!bill) {
        return res.status(404).json({ message: 'Bill not found' });
      }
  
      // Lấy status từ JSON body
      if (req.body.status) {
        bill.status = req.body.status;
      }
  
      const updatedBill = await bill.save();
      
      // Phát sự kiện "billUpdated" cho client
      io.emit('billUpdated', updatedBill);
      res.status(200).json(updatedBill);
    } catch (err) {
      res.status(400).json({ message: err.message });
    }
  });
  
  // Xóa Bill theo idBill
  router.delete('/:idBill', async (req, res) => {
    try {
      const bill = await Bill.findOne({ idBill: req.params.idBill });
      if (!bill) {
        return res.status(404).json({ message: 'Bill not found' });
      }

      await bill.deleteOne();
      // Phát sự kiện "billDeleted" cho client
      io.emit('billDeleted', { idBill: req.params.idBill });
      res.status(200).json({ message: 'Bill deleted' });
    } catch (err) {
      res.status(500).json({ message: err.message });
    }
  });

  // Thêm Cart mới vào Bill theo idBill
  router.post('/:idBill/cart', async (req, res) => {
    try {
      const bill = await Bill.findOne({ idBill: req.params.idBill });
      if (!bill) {
        return res.status(404).json({ message: 'Bill not found' });
      }

      // Thêm đối tượng Cart mới vào mảng Cart
      bill.Cart.push(req.body);

      const updatedBill = await bill.save();
      // Phát sự kiện "cartAdded" cho client
      io.emit('cartAdded', updatedBill);

      res.status(200).json(updatedBill);
    } catch (err) {
      res.status(400).json({ message: err.message });
    }
  });

// Lấy tất cả Cart theo userClient
router.get('/cart/user/:userClient', async (req, res) => {
  try {
    const bills = await Bill.find({
      'Cart.userClient': req.params.userClient // Lọc theo userClient trong Cart
    });

    // Tạo mảng chứa các Cart với userClient khớp
    const userCarts = bills.flatMap(bill => bill.Cart.filter(cart => cart.userClient === req.params.userClient));
    
    if (userCarts.length === 0) {
      return res.status(404).json({ message: 'No carts found for this userClient' });
    }

    res.status(200).json(userCarts);
  } catch (err) {
    res.status(500).json({ message: err.message });
  }
});

// Lấy Bill theo idPartner hoặc idClient với status "No"
router.get('/status/no/:user', async (req, res) => {
  try {
    const bills = await Bill.find({
      $or: [
        { idPartner: req.params.user, status: "No" },
        { idClient: req.params.user, status: "No" }
      ]
    });

    if (bills.length === 0) {
      return res.status(404).json({ message: 'No bills found for this user with status No' });
    }

    res.status(200).json(bills);
  } catch (err) {
    res.status(500).json({ message: err.message });
  }
});

// Lấy Bill trong khoảng thời gian theo user
router.get('/range', async (req, res) => {
  const { user, startDate, endDate } = req.query;

  // Kiểm tra xem tất cả các tham số cần thiết đã được cung cấp
  if (!user || !startDate || !endDate) {
    return res.status(400).json({ message: 'Vui lòng cung cấp user, startDate và endDate' });
  }

  // Log các tham số nhận được
  console.log(`User: ${user}, Start Date: ${startDate}, End Date: ${endDate}`);

  try {
    // Chuyển đổi startDate và endDate thành đối tượng Date
    const start = new Date(startDate);
    const end = new Date(endDate);
    
    // Kiểm tra xem các ngày có hợp lệ không
    if (isNaN(start) || isNaN(end)) {
      return res.status(400).json({ message: 'Ngày không hợp lệ' });
    }

    // Tìm các bill theo idPartner hoặc idClient và trong khoảng thời gian
    const bills = await Bill.find({
      $or: [
        { idPartner: user, dayOut: { $gte: start, $lte: end } },
        { idClient: user, dayOut: { $gte: start, $lte: end } }
      ]
    });

    console.log(`Found Bills: ${bills.length}`);

    if (bills.length === 0) {
      return res.status(404).json({ message: 'Không tìm thấy hóa đơn trong khoảng thời gian này' });
    }

    res.status(200).json(bills);
  } catch (err) {
    console.error(err);
    res.status(500).json({ message: err.message });
  }
});



  return router; // Đảm bảo trả về router
};
