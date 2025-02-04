const express = require('express');
const router = express.Router();
const User = require('../models/User');
const RefreshToken = require('../models/refreshToken');
const bcrypt = require('bcryptjs');
const jwt = require('jsonwebtoken');
const cookieParser = require('cookie-parser');

router.use(cookieParser());

// GET all Users
router.get('/', async (req, res) => {
  try {
    const users = await User.find();
    res.json(users);
  } catch (err) {
    res.status(500).json({ message: err.message });
  }
});

// GET User by ID
router.get('/id/:id', async (req, res) => {
  try {
    const user = await User.findById(req.params.id);
    if (!user) return res.status(404).json({ message: 'User not found' });
    res.json(user);
  } catch (err) {
    res.status(500).json({ message: err.message });
  }
});

//Get user by phonenumber
router.get('/phone/:phoneNumber', async (req, res) => {
  try {
    const phoneNumber = req.params.phoneNumber;
    const user = await User.findOne({ phoneNumber: phoneNumber });
    
    if (!user) {
      return res.status(404).json({ message: 'User not found' });
    }
    
    res.json(user);
  } catch (error) {
    res.status(500).json({ message: error.message });
  }
});

// Đăng nhập
router.post('/login', async (req, res) => {
    const { username, password } = req.body;

    try {
        const user = await User.findOne({ phoneNumber: username });
        if (!user) return res.status(400).json({ message: 'User not found' });

        const isMatch = await bcrypt.compare(password, user.password);
        if (!isMatch) return res.status(400).json({ message: 'Invalid credentials' });

        const accessToken = jwt.sign({ id: user._id }, process.env.ACCESS_TOKEN_SECRET, { expiresIn: '1h' });
        const refreshToken = jwt.sign({ id: user._id }, process.env.REFRESH_TOKEN_SECRET, { expiresIn: '7d' });

        // Lưu refresh token vào cơ sở dữ liệu
        const newRefreshToken = new RefreshToken({
            token: refreshToken,
            userId: user._id,
            expiryDate: new Date(Date.now() + 7 * 24 * 60 * 60 * 1000) // 7 ngày
        });
        await newRefreshToken.save();

        res.json({
            accessToken,
            refreshToken,
            user: {
                id: user._id,
                name: user.name,
                phoneNumber: user.phoneNumber
            }
        });

    } catch (error) {
        res.status(500).json({ message: error.message });
    }
});

// Đăng xuất
router.post('/logout', async (req, res) => {
    try {
        const refreshToken = req.body.refreshToken;
        console.log('Logout attempt with refresh token:', refreshToken);

        if (!refreshToken) {
            console.log('No refresh token found. You are already logged out.');
            return res.status(200).json({ message: 'No refresh token found. You are already logged out.' });
        }

        // Tìm và cập nhật refresh token để thu hồi
        const token = await RefreshToken.findOneAndUpdate(
            { token: refreshToken },
            { isRevoked: true },
            { new: true }
        );

        if (!token) {
            console.log('Refresh token not found. You are already logged out.');
            return res.status(200).json({ message: 'Refresh token not found. You are already logged out.' });
        }

        console.log('Refresh token revoked:', token);

        // Xóa cookie
        res.clearCookie('refreshToken');
        res.json({ message: 'Logged out successfully' });
    } catch (error) {
        console.error('Error during logout:', error);
        res.status(500).json({ message: error.message });
    }
});

// Tạo access token mới từ refresh token
router.post('/token', async (req, res) => {
    const { refreshToken } = req.body;
    if (!refreshToken) return res.status(401).json({ message: 'Refresh token is required' });

    try {
        const token = await RefreshToken.findOne({ token: refreshToken, isRevoked: false });
        if (!token) return res.status(403).json({ message: 'Invalid refresh token' });

        const user = await User.findById(token.userId);
        if (!user) return res.status(403).json({ message: 'Invalid refresh token' });

        const accessToken = jwt.sign({ id: user._id }, process.env.ACCESS_TOKEN_SECRET, { expiresIn: '15m' });
        res.json({ accessToken });
    } catch (error) {
        res.status(500).json({ message: error.message });
    }
});

// tạo user
router.post('/signup', async (req, res) => {
  try {
    console.log('Request Body:', req.body); // Log dữ liệu nhận từ client
    const hashedPassword = await bcrypt.hash(req.body.password, 10);
    const user = new User({
      address: req.body.address,
      id: req.body.id,
      name: req.body.name,
      password: hashedPassword,
      phoneNumber: req.body.phoneNumber,
      strUriAvatar: req.body.strUriAvatar || ''
    });

    const newUser = await user.save();
    res.status(201).json(newUser);
  } catch (err) {
    console.error('Error creating user:', err.message);
    res.status(400).json({ message: err.message });
  }
});

// Thay đổi mật khẩu người dùng
router.patch('/change-password/:id', async (req, res) => {
    const { oldPassword, newPassword } = req.body;
    const userId = req.params.id;

    try {
        const user = await User.findOne({id:userId});
        if (!user) return res.status(404).json({ message: 'User not found' });

        const isMatch = await bcrypt.compare(oldPassword, user.password);
        if (!isMatch) return res.status(400).json({ message: 'Old password is incorrect' });

        const hashedNewPassword = await bcrypt.hash(newPassword, 10);
        user.password = hashedNewPassword;
        await user.save();

        res.json({ message: 'Password changed successfully' });
    } catch (error) {
        console.error('Error changing password:', error);
        res.status(500).json({ message: error.message });
    }
});

// UPDATE a User by ID
router.patch('/id/:id', async (req, res) => {
  try {
    const id = req.params.id;
    const user = await User.findOne({ id: id });
    if (!user) return res.status(404).json({ message: 'User not found' });
    
    if (req.body.address !== undefined) user.address = req.body.address;
    if (req.body.name !== undefined) user.name = req.body.name;
    if (req.body.password !== undefined) user.password = req.body.password;
    if (req.body.phoneNumber !== undefined) user.phoneNumber = req.body.phoneNumber;
    if (req.body.strUriAvatar !== undefined) user.strUriAvatar = req.body.strUriAvatar;

    const updatedUser = await user.save();
    res.json(updatedUser);
  } catch (err) {
    res.status(400).json({ message: err.message });
  }
});

// DELETE a User by ID
router.delete('/id/:id', async (req, res) => {
  try {
    const id = req.params.id;
    const user = await User.findOne({ id: id });
    if (!user) return res.status(404).json({ message: 'User not found' });

    await user.deleteOne();
    res.json({ message: 'Deleted User' });
  } catch (err) {
    res.status(500).json({ message: err.message });
  }
});

module.exports = router;

// Hàm tạo access token
const generateAccessToken = (user) => {
  // Create a plain object with only the needed user data
  const payload = {
      id: user._id,
      username: user.username,
      phoneNumber: user.phoneNumber
  };
  return jwt.sign(payload, process.env.ACCESS_TOKEN_SECRET, { expiresIn: '15m' });
};

// Hàm tạo refresh token
const generateRefreshToken = async (user) => {
  // Create a plain object with only the needed user data
  const payload = {
      id: user._id,
      username: user.username,
      phoneNumber: user.phoneNumber
  };
  const token = jwt.sign(payload, process.env.REFRESH_TOKEN_SECRET);
  
  const refreshToken = new RefreshToken({
      token: token,
      userId: user._id,
      expiryDate: new Date(Date.now() + 7 * 24 * 60 * 60 * 1000),
      isRevoked: false
  });
  
  await refreshToken.save();
  return token;
};