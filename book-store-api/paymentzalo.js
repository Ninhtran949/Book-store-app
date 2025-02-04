// paymentzalo.js
const axios = require('axios').default;
const CryptoJS = require('crypto-js');
const moment = require('moment');
const qs = require('qs');
const express = require('express');
const router = express.Router();

// Cấu hình ZaloPay
const config = {
  app_id: '2553',
  key1: 'PcY4iZIKFCIdgZvA6ueMcMHHUbRLYjPL',
  key2: 'kLtgPl8HHhfvMuDHPwKfgfsY4Ydm9eIz',
  endpoint: 'https://sb-openapi.zalopay.vn/v2/create',
};

//  (Endpoint tạo đơn hàng và thanh toán)
router.post('/payment', async (req, res) => {
    const { app_user, amount, description } = req.body;
    const embed_data = {
        //sau khi hoàn tất thanh toán sẽ đi vào link này ngrok http 8080
        redirecturl: 'https://d945-14-190-92-233.ngrok-free.app/zalopay/return',
    };
  
    const items = [];
    const transID = Math.floor(Math.random() * 1000000);
  
    const order = {
      app_id: config.app_id,
      app_trans_id: `${moment().format('YYMMDD')}_${transID}`,
      app_user,
      app_time: Date.now(),
      item: JSON.stringify(items),
      embed_data: JSON.stringify(embed_data),
      amount,
      callback_url: 'https://d945-14-190-92-233.ngrok-free.app/zalopay/callback', //  URL server 
      description,
      bank_code: '',
      return_url: 'https://d945-14-190-92-233.ngrok-free.app/zalopay/return', 
    };
  
    const data = `${config.app_id}|${order.app_trans_id}|${order.app_user}|${order.amount}|${order.app_time}|${order.embed_data}|${order.item}`;
    order.mac = CryptoJS.HmacSHA256(data, config.key1).toString();
  
    try {
      const result = await axios.post(config.endpoint, null, { params: order });
      console.log("Order URL:", result.data.order_url);
      return res.status(200).json(result.data);
      
    } catch (error) {
      console.log(error.response ? error.response.data : error.message);
      return res.status(500).json({ error: 'Internal Server Error' });
    }
    
  });
  
// Endpoint return để ZaloPay gọi khi thanh toán hoàn tất
router.get('/return', (req, res) => {
  const { status, apptransid } = req.query; // Các tham số tùy thuộc vào ZaloPay
  console.log('Received return request:', req.query); // Thêm dòng log này

  // Xây dựng URL redirect tới ứng dụng Android với các tham số cần thiết
  const redirectUrl = `yourapp://paymentresult?status=${status}&apptransid=${apptransid}`;

  // Redirect tới ứng dụng Android
  res.redirect(redirectUrl);
});



// Endpoint callback để ZaloPay gọi khi thanh toán thành công
router.post('/callback', (req, res) => {
  let result = {};
  console.log(req.body);
  try {
    let dataStr = req.body.data;
    let reqMac = req.body.mac;

    let mac = CryptoJS.HmacSHA256(dataStr, config.key2).toString();
    console.log('Client MAC:', reqMac);
    console.log('Server MAC:', mac);

    if (reqMac !== mac) {
      result.return_code = -1;
      result.return_message = 'mac not equal';
    } else {
      let dataJson = JSON.parse(dataStr);
      console.log("update order's status = success where app_trans_id =", dataJson['app_trans_id']);

      // Thực hiện cập nhật trạng thái đơn hàng trong cơ sở dữ liệu 

      result.return_code = 1;
      result.return_message = 'success';
    }
  } catch (ex) {
    console.log('Lỗi:', ex.message);
    result.return_code = 0;
    result.return_message = ex.message;
  }

  // Trả về phản hồi cho ZaloPay
  res.json(result);
});


// Endpoint kiểm tra trạng thái đơn hàng
router.post('/check-status-order', async (req, res) => {
  const { app_trans_id } = req.body;

  let postData = {
    app_id: config.app_id,
    app_trans_id,
  };

  let data = `${postData.app_id}|${postData.app_trans_id}|${config.key1}`;
  postData.mac = CryptoJS.HmacSHA256(data, config.key1).toString();

  let postConfig = {
    method: 'post',
    url: 'https://sb-openapi.zalopay.vn/v2/query',
    headers: {
      'Content-Type': 'application/x-www-form-urlencoded',
    },
    data: qs.stringify(postData),
  };

  try {
    const result = await axios(postConfig);
    console.log(result.data);
    return res.status(200).json(result.data);
  } catch (error) {
    console.log('lỗi');
    console.log(error);
    return res.status(500).json({ error: 'Internal Server Error' });
  }
});


module.exports = router;
