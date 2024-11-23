
const express = require('express');
const { addDevice, listDevices, controlDevice, deleteDevice, getStatus } = require('../controllers/deviceController');
const { authenticateToken, authorizeRole } = require('../middleware/authMiddleware');

const router = express.Router();

// Sadece yöneticiler cihaz ekleyebilir veya silebilir
router.post('/add', authenticateToken, authorizeRole(['administrator']), addDevice);
router.delete('/delete/:id', authenticateToken, authorizeRole(['administrator']), deleteDevice);

// Hem kullanıcılar hem de yöneticiler cihazları listeleyebilir ve kontrol edebilir
router.get('/', authenticateToken, listDevices);
router.post('/control/:id', authenticateToken, authorizeRole(['user', 'administrator']), controlDevice);

// Cihaz durumları için yeni rota
router.get('/status', authenticateToken, getStatus);

module.exports = router;
