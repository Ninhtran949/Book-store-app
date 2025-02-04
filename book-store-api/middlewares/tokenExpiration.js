const RefreshToken = require('../models/refreshToken');

const checkTokenExpiration = async (req, res, next) => {
  try {
    // Xóa các refresh token hết hạn hoặc đã thu hồi
    await RefreshToken.deleteMany({
      $or: [
        { expiryDate: { $lt: new Date() } },
        { isRevoked: true }
      ]
    });
    next();
  } catch (error) {
    next(error);
  }
};

module.exports = checkTokenExpiration;
