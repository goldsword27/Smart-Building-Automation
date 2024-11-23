const express = require('express');
const { getLogs } = require('../controllers/logController');
const { authenticateToken, authorizeRole } = require('../middleware/authMiddleware');

const router = express.Router();

// Sadece yöneticiler logları görebilir
router.get('/', authenticateToken, authorizeRole(['administrator']), getLogs);

module.exports = router;
