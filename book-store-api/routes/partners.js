const express = require('express');
const router = express.Router();
const Partner = require('../models/Partner');

// Lấy tất cả các Partner
router.get('/', async (req, res) => {
  try {
    const partners = await Partner.find();
    res.status(200).json(partners);
  } catch (err) {
    res.status(500).json({ message: err.message });
  }
});

// Lấy Partner theo idPartner 
router.get('/:idPartner', async (req, res) => {
  try {
    const idPartner = req.params.idPartner;
    const partner = await Partner.findOne({ idPartner: idPartner });
    if (!partner) return res.status(404).json({ message: 'Partner not found' });
    res.status(200).json(partner);
  } catch (err) {
    res.status(500).json({ message: err.message });
  }
});

// Tạo mới Partner
router.post('/', async (req, res) => {
  // Kiểm tra dữ liệu từ req.body
  const { addressPartner, idPartner, imgPartner, namePartner, passwordPartner, userPartner } = req.body;

  // Xác thực dữ liệu
  if (!addressPartner || !idPartner || !namePartner || !passwordPartner || !userPartner) {
    return res.status(400).json({ message: "Vui lòng cung cấp đầy đủ thông tin đối tác." });
  }

  try {
    // Kiểm tra xem idPartner đã tồn tại chưa
    const existingPartner = await Partner.findOne({ idPartner });
    if (existingPartner) {
      return res.status(400).json({ message: "ID đối tác đã tồn tại." });
    }

    // Tạo đối tượng Partner mới
    const partner = new Partner({
      addressPartner,
      idPartner,
      imgPartner,
      namePartner,
      passwordPartner,
      userPartner
    });

    // Lưu đối tác mới vào database
    const newPartner = await partner.save();
    res.status(201).json(newPartner);

  } catch (err) {
    res.status(500).json({ message: err.message }); // Sử dụng mã lỗi 500 cho lỗi server
  }
});

// Cập nhật Partner theo idPartner (ép kiểu thành số)
router.patch('/:idPartner', async (req, res) => {
  try {
    const idPartner = req.params.idPartner;
    const partner = await Partner.findOne({ idPartner: idPartner });
    if (!partner) return res.status(404).json({ message: 'Partner not found' });

    // Cập nhật các trường nếu có trong request body
    if (req.body.addressPartner) partner.addressPartner = req.body.addressPartner;
    if (req.body.imgPartner) partner.imgPartner = req.body.imgPartner;
    if (req.body.namePartner) partner.namePartner = req.body.namePartner;
    if (req.body.passwordPartner) partner.passwordPartner = req.body.passwordPartner;
    if (req.body.userPartner) partner.userPartner = req.body.userPartner;

    const updatedPartner = await partner.save();
    res.status(200).json(updatedPartner);
  } catch (err) {
    res.status(400).json({ message: err.message });
  }
});

// Xóa Partner theo idPartner
router.delete('/idPartner/:idPartner', async (req, res) => {
  try {
    const idPartner = req.params.idPartner;
    const partner = await Partner.findOne({ idPartner: idPartner });
    if (!partner) return res.status(404).json({ message: 'Partner not found' });

    await partner.deleteOne();
    res.json({ message: 'Deleted Partner' });
  } catch (err) {
    res.status(500).json({ message: err.message });
  }
});





module.exports = router;
