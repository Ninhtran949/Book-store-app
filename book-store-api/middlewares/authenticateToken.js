const jwt = require('jsonwebtoken');
const RefreshToken = require('../models/refreshToken');

const authenticateToken = async (req, res, next) => {
    const token = req.headers['authorization']?.split(' ')[1];
    if (!token) return res.status(401).json({ message: 'Access token is required' });

    try {
        const decoded = jwt.verify(token, process.env.ACCESS_TOKEN_SECRET);

        // Kiểm tra xem refresh token có bị thu hồi không
        const refreshToken = await RefreshToken.findOne({ userId: decoded.id, isRevoked: false });
        if (!refreshToken) {
            return res.status(403).json({ message: 'Invalid access token' });
        }

        req.user = decoded;
        next();
    } catch (err) {
        return res.status(403).json({ message: 'Invalid access token' });
    }
};

module.exports = authenticateToken;
