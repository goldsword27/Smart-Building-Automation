const express = require('express');
const { login, forgotPassword, verifyCode, resetPassword, addUser, listUsers, deleteUser } = require('../controllers/authController');
const { authenticateToken, authorizeRole } = require('../middleware/authMiddleware');

const router = express.Router();

router.post('/login', login);
router.post('/forgot-password', forgotPassword);
router.post('/verify-code', verifyCode);
router.post('/reset-password', resetPassword);

// Sadece yöneticiler kullanıcı ekleyebilir, listeleyebilir veya silebilir
router.post('/add-user', authenticateToken, authorizeRole(['administrator']), addUser);
router.get('/users', authenticateToken, authorizeRole(['administrator']), listUsers);
router.delete('/delete-user/:id', authenticateToken, authorizeRole(['administrator']), deleteUser);

module.exports = router;
